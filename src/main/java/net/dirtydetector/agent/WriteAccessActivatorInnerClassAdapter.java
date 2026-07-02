package net.dirtydetector.agent;

import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Same as {@link WriteAccessActivatorAdapter}, but for a non-static inner class: field writes
 * that matter here are writes to the *outer* class's fields (reached directly, since same-nest
 * field access compiles to a plain PUTFIELD with owner = outer class), and SETDIRTY /
 * ADDMODIFIEDFIELD must be invoked through the synthetic {@code this$0} reference to the outer
 * instance rather than on {@code this}.
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class WriteAccessActivatorInnerClassAdapter extends AbstractWriteAccessActivatorAdapter {

    private final String outerClass;

    public WriteAccessActivatorInnerClassAdapter(int api,
                                                  String className,
                                                  int access,
                                                  String name,
                                                  String descriptor,
                                                  String signature,
                                                  String[] exceptions,
                                                  MethodVisitor methodVisitor,
                                                  List<String> ignoreFields,
                                                  List<String> collectionFields) {
        super(api, className, access, name, descriptor, signature, exceptions, methodVisitor, ignoreFields, collectionFields);
        this.outerClass = className.substring(0, className.lastIndexOf("$"));
    }

    @Override
    protected boolean isTrackedFieldWrite(FieldInsnNode putfield) {
        return outerClass.equals(putfield.owner) && !ignoreFields.contains(putfield.name);
    }

    @Override
    protected void pushDirtyTrackerRef(InsnList il) {
        il.add(new VarInsnNode(Opcodes.ALOAD, 0));
        il.add(new FieldInsnNode(Opcodes.GETFIELD, owner, "this$0", "L" + outerClass + ";"));
    }

    @Override
    protected String dirtyTrackerClass() {
        return outerClass;
    }
}
