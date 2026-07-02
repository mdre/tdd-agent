package net.dirtydetector.agent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

/**
 * Common instrumentation engine for {@link WriteAccessActivatorAdapter} (top-level / static
 * nested classes) and {@link WriteAccessActivatorInnerClassAdapter} (non-static inner classes).
 *
 * <h2>Why a MethodNode/Analyzer two-pass design</h2>
 * The previous implementation was a single-pass {@code AnalyzerAdapter} that kept a
 * {@code Map<stackPosition, fieldName>} updated ad-hoc on every GETFIELD/GETSTATIC. That map
 * had no way to know when a stack slot's value had been replaced by something unrelated to a
 * field (e.g. loading a local variable, or the result of a method call landing on the same
 * stack index a field used to occupy), so it could go stale and cause false positives -
 * e.g. mutating a purely local {@code ArrayList} was wrongly flagged as a dirty field write.
 *
 * <p>Here we instead buffer the whole method into a {@link MethodNode}, run a real
 * {@link Analyzer} with {@link FieldOriginInterpreter} to compute, for every instruction, the
 * exact provenance of every value on the stack, and only then walk the instructions to decide
 * where to insert the {@code setDirty}/{@code addModifiedField} calls. This tracks field
 * origin correctly across ASTORE/ALOAD, DUP/SWAP, CHECKCAST and branch merges.
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public abstract class AbstractWriteAccessActivatorAdapter extends MethodNode
        implements ITransparentDirtyDetectorDef, IJavaCollections {

    private final static Logger LOGGER = LogManager.getLogger(AbstractWriteAccessActivatorAdapter.class.getName());
    static {
        Configurator.setLevel(AbstractWriteAccessActivatorAdapter.class.getName(),
                            LogginProperties.WriteAccessActivatorAdapter);
    }

    protected final String owner;
    protected final List<String> ignoreFields;
    protected final List<String> collectionFields;
    private final MethodVisitor target;

    protected AbstractWriteAccessActivatorAdapter(int api,
                                                   String owner,
                                                   int access,
                                                   String name,
                                                   String descriptor,
                                                   String signature,
                                                   String[] exceptions,
                                                   MethodVisitor target,
                                                   List<String> ignoreFields,
                                                   List<String> collectionFields) {
        super(api, access, name, descriptor, signature, exceptions);
        this.owner = owner;
        this.target = target;
        this.ignoreFields = ignoreFields;
        this.collectionFields = collectionFields;
    }

    /** Whether a PUTFIELD as seen in the bytecode should be treated as a tracked field write. */
    protected abstract boolean isTrackedFieldWrite(FieldInsnNode putfield);

    /**
     * Emits the instructions that push a reference to the object that declares
     * SETDIRTY/ADDMODIFIEDFIELD: {@code this} for a top-level class, {@code this.this$0} for
     * a non-static inner class.
     */
    protected abstract void pushDirtyTrackerRef(InsnList il);

    /** The class that declares SETDIRTY/ADDMODIFIEDFIELD (used as the INVOKEVIRTUAL owner). */
    protected abstract String dirtyTrackerClass();

    @Override
    public void visitEnd() {
        super.visitEnd();
        try {
            instrument();
        } catch (AnalyzerException e) {
            // Preferimos fallar de forma ruidosa a instrumentar a ciegas y generar bytecode
            // inconsistente que recién explote (de forma mucho más confusa) en el ClassWriter
            // o en tiempo de ejecución.
            throw new IllegalStateException(
                    "TDD-Agent: no se pudo analizar el flujo de datos del método "
                            + name + desc + " de la clase " + owner, e);
        }
        accept(target);
    }

    private void instrument() throws AnalyzerException {
        Analyzer<BasicValue> analyzer = new Analyzer<>(new FieldOriginInterpreter(api));
        Frame<BasicValue>[] frames = analyzer.analyze(owner, this);

        Set<String> pendingCollectionFields = new HashSet<>();
        boolean activate = false;

        // Se toma una foto de la lista de instrucciones: instructions.insertBefore(...) muta
        // la lista real, pero no este array, por lo que es seguro seguir iterando sobre él.
        AbstractInsnNode[] insns = instructions.toArray();
        for (int i = 0; i < insns.length; i++) {
            AbstractInsnNode insn = insns[i];
            // El frame en la posición i representa el estado de la pila/variables locales
            // INMEDIATAMENTE ANTES de ejecutar esta instrucción (es null si es código muerto).
            Frame<BasicValue> frame = frames[i];

            if (insn.getOpcode() == Opcodes.PUTFIELD) {
                FieldInsnNode fin = (FieldInsnNode) insn;
                if (isTrackedFieldWrite(fin)) {
                    LOGGER.log(Level.TRACE, "Modificación detectada!! campo: {}", fin.name);
                    activate = true;
                    instructions.insertBefore(insn, addModifiedFieldInsns(fin.name));
                }
                continue;
            }

            if (frame != null && insn instanceof MethodInsnNode) {
                String field = collectionFieldMutated((MethodInsnNode) insn, frame);
                if (field != null) {
                    LOGGER.log(Level.TRACE, "Modificación de colección detectada! campo: {}", field);
                    pendingCollectionFields.add(field);
                    activate = true;
                }
                continue;
            }

            if (frame != null && insn instanceof InvokeDynamicInsnNode) {
                for (String field : collectionFieldsMutatedByLambda((InvokeDynamicInsnNode) insn, frame)) {
                    LOGGER.log(Level.TRACE, "Modificación de colección (lambda) detectada! campo: {}", field);
                    pendingCollectionFields.add(field);
                    activate = true;
                }
                continue;
            }

            if (insn instanceof LabelNode) {
                if (!pendingCollectionFields.isEmpty()) {
                    instructions.insertBefore(insn, addModifiedFieldsInsns(pendingCollectionFields));
                    pendingCollectionFields.clear();
                }
                continue;
            }

            if (insn.getOpcode() == Opcodes.GOTO) {
                if (activate && !pendingCollectionFields.isEmpty()) {
                    instructions.insertBefore(insn, addModifiedFieldsInsns(pendingCollectionFields));
                    pendingCollectionFields.clear();
                }
                continue;
            }

            if (activate && isReturnOrThrow(insn.getOpcode())) {
                if (!pendingCollectionFields.isEmpty()) {
                    instructions.insertBefore(insn, addModifiedFieldsInsns(pendingCollectionFields));
                    pendingCollectionFields.clear();
                }
                instructions.insertBefore(insn, setDirtyInsns());
            }
        }
    }

    /** @return the tracked field name mutated by this collection method call, or null. */
    private String collectionFieldMutated(MethodInsnNode min, Frame<BasicValue> frame) {
        if (!getJavaCollections().contains("L" + min.owner + ";")
                || !getJavaCollectionsDirtyMethods().contains(min.name)) {
            return null;
        }
        int argCount = Type.getArgumentTypes(min.desc).length;
        int receiverIdx = frame.getStackSize() - 1 - argCount;
        if (receiverIdx < 0) {
            return null;
        }
        String field = FieldOriginInterpreter.fieldNameOf(frame.getStack(receiverIdx));
        return (field != null && collectionFields.contains(field)) ? field : null;
    }

    /** @return the tracked field names mutated by a collection method invoked via a method-reference/lambda. */
    private Set<String> collectionFieldsMutatedByLambda(InvokeDynamicInsnNode idin, Frame<BasicValue> frame) {
        Set<String> found = new HashSet<>();
        int receiverIdx = frame.getStackSize() - 1;
        if (receiverIdx < 0) {
            return found;
        }
        String field = FieldOriginInterpreter.fieldNameOf(frame.getStack(receiverIdx));
        if (field == null || !collectionFields.contains(field)) {
            return found;
        }
        for (Object bsmArg : idin.bsmArgs) {
            if (!(bsmArg instanceof Handle)) {
                continue;
            }
            Handle h = (Handle) bsmArg;
            if (getJavaCollections().contains("L" + h.getOwner() + ";")
                    && getJavaCollectionsDirtyMethods().contains(h.getName())) {
                found.add(field);
            }
        }
        return found;
    }

    private static boolean isReturnOrThrow(int opcode) {
        return (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW;
    }

    private InsnList addModifiedFieldInsns(String fieldName) {
        InsnList il = new InsnList();
        pushDirtyTrackerRef(il);
        il.add(new LdcInsnNode(fieldName));
        il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, dirtyTrackerClass(), ADDMODIFIEDFIELD, "(Ljava/lang/String;)V", false));
        return il;
    }

    private InsnList addModifiedFieldsInsns(Set<String> fieldNames) {
        InsnList il = new InsnList();
        for (String fieldName : fieldNames) {
            il.add(addModifiedFieldInsns(fieldName));
        }
        return il;
    }

    private InsnList setDirtyInsns() {
        InsnList il = new InsnList();
        pushDirtyTrackerRef(il);
        il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, dirtyTrackerClass(), SETDIRTY, "()V", false));
        return il;
    }
}
