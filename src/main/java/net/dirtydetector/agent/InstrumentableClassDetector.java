/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.dirtydetector.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class InstrumentableClassDetector extends ClassVisitor implements IJavaCollections {

    private final static Logger LOGGER = LogManager.getLogger(InstrumentableClassDetector.class.getName());
    static {
        Configurator.setLevel(InstrumentableClassDetector.class.getName(), LogginProperties.InstrumentableClassDetector);
    }
    
    private boolean isInstrumentable = false;
    private boolean isInstrumented = false;
    private boolean hasDefaultContructor = false;
    private List<String> instrumentableClassFilter = new ArrayList();

    private List<String> innerClasses = new ArrayList<>();
    private List<String> ignoredFields = new ArrayList();
    private List<String> collectionsFields = new ArrayList();
    private String clazzName = null;
    
    public InstrumentableClassDetector(ClassVisitor cv) {
        super(Opcodes.ASM9, cv);
    }
    
    public List<String> getInstrumentableClassFilter() {
        return instrumentableClassFilter;
    }

    public InstrumentableClassDetector setInstrumentableClassFilter(List<String> instrumentableClassFilter) {
        instrumentableClassFilter.stream().forEach(cf -> { 
            this.instrumentableClassFilter.add("L"+cf.replace(".", "/")+";");
        });
        return this;
    }
    
    
    @Override
    public synchronized void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        String ddi = ITransparentDirtyDetector.class.getName().replace(".", "/");
        this.clazzName = name;
        for (String aInterface : interfaces) {
            if (aInterface.equals(ddi)) {
                // si tiene la interface implementada marcarla como instrumentada
                LOGGER.log(Level.DEBUG, "La clase "+name+" ya ha sido instrumentada. No hacer nada.");
                this.isInstrumented = true;
            }
        }
        cv.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public synchronized  AnnotationVisitor visitAnnotation(String ann, boolean bln) {
        LOGGER.log(Level.TRACE, "//=====================================================");
        LOGGER.log(Level.TRACE, "Annotations: ");
        LOGGER.log(Level.TRACE, "//=====================================================");
        LOGGER.log(Level.TRACE, "Annotation: >"+ann+"<");
        LOGGER.log(Level.TRACE, "detectors: >"+Arrays.toString(instrumentableClassFilter.toArray())+"<");
        if (instrumentableClassFilter.contains(ann) ) {  //ann.startsWith("Lnet/odbogm/annotations/Entity")
            LOGGER.log(Level.DEBUG, clazzName + ": Annotation: >"+ann+"<");
            LOGGER.log(Level.DEBUG, ">>>>>>>>>>> marcar como instrumentable");
            this.isInstrumentable = true;
        }
        
        LOGGER.log(Level.TRACE, "//=====================================================");
        return super.visitAnnotation(ann, bln); 
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        // FIXME: agregar una anotación para ignorar campos 
        FieldVisitor fv = super.visitField(access, name, descriptor, signature, value);
        if (this.isInstrumentable && (access & Opcodes.ACC_TRANSIENT)!= 0) {
            this.ignoredFields.add(name);
        } else {
            // determinar si se debe ignorar
            fv = new FieldAnnotationVisitor(fv, name);
            if (getJavaCollections().contains(descriptor) && !ignoredFields.contains(name)) {
                LOGGER.log(Level.TRACE, "Colección detectada: "+name+" : "+descriptor);
                collectionsFields.add(name);
            }
        }
        return fv;
    }

    @Override
    public synchronized MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv;
        LOGGER.log(Level.TRACE, "visitando método: " + name + " desc: " + desc + " signature: "+signature);
        mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if ((mv != null) && name.equals("<init>") && desc.equals("()V") ) {
            hasDefaultContructor = true;
        }
        return mv;
    }

    public synchronized boolean isInstrumentable() {
        return this.isInstrumentable;
    }
    
    public synchronized boolean isInstrumented() {
        return this.isInstrumented;
    }
    
    public synchronized boolean hasDefaultContructor() {
        return this.hasDefaultContructor;
    }

    public List<String> getIgnoredFields() {
        return this.ignoredFields;
    }
    
    public List<String> getCollectionsFields() {
        return this.collectionsFields;
    }
    
    
    class FieldAnnotationVisitor extends FieldVisitor {
        String name;
        public FieldAnnotationVisitor(FieldVisitor fv, String name) {
            super(Opcodes.ASM9, fv);
            this.name = name;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (Type.getType(descriptor).getClassName().equals(TDDIgnore.class.getName())) {
                ((InstrumentableClassDetector) cv).ignoredFields.add(name);
            }
            return super.visitAnnotation(descriptor, visible);
        }
    }
}
