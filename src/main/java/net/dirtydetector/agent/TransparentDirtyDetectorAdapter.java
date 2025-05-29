package net.dirtydetector.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class TransparentDirtyDetectorAdapter extends ClassVisitor implements ITransparentDirtyDetectorDef, IJavaCollections {

    private final static Logger LOGGER = LogManager.getLogger(TransparentDirtyDetectorAdapter.class.getName());
    static {
        Configurator.setLevel(TransparentDirtyDetectorAdapter.class.getName(), LogginProperties.TransparentDirtyDetectorAdapter);
    }
    
    
    private boolean isFieldPresent = false;
    private List<String> ignoredFields = new ArrayList();
    private List<String> collectionsFields = new ArrayList();
    private String className;
//    private HashSet<String> javaCollections = new HashSet();
    
    public TransparentDirtyDetectorAdapter(ClassVisitor cv, List<String> ignoredFields, List<String> collectionsFields) {
        super(Opcodes.ASM9, cv);
        this.ignoredFields = ignoredFields;
        this.collectionsFields = collectionsFields;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        String[] addInterfaces = Arrays.copyOf(interfaces, interfaces.length + 1); //create new array from old array and allocate one more element
        addInterfaces[addInterfaces.length - 1] = ITransparentDirtyDetector.class.getName().replace(".", "/");
        LOGGER.log(Level.DEBUG, "visitando clase: {} super: {} y agregando la interface.",
                new Object[]{name, superName});
        // se elimina la propiedad FINAL de todas las clases visitadas para que 
        // CGLIB pueda extenderlas.
        LOGGER.log(Level.TRACE, ((access & Opcodes.ACC_FINAL) > 0)?"Clase FINAL detectada":"");
        
        className = name;
        cv.visit(version, access & (~Opcodes.ACC_FINAL) , name, signature, superName, addInterfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        LOGGER.log(Level.TRACE, "field: "+name+" : "+desc+" : "+signature);
        
        if (name.equals(DIRTYMARK)) {
            isFieldPresent = true;
            LOGGER.log(Level.DEBUG, "El campo ya existe!!!! WARNING!!! Esto no deberia ocurrir!!! ************************");
        }
//        if (getJavaCollections().contains(desc) && !ignoredFields.contains(name)) {
//            LOGGER.log(Level.TRACE, "Colección detectada: "+name+" : "+desc);
//            collectionsFields.add(name);
//        }
        return cv.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv;
        LOGGER.log(Level.TRACE, "visitando método: {} desc: {}", new Object[]{name, desc});
        //se quitan todos los FINAL de los métodos
        mv = cv.visitMethod(access & (~Opcodes.ACC_FINAL), name, desc, signature, exceptions);
        if ((mv != null) && !name.equals("<clinit>")) {
            if (name.equals("<init>")) {
                // si es un constructor inicializar el Set
                LOGGER.log(Level.TRACE, "Constructor detectado!");
                mv = new WriteConstructorAccessActivatorAdapter(className,mv, ignoredFields);
            } else {
                LOGGER.log(Level.DEBUG, ">>>>>>>>>>> Instrumentando método: {}", name);
                LOGGER.log(Level.DEBUG, ">>>>>>>>>>> owner: {} - access: {} - name: {} - desc: {}",
                                                new Object[]{ className,access,name,desc});
                mv = new WriteAccessActivatorAdapter(Opcodes.ASM9, className, access, name, desc, mv, ignoredFields, collectionsFields);
            }
            LOGGER.log(Level.TRACE, "fin instrumentación ---------------------------------------------------");
        } else {
            LOGGER.log(Level.TRACE, "mv = NULL !!!! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }
        return mv;
    }

    @Override
    public void visitEnd() {
        if (!isFieldPresent) {
            LOGGER.log(Level.DEBUG, "Agregando el campo");
            FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC, DIRTYMARK,
                    org.objectweb.asm.Type.BOOLEAN_TYPE.getDescriptor(), null, null);
            if (fv != null) {
                fv.visitEnd();
                LOGGER.log(Level.DEBUG, "DIRTYMARK fv.visitEnd..");
            }
            fv = cv.visitField(Opcodes.ACC_PUBLIC, MODIFIEDFIELDS, "Ljava/util/Set;", null, null);
            if (fv != null) {
                fv.visitEnd();
                LOGGER.log(Level.DEBUG, "MODIFIEDFIELDS fv.visitEnd..");
            }
        }
        cv.visitEnd();
    }

}
