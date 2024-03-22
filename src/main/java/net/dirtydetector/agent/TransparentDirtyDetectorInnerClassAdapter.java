package net.dirtydetector.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Solo se instrumentan los métodos para que pongan arctiven la propiedad DIRTYMARK del la clase contenedora.
 * Dado que es una clase inner debería estar ya instrumentada la clase outer.
 * 
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class TransparentDirtyDetectorInnerClassAdapter extends ClassVisitor implements ITransparentDirtyDetectorDef, IJavaCollections {

    private final static Logger LOGGER = Logger.getLogger(TransparentDirtyDetectorInnerClassAdapter.class.getName());
    static {
        if (LOGGER.getLevel() == null) {
            LOGGER.setLevel(LogginProperties.TransparentDirtyDetectorAdapter);
        }
    }
    
    private boolean isFieldPresent = false;
    private String className;
    private List<String> ignoredFields;
    private List<String> collectionsFields = new ArrayList();
    
    public TransparentDirtyDetectorInnerClassAdapter(ClassVisitor cv, String cn, List<String> ignoredFields, List<String> collectionsFields) {
        super(Opcodes.ASM9, cv);
        this.className = cn;
        this.ignoredFields = ignoredFields;
        this.collectionsFields = collectionsFields;
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv;
        LOGGER.log(Level.FINER, "visitando método: {0}, desc: {1}. signature: {1}", new Object[]{name, desc, signature});
        //se quitan todos los FINAL de los métodos
        mv = cv.visitMethod(access & (~Opcodes.ACC_FINAL), name, desc, signature, exceptions);
        if ((mv != null) && !name.equals("<init>") && !name.equals("<clinit>")) {
            LOGGER.log(Level.FINER, ">>>>>>>>>>> Instrumentando método: {0}", name);
            mv = new WriteAccessActivatorInnerClassAdapter(Opcodes.ASM9, className, access, name, desc, mv, ignoredFields, collectionsFields);
            LOGGER.log(Level.FINEST, "fin instrumentación ---------------------------------------------------");
        } else {
            LOGGER.log(Level.FINEST, "mv = NULL !!!! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }
        return mv;
    }

    @Override
    public void visitEnd() {
        cv.visitEnd();
    }

}
