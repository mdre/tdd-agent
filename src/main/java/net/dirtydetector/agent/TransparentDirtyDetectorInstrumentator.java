package net.dirtydetector.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class TransparentDirtyDetectorInstrumentator
        implements ClassFileTransformer, ITransparentDirtyDetectorDef {

    private final static Logger LOGGER = LogManager.getLogger(TransparentDirtyDetectorInstrumentator.class.getName());

    static {
        Configurator.setLevel(TransparentDirtyDetectorInstrumentator.class.getName(),
                            LogginProperties.TransparentDirtyDetectorInstrumentator);
    }

    private String dumpDirectoryPath;
    private List<String> instrumentableClassDetector = new ArrayList<>();
    private List<String> ignored = new ArrayList(Arrays.asList("java/",
                                                               "jdk/",
                                                               "sun/",
                                                               "com/sun/",
                                                               "org/gradle/", 
                                                               "worker/org/gradle/",
                                                               "org/slf4j",
                                                               "org/junit",
                                                               "org/apache"
                                                               )
                                                    );
    
    /**
     * Instrumentador.
     */
    public TransparentDirtyDetectorInstrumentator(String classDetector) {
        if (classDetector != null) {
            this.instrumentableClassDetector.addAll(List.of(classDetector.split("&")));
        }
    }

    /**
     * Add a class anotation detector. The anotation must be the full package.
     * Ej: com.application.Entity
     * 
     * @param detector
     * @return 
     */
    public TransparentDirtyDetectorInstrumentator addDetector(String detector) {
        this.instrumentableClassDetector.add(detector);
        return this;
    }
    
    public TransparentDirtyDetectorInstrumentator removeDetector(String detector) {
        this.instrumentableClassDetector.remove(detector);
        return this;
    }
    
    public List<String> getDetectors() {
        return instrumentableClassDetector;
    }
    
    /**
     * Remove all de ignores class
     * @return 
     */
    public TransparentDirtyDetectorInstrumentator clearIgnored() {
        this.ignored.clear();
        return this;
    }
    
    /**
     * Add a string to the ignored list. The agent will ignored all classes that start
     * with any string in the list.
     * For example, if is called addIgnore("com.google") all classes that start with com.google.xxx 
     * will be ignored and not analyzed.
     * 
     * @param ignore
     * @return 
     */
    public TransparentDirtyDetectorInstrumentator addIgnore(String ignore) {
        this.ignored.add(ignore.replace(".", "/"));
        return this;
    }
    
    public TransparentDirtyDetectorInstrumentator removeIgnore(String ignore) {
        this.ignored.remove(ignore.replace(".", "/"));
        return this;
    }
    
    public List<String> getIgnored() {
        return this.ignored;
    }
    
    // public TransparentDirtyDetectorInstrumentator setClassLevelLog(Class<?> clazz, Level level) {
    //     Logger L = Logger.getLogger(clazz.getName());
    //     L.setLevel(level);
    //     return this;
    // }
    
    public TransparentDirtyDetectorInstrumentator enableDumpDebugDirectory(String pathToDumpDirectory) {
        this.dumpDirectoryPath = pathToDumpDirectory;
        return this;
    }
    
    /**
     * Implementación del Agente.
     *
     * Si se establece el nivel DEBUG para esta clase, se realiza un volcado a disco de las clases intervenidas en la carpeta /tmp/asm.
     *
     * @param loader classloader
     * @param className nombre de la clase
     * @param classBeingRedefined clase
     * @param protectionDomain poterctionDomain
     * @param classfileBuffer buffer de datos con la clases a redefinir
     * @return byte[] con la clase redefinida
     * @throws IllegalClassFormatException ex
     */
    @Override
    public synchronized byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {
        // verificar si se debe analizar o se ignora
        boolean shouldAnalyze = true;
        for (String ignore : this.ignored) {
            if (className.startsWith(ignore)) {
                shouldAnalyze = false;
                LOGGER.log(Level.TRACE, "Ignorando clase: {}...", className);
                break;
            }
        }
        
        if (shouldAnalyze) {
            LOGGER.log(Level.DEBUG, "analizando clase: {}...", className);

            ClassReader cr = new ClassReader(classfileBuffer);
            if (isInterface(cr)) {
                // No procesar las interfaces
                LOGGER.log(Level.DEBUG, "Interface detectada {}. NO PROCESAR!", className);
                return classfileBuffer;
            }

            // determinar si se trata de una inner class o una clase base
            if (!className.substring(className.lastIndexOf("/") + 1).contains("$")) {
                // si no contienen el signo $ significa que es una clase base
                // y la procesamos normalmente

                ClassWriter cw = new ClassWriter(cr, 0);
                InstrumentableClassDetector icd = new InstrumentableClassDetector(cw).setInstrumentableClassFilter(instrumentableClassDetector);
                cr.accept(icd, 0);

                LOGGER.log(Level.DEBUG, "isInstrumentable: " + icd.isInstrumentable());
                if (icd.isInstrumentable() && !icd.isInstrumented()) {
                    LOGGER.log(Level.DEBUG, ""
                            + "\n****************************************************************************"
                            + "\nRedefiniendo on-the-fly {}..."
                            + "\n****************************************************************************",
                            className);
                    ClassReader crRedefine = new ClassReader(classfileBuffer);
                    cw = new ClassWriter(crRedefine, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
                        // Asegurar que se usa el mismo CL para cargar las clases.
                        @Override
                        protected String getCommonSuperClass(String type1, String type2) {
                            LOGGER.log(Level.DEBUG, "type1: " + type1 + "   - type2: " + type2);
                            Class<?> c, d;
                            try {
                                c = Class.forName(type1.replace('/', '.'), false, loader);
                                d = Class.forName(type2.replace('/', '.'), false, loader);
                            } catch (Exception e) {
                                throw new RuntimeException(e.toString());
                            }
                            if (c.isAssignableFrom(d)) {
                                return type1;
                            }
                            if (d.isAssignableFrom(c)) {
                                return type2;
                            }
                            if (c.isInterface() || d.isInterface()) {
                                return "java/lang/Object";
                            } else {
                                do {
                                    c = c.getSuperclass();
                                } while (!c.isAssignableFrom(d));
                                return c.getName().replace('.', '/');
                            }
                        }
                    };
                    TransparentDirtyDetectorAdapter taa = new TransparentDirtyDetectorAdapter(cw, icd.getIgnoredFields(), icd.getCollectionsFields());
                    try {
                        crRedefine.accept(taa, ClassReader.EXPAND_FRAMES | ClassReader.SKIP_FRAMES ); // estaba con SKIP_FRAMES
                    } catch (Exception e) {
                        LOGGER.log(Level.ERROR, "ERROR <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                        e.printStackTrace();
                    }
                    // instrumentar el método ___getDirty()
                    LOGGER.log(Level.DEBUG, "insertando el método ___isDirty() ...");
                    MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, ISDIRTY, "()Z", null, null);
                    mv.visitCode();
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitFieldInsn(Opcodes.GETFIELD, className, DIRTYMARK, "Z");
                    mv.visitInsn(Opcodes.IRETURN);
                    mv.visitMaxs(1, 1);
                    mv.visitEnd();

                    // instrumentar el método ___setDirty()
                    LOGGER.log(Level.DEBUG, "insertando el método ___setDirty() ...");
                    mv = cw.visitMethod(Opcodes.ACC_PUBLIC, SETDIRTY, "()V", null, null);
                    mv.visitCode();
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitFieldInsn(Opcodes.PUTFIELD, className, DIRTYMARK, "Z");
                    mv.visitInsn(Opcodes.RETURN);
                    mv.visitMaxs(1, 1);
                    mv.visitEnd();

                    LOGGER.log(Level.DEBUG, "insertando el método ___tdd___clearDirty() ...");
                    mv = cw.visitMethod(Opcodes.ACC_PUBLIC, CLEARDIRTY, "()V", null, null);
                    mv.visitCode();
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitInsn(Opcodes.ICONST_0);
                    mv.visitFieldInsn(Opcodes.PUTFIELD, className, DIRTYMARK, "Z");
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitFieldInsn(Opcodes.GETFIELD, className, MODIFIEDFIELDS, "Ljava/util/Set;");
                    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Set", "clear", "()V", true);
                    mv.visitInsn(Opcodes.RETURN);
                    mv.visitMaxs(2, 1);
                    mv.visitEnd();
                    
                    LOGGER.log(Level.DEBUG, "insertando el método ___tdd___getModifiedFields() ...");
                    mv = cw.visitMethod(Opcodes.ACC_PUBLIC, GETMODIFIEDFIELDS, "()Ljava/util/Set;", null, null);
                    mv.visitCode();
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitFieldInsn(Opcodes.GETFIELD, className, MODIFIEDFIELDS, "Ljava/util/Set;");
                    mv.visitInsn(Opcodes.ARETURN);
                    mv.visitMaxs(1, 1);
                    mv.visitEnd();
                    
                    LOGGER.log(Level.DEBUG, "insertando el método ___tdd___addModifiedFields(String f) ...");
                    try {
                        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, ADDMODIFIEDFIELD, "(Ljava/lang/String;)V", null, null);
                        mv.visitCode();
                        Label lblStart = new Label();
                        Label lblEnd = new Label();
                        mv.visitLabel(lblStart);
    //                    mv.visitLineNumber(61, label0);
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitFieldInsn(Opcodes.GETFIELD, className, MODIFIEDFIELDS, "Ljava/util/Set;");
                        mv.visitVarInsn(Opcodes.ALOAD, 1);
                        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Set", "add", "(Ljava/lang/Object;)Z", true);
                        mv.visitInsn(Opcodes.POP);
    //                    Label label1 = new Label();
    //                    mv.visitLabel(label1);
    //                    mv.visitLineNumber(62, label1);
                        mv.visitInsn(Opcodes.RETURN);
    //                    Label label2 = new Label();
                        mv.visitLabel(lblEnd);
                        LOGGER.log(Level.DEBUG, "\n\n\n\n\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+className);
                        mv.visitLocalVariable("this", "L"+className+";", null, lblStart, lblEnd, 0);
                        mv.visitLocalVariable("f", "Ljava/lang/String;", null, lblStart, lblEnd, 1);
                        mv.visitMaxs(2, 2);
                        mv.visitEnd();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    
                    LOGGER.log(Level.DEBUG, "fin instrumentación métodos ITransparentDirtyDetector.");
                    
                    // detectar si tiene el contructor por defecto y en caso de no tenerlo insertar uno.
                    if (!icd.hasDefaultContructor()) {
                        LOGGER.log(Level.DEBUG, "No se ha encontrado el contructor por defecto. Insertando uno...");
                        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
                        mv.visitCode();
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
//                        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, cr.getSuperName(), "<init>", "()V", false);
                        
                        // inicializar el Set de campos
                        LOGGER.log(Level.TRACE, "inicializar el hashset...");
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitTypeInsn(Opcodes.NEW, "java/util/HashSet");
                        mv.visitInsn(Opcodes.DUP);
                        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/HashSet", "<init>", "()V");
                        mv.visitFieldInsn(Opcodes.PUTFIELD, className, MODIFIEDFIELDS, "Ljava/util/Set;");
                        
                        mv.visitInsn(Opcodes.RETURN);
                        mv.visitMaxs(1, 1);
                        mv.visitEnd();
                    } else {
                        LOGGER.log(Level.DEBUG, "Se ha encontrado el contructor por defecto. ");
                    }
                    LOGGER.log(Level.INFO, "-=-=-=-=-=-=-=-");
                    LOGGER.log(Level.INFO, "dump to: {}",(dumpDirectoryPath==null?"NULL":dumpDirectoryPath));
                    if (this.dumpDirectoryPath!=null || (LOGGER.getLevel() == Level.DEBUG)||(LOGGER.getLevel() == Level.TRACE)) {
                        writeToFile(className, cw.toByteArray());
                    }
                    LOGGER.log(Level.DEBUG, "\n\n\nFIN instrumentación {}"
                            + "\n^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
                            + "\n****************************************************************************",
                            new Object[]{className}
                    );
                    return cw.toByteArray();
                }
            } else {
                // si contiene el $, significa que es una innerClass. 
                // debermos revisar si corresponde intrumentarla visitando la clase principal
                String outerClass = className.substring(0, className.lastIndexOf("$")).replace("/", ".");

                try {
                    LOGGER.log(Level.DEBUG, "analizando innerClass: " + className);
                    ClassReader ocr = new ClassReader(outerClass);
                    ClassWriter ocw = new ClassWriter(ocr, 0);
                    InstrumentableClassDetector oicd = new InstrumentableClassDetector(ocw).setInstrumentableClassFilter(instrumentableClassDetector);
                    ocr.accept(oicd, 0);
                    if (oicd.isInstrumentable() && !oicd.isInstrumented()) {
                        LOGGER.log(Level.DEBUG, "Se encontró la anotattion Entity en la clase contenedora. Instrumentar la innerClass");

                        // FIXME: esto debería sacarse a una clase aparte o a un método, para que quede mas prolijo. 
                        LOGGER.log(Level.DEBUG, ""
                                + "\n****************************************************************************"
                                + "\nRedefiniendo innerClass on-the-fly {}..."
                                + "\n****************************************************************************",
                                className);
                        ClassReader crRedefine = new ClassReader(classfileBuffer);

                        ClassWriter cw = new ClassWriter(crRedefine, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
                            // Asegurar que se usa el mismo CL para cargar las clases.
                            @Override
                            protected String getCommonSuperClass(String type1, String type2) {
                                LOGGER.log(Level.DEBUG, "type1: " + type1 + "   - type2: " + type2);
                                Class<?> c, d;
                                try {
                                    c = Class.forName(type1.replace('/', '.'), false, loader);
                                    d = Class.forName(type2.replace('/', '.'), false, loader);
                                } catch (Exception e) {
                                    throw new RuntimeException(e.toString());
                                }
                                if (c.isAssignableFrom(d)) {
                                    return type1;
                                }
                                if (d.isAssignableFrom(c)) {
                                    return type2;
                                }
                                if (c.isInterface() || d.isInterface()) {
                                    return "java/lang/Object";
                                } else {
                                    do {
                                        c = c.getSuperclass();
                                    } while (!c.isAssignableFrom(d));
                                    return c.getName().replace('.', '/');
                                }
                            }
                        };

                        // activar el chequeo de la clase
                        TransparentDirtyDetectorInnerClassAdapter taa = new TransparentDirtyDetectorInnerClassAdapter(new CheckClassAdapter(cw,true), className, oicd.getIgnoredFields(), oicd.getCollectionsFields());
                        try {
                            crRedefine.accept(taa, ClassReader.EXPAND_FRAMES);

                            if (this.dumpDirectoryPath!=null || (LOGGER.getLevel() == Level.DEBUG)||(LOGGER.getLevel() == Level.TRACE)) {
                                writeToFile(className, cw.toByteArray());
                            }
                            LOGGER.log(Level.DEBUG, "FIN instrumentación {}"
                                    + "\n^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
                                    + "\n****************************************************************************",
                                    className
                            );
                        } catch (Exception e) {
                            LOGGER.log(Level.ERROR, "ERROR <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                            e.printStackTrace();
                        }
                        // descomentar estas líneas junto con la de arriba para realizar un checkeo de la clase.
    //                    System.out.println("****************************************************************************");
    //                    System.out.println("****************************************************************************");
    //                    System.out.println("****************************************************************************\n");
    //                    StringWriter stringWriter = new StringWriter();
    //                    PrintWriter printWriter = new PrintWriter(stringWriter);
    //                    CheckClassAdapter.verify(new ClassReader(cw.toByteArray()), false, printWriter);
    //                    System.out.println("\n****************************************************************************");
    //                    System.out.println("****************************************************************************");
    //                    System.out.println("****************************************************************************");
                        return cw.toByteArray();

                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.DEBUG, "No se ha podido analizar la clase inner "+className);
                }
            }
        }
        return classfileBuffer;
    }
    
    /**
     * Herramienta para realizar un volcado de la clase a disco.
     *
     * @param className nombre del archivo a graba
     * @param myByteArray datos de la clase.
     */
    private void writeToFile(String className, byte[] myByteArray) {
        LOGGER.log(Level.DEBUG, "Escribiendo archivo a disco en /tmp/asm");
        try {
            String outPath = this.dumpDirectoryPath!=null ? this.dumpDirectoryPath:"/tmp/asm";
            File theDir = new File(outPath);
            if (!theDir.exists()) {
                theDir.mkdir();
            }

            FileOutputStream fos = new FileOutputStream(outPath + className.substring(className.lastIndexOf("/")) + ".class");
            fos.write(myByteArray);
            fos.close();

        } catch (IOException ex) {
            LOGGER.log(Level.ERROR, ex);
        }
    }

    /**
     * Determina si se trata de una interface o una clase.
     *
     * @param cr clase a analizar
     * @return true si es una interface
     */
    public synchronized boolean isInterface(ClassReader cr) {
        return ((cr.getAccess() & 0x200) != 0);
    }
}
