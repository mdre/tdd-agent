package net.dirtydetector.agent;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

/**
 * An {@link org.objectweb.asm.tree.analysis.Interpreter} that behaves exactly like
 * {@link BasicInterpreter} (it delegates all type/size computation to it) but additionally
 * tags every value that represents "the current contents of field X" with the name of
 * that field, using {@link FieldOriginValue}.
 *
 * <p>Replaces the ad-hoc {@code Map<stackPosition, fieldName>} that used to live in
 * {@code WriteAccessActivatorAdapter}. That map went stale the moment any instruction
 * other than GETFIELD/GETSTATIC pushed a value into a stack slot that used to hold a
 * field reference (e.g. loading a local variable), producing false positives such as
 * marking the object dirty when only a method-local collection was mutated.
 *
 * <p>Using a real dataflow {@link org.objectweb.asm.tree.analysis.Analyzer} instead means
 * every value is tracked individually as it flows through the method (including across
 * ASTORE/ALOAD, DUP, SWAP, CHECKCAST and control-flow merges), so the tag is only ever
 * present when the value genuinely still refers to that field's original contents.
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class FieldOriginInterpreter extends BasicInterpreter {

    public FieldOriginInterpreter(int api) {
        super(api);
    }

    /**
     * Safely reads the field-origin tag of a value that may or may not be a
     * {@link FieldOriginValue} (constants produced by {@link BasicInterpreter} are plain
     * {@link BasicValue} singletons, never tagged).
     */
    public static String fieldNameOf(BasicValue value) {
        return (value instanceof FieldOriginValue) ? ((FieldOriginValue) value).getFieldName() : null;
    }

    @Override
    public BasicValue newOperation(final AbstractInsnNode insn) throws AnalyzerException {
        BasicValue value = super.newOperation(insn);
        // GETSTATIC: 0 stack values in, 1 out -> reported via newOperation.
        if (insn.getOpcode() == Opcodes.GETSTATIC) {
            return new FieldOriginValue(value.getType(), ((FieldInsnNode) insn).name);
        }
        return value;
    }

    @Override
    public BasicValue unaryOperation(final AbstractInsnNode insn, final BasicValue value) throws AnalyzerException {
        BasicValue result = super.unaryOperation(insn, value);
        switch (insn.getOpcode()) {
            case Opcodes.GETFIELD:
                // objectref -> value : 1 in, 1 out -> reported via unaryOperation.
                return new FieldOriginValue(result.getType(), ((FieldInsnNode) insn).name);
            case Opcodes.CHECKCAST: {
                // A cast doesn't change object identity: keep propagating the origin, if any.
                String field = fieldNameOf(value);
                return field == null ? result : new FieldOriginValue(result.getType(), field);
            }
            default:
                // Any other transformation (arithmetic negation, ARRAYLENGTH, INSTANCEOF,
                // NEWARRAY/ANEWARRAY, etc.) produces a brand-new value: no tag.
                return result;
        }
    }

    @Override
    public BasicValue copyOperation(final AbstractInsnNode insn, final BasicValue value) throws AnalyzerException {
        // Covers *LOAD, *STORE, DUP/DUP2/DUP_X1/DUP_X2/DUP2_X1/DUP2_X2 and SWAP: the value
        // itself doesn't change, it's just moved/duplicated, so the tag must survive.
        BasicValue result = super.copyOperation(insn, value);
        String field = fieldNameOf(value);
        return field == null ? result : new FieldOriginValue(result.getType(), field);
    }

    @Override
    public BasicValue merge(final BasicValue v, final BasicValue w) {
        BasicValue merged = super.merge(v, w);
        String f1 = fieldNameOf(v);
        String f2 = fieldNameOf(w);
        // Two control-flow paths agree the value still comes from the same field: keep the tag.
        // Otherwise (different fields, or one/both untagged) the merged value is untagged,
        // matching normal Java semantics: after `x = cond ? this.a : this.b;` you no longer
        // know statically which field `x` mirrors.
        if (f1 != null && f1.equals(f2)) {
            return new FieldOriginValue(merged.getType(), f1);
        }
        return merged;
    }
}
