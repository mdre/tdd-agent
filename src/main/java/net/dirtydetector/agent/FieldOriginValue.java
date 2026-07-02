package net.dirtydetector.agent;

import java.util.Objects;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.BasicValue;

/**
 * A {@link BasicValue} that additionally remembers which field (if any) this
 * value originally came from.
 *
 * <p>This is what allows {@link FieldOriginInterpreter} (and therefore
 * {@link AbstractWriteAccessActivatorAdapter}) to tell apart:
 * <pre>
 *   this.fieldAL.add(x);           // field -> tagged "fieldAL"
 *   List local = new ArrayList();
 *   local.add(x);                  // no field -> untagged (null)
 *   List local2 = this.fieldAL;
 *   local2.add(x);                 // still "fieldAL": the tag survives ASTORE/ALOAD,
 *                                   // DUP/SWAP and CHECKCAST, because those operations
 *                                   // don't change *which object* is being referred to.
 * </pre>
 *
 * Any other operation (arithmetic, a method call, a new object, etc.) produces an
 * untagged value, since the result is no longer "the field itself".
 *
 * <p>Not every value on the stack during analysis will be a {@code FieldOriginValue}
 * (e.g. constants such as {@code ICONST_1} are returned by {@link
 * org.objectweb.asm.tree.analysis.BasicInterpreter} as shared singleton
 * {@code BasicValue} instances). Code reading the tag must therefore always go
 * through {@link FieldOriginInterpreter#fieldNameOf(BasicValue)} rather than casting
 * directly.
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class FieldOriginValue extends BasicValue {

    private final String fieldName;

    public FieldOriginValue(Type type, String fieldName) {
        super(type);
        this.fieldName = fieldName;
    }

    /** @return the name of the field this value originated from, or {@code null} if none/unknown. */
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BasicValue) || !super.equals(o)) {
            return false;
        }
        String otherFieldName = (o instanceof FieldOriginValue) ? ((FieldOriginValue) o).fieldName : null;
        return Objects.equals(this.fieldName, otherFieldName);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + Objects.hashCode(fieldName);
    }

    @Override
    public String toString() {
        return super.toString() + (fieldName == null ? "" : "{" + fieldName + "}");
    }
}
