package net.dirtydetector.agent;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class WriteConstructorAccessActivatorAdapter extends MethodVisitor implements ITransparentDirtyDetectorDef {

    private final static Logger LOGGER = Logger.getLogger(WriteConstructorAccessActivatorAdapter.class.getName());
    private List<String> ignoreFields;
    private String className;
    private boolean initialize = true;
    
    static {
        if (LOGGER.getLevel() == null) {
            LOGGER.setLevel(LogginProperties.WriteConstructorAccessActivatorAdapter);
        }
    }
    
    public WriteConstructorAccessActivatorAdapter(String className, MethodVisitor mv, List ignoreFields) {
        super(Opcodes.ASM9, mv);
        this.ignoreFields = ignoreFields;
        this.className = className;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        LOGGER.log(Level.FINEST, "visit code!");
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
        LOGGER.log(Level.FINEST, "visit MethodInsn!");
        if (this.initialize) {
            LOGGER.log(Level.FINEST, "insertar la inicialización del hashset...");
            LOGGER.log(Level.FINEST, "className: {1}",className);

            // Crear una nueva instancia de HashSet
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitTypeInsn(Opcodes.NEW, "java/util/HashSet");
            mv.visitInsn(Opcodes.DUP);

            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/HashSet", "<init>", "()V");
            mv.visitFieldInsn(Opcodes.PUTFIELD, this.className, MODIFIEDFIELDS, "Ljava/util/Set;");
            this.initialize = false;
        }
    }
    
    @Override
    public synchronized void visitFieldInsn(int opcode, String owner, String name, String desc) {
        LOGGER.log(Level.FINER, "owner: {0} - name: {1} - desc: {2} - opcode: {3}", new Object[]{owner, name, desc, opcode});
        //  owner: test/Outer$1 - name: this$0 - desc: Ltest/Outer;

        mv.visitFieldInsn(opcode, owner, name, desc);
        LOGGER.log(Level.FINEST, "fin --------------------------------------------------");
    }
    
    
    @Override
    public void visitEnd() {

        LOGGER.log(Level.FINEST, "fin Constructor MethodVisitor -------------------------------------");
//        mv.visitMaxs(0, 0);
        super.visitEnd();
    }
    
}

