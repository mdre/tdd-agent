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
public class WriteAccessActivatorInnerClassAdapter extends MethodVisitor
        implements ITransparentDirtyDetectorDef {

    private final static Logger LOGGER = Logger.getLogger(WriteAccessActivatorInnerClassAdapter.class.getName());
    static {
        if (LOGGER.getLevel() == null) {
            LOGGER.setLevel(LogginProperties.WriteAccessActivatorInnerClassAdapter);
        }
    }
    
    private boolean activate = false;
    private String className;
    private String outerClass;
    private List<String> ignoredFields;

    public WriteAccessActivatorInnerClassAdapter(MethodVisitor mv, String cn, List<String> ignoredFields) {
        super(Opcodes.ASM7, mv);
        this.className = cn;
        this.ignoredFields = ignoredFields;
        this.outerClass = className.substring(0, className.lastIndexOf("$"));
    }

    /**
     * Add a call to setDirty in every method that has a PUTFIELD in its code.
     * @param opcode código a analizar
     */
    @Override
    public synchronized void visitInsn(int opcode) {
        LOGGER.log(Level.FINER, "Activate: {0}", this.activate);
        if ((this.activate)&&((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW)) {
            LOGGER.log(Level.FINER, "Agregando llamada a setDirty...");
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            //getfield test/OuterTarget$1.this$0:test.OuterTarget
            
            LOGGER.log(Level.FINER, "className: {0}, outerClass: {1}", new String[]{className,outerClass});
            mv.visitFieldInsn(Opcodes.GETFIELD, this.className,"this$0","L"+this.outerClass+";");
            
            //mv.visitInsn(Opcodes.ICONST_1);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, outerClass, SETDIRTY, "()V", false);
            //mv.visitFieldInsn(Opcodes.PUTFIELD, owner, "__ogm__dirtyMark", "Z");
        }
        mv.visitInsn(opcode);
        LOGGER.log(Level.FINEST, "fin --------------------------------------------------");
    }

    @Override
    public synchronized void visitFieldInsn(int opcode, String owner, String name, String desc) {
        LOGGER.log(Level.FINER, "owner: {0} - name: {1} - desc: {2} - opcode: {3}", new Object[]{owner, name, desc, opcode});
        //  owner: test/Outer$1 - name: this$0 - desc: Ltest/Outer;

        mv.visitFieldInsn(opcode, owner, name, desc);
        if (opcode == Opcodes.PUTFIELD && outerClass.equals(owner) && !ignoredFields.contains(name)) {
            this.activate = true;
            
            // registrar el campo
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, className, "this$0", "L"+outerClass+";");
            mv.visitFieldInsn(Opcodes.GETFIELD, owner, MODIFIEDFIELDS, "Ljava/util/Set;");
            mv.visitLdcInsn(name);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Set", "add", "(Ljava/lang/Object;)Z", true);
            mv.visitInsn(Opcodes.POP); // Descartar el resultado booleano de add
        }
        LOGGER.log(Level.FINEST, "fin --------------------------------------------------");
    }

    @Override
    public void visitEnd() {
        LOGGER.log(Level.FINEST, "fin MethodVisitor -------------------------------------");
//        mv.visitMaxs(0, 0);
        super.visitEnd();
    }
    
}

