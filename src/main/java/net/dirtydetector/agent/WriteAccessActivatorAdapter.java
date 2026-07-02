package net.dirtydetector.agent;

import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Instruments every method of a top-level (or static nested) class so that any write to one
 * of its tracked fields - directly via PUTFIELD, or indirectly via a mutating call on a
 * tracked collection field - ends up calling {@code SETDIRTY}/{@code ADDMODIFIEDFIELD} on
 * {@code this}.
 *
 * <p>See {@link AbstractWriteAccessActivatorAdapter} for how field origin is tracked (via a
 * {@link org.objectweb.asm.tree.analysis.Analyzer} + {@link FieldOriginInterpreter}), which is
 * what correctly distinguishes a write to {@code this.someCollection} from a write to a
 * same-named local variable or a freshly created collection.
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class WriteAccessActivatorAdapter extends AbstractWriteAccessActivatorAdapter {

    public WriteAccessActivatorAdapter(int api,
                                        String owner,
                                        int access,
                                        String name,
                                        String descriptor,
                                        String signature,
                                        String[] exceptions,
                                        MethodVisitor methodVisitor,
                                        List<String> ignoreFields,
                                        List<String> collectionFields) {
        super(api, owner, access, name, descriptor, signature, exceptions, methodVisitor, ignoreFields, collectionFields);
    }

    @Override
    protected boolean isTrackedFieldWrite(FieldInsnNode putfield) {
        return !ignoreFields.contains(putfield.name);
    }

    @Override
    protected void pushDirtyTrackerRef(InsnList il) {
        il.add(new VarInsnNode(Opcodes.ALOAD, 0));
    }

    @Override
    protected String dirtyTrackerClass() {
        return owner;
    }
}
