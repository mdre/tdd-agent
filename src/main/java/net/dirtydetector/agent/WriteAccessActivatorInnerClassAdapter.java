package net.dirtydetector.agent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.util.Printer;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class WriteAccessActivatorInnerClassAdapter extends AnalyzerAdapter
        implements ITransparentDirtyDetectorDef, IJavaCollections {

    private final static Logger LOGGER = LogManager.getLogger(WriteAccessActivatorInnerClassAdapter.class.getName());

    static {
        Configurator.setLevel(WriteAccessActivatorInnerClassAdapter.class.getName(),
                              LogginProperties.WriteAccessActivatorInnerClassAdapter);
    }

    private boolean activate = false;
    private String className;
    private String outerClass;
    private List<String> ignoredFields;
    private List<String> collectionFields;

    HashSet<String> lastCollectionModifiedFields = new HashSet<>();

    // mapea la posición de la pila con el nombre del campo asociado
    private Map<String, String> stackToField = new HashMap<>();

    public WriteAccessActivatorInnerClassAdapter(
            int api,
            String className,
            int access,
            String name,
            String descriptor,
            MethodVisitor methodVisitor,
            List<String> ignoreFields,
            List<String> collectionsFields
    ) {
        super(api, className, access, name, descriptor, methodVisitor);
        this.className = className;
        this.ignoredFields = ignoreFields;
        this.collectionFields = collectionsFields;
        this.outerClass = className.substring(0, className.lastIndexOf("$"));
    }

    /**
     * Add a call to setDirty in every method that has a PUTFIELD in its code.
     *
     * @param opcode código a analizar
     */
    @Override
    public synchronized void visitInsn(int opcode) {
        LOGGER.log(Level.DEBUG, "Activate: {0}", this.activate);
        if ((this.activate) && ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW)) {
            // si hay colleciones agregadas, incluirlas como dirty antes de retornar. 
            if (lastCollectionModifiedFields.size() > 0) {
                insertDirtyCollectionsFields();
                lastCollectionModifiedFields.clear();
            }

            LOGGER.log(Level.DEBUG, "Agregando llamada a setDirty...");
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            //getfield test/OuterTarget$1.this$0:test.OuterTarget

            LOGGER.log(Level.DEBUG, "className: {0}, outerClass: {1}", new String[]{className, outerClass});
            mv.visitFieldInsn(Opcodes.GETFIELD, this.className, "this$0", "L" + this.outerClass + ";");

            //mv.visitInsn(Opcodes.ICONST_1);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, outerClass, SETDIRTY, "()V", false);
            //mv.visitFieldInsn(Opcodes.PUTFIELD, owner, "__ogm__dirtyMark", "Z");
        }
        mv.visitInsn(opcode);
        LOGGER.log(Level.TRACE, "fin --------------------------------------------------");
    }

    @Override
    public synchronized void visitFieldInsn(int opcode, String owner, String name, String desc) {
        LOGGER.log(Level.DEBUG, "owner: {0} - name: {1} - desc: {2} - opcode: {3}", new Object[]{owner, name, desc, opcode});
        //  owner: test/Outer$1 - name: this$0 - desc: Ltest/Outer;

        mv.visitFieldInsn(opcode, owner, name, desc);
        if ((opcode == Opcodes.GETFIELD) || (opcode == Opcodes.GETSTATIC)) {
            // si se está accediendo a un field, preservar el nombre para futuras referencias.
            this.stackToField.put("" + (this.stack == null ? 0 : (this.stack.size() - 1)), name);
//            this.owner = owner;
        }
        if (opcode == Opcodes.PUTFIELD && outerClass.equals(owner) && !ignoredFields.contains(name)) {
            this.activate = true;
            // registrar el campo
            insertDirtyField(name);
        }
        LOGGER.log(Level.TRACE, "fin --------------------------------------------------");
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        LOGGER.log(Level.TRACE, "opcode: {0} - owner: {1} - name: {2} - desc: {3} - isInterface: {4}", new Object[]{Printer.OPCODES[opcode], owner, name, descriptor, isInterface});
        printStack();
        // si el método coincide con una de las clases y métodos a monitorear, revisar el stack para verificar
        // que el campo sea un field.
        LOGGER.log(Level.TRACE, "activable object?: " + getJavaCollections().contains("L" + owner + ";")
                + " - method: " + name + "> activable? : " + getJavaCollectionsDirtyMethods().contains(name));
        if ((getJavaCollections().contains("L" + owner + ";")) && (getJavaCollectionsDirtyMethods().contains(name))) {
            // calcular la posición de la pila a acceder
            int stackOffset = descriptor.equals("()V") ? 0 : descriptor.substring(1, descriptor.indexOf(")"))
                    .split(";").length;
            int stackIdx = this.stack == null ? 0 : this.stack.size() - 1 - stackOffset;
            String field = this.stackToField.get("" + stackIdx);

            LOGGER.log(Level.TRACE, "modificación de una colección detectada! stack idx: " + stackIdx + " field: " + field);
            if (this.collectionFields.contains(field)) {
                lastCollectionModifiedFields.add(field);
                this.activate = true;
//                this.owner = owner;
            }
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments); 
        LOGGER.log(Level.TRACE, "\n\n\n\n\nname: "+name+" - desc: "+descriptor+"   bs: "+ Arrays.toString(bootstrapMethodArguments));
        printStack();
        
        for (Object bsMthArg : bootstrapMethodArguments) {
            String bsMth = bsMthArg.toString();
            int dot = bsMth.indexOf('.');
            int bracket = bsMth.indexOf("(");
            if (dot > 0 && bracket > 0) {
                String cls = "L"+bsMth.substring(0, dot)+";";
                String mth = bsMth.substring(dot+1, bracket);
                LOGGER.log(Level.TRACE, "cls: "+cls + "   -   method: "+mth);

                if (getJavaCollections().contains(cls) && getJavaCollectionsDirtyMethods().contains(mth)) {
                    int stackIdx = this.stack.size() - 1 ;
                    String field = this.stackToField.get(""+stackIdx);
                    LOGGER.log(Level.TRACE, "modificación de una colección detectada! stack idx: "+stackIdx+" field: "+field);
                    if (this.collectionFields.contains(field)) {
                        lastCollectionModifiedFields.add(field);
                        this.activate = true;
                    } 
                }
            }
        }
        
        LOGGER.log(Level.TRACE, "\n\n\n\n\n");
        
    }

    @Override
    public void visitLabel(Label label) {
        LOGGER.log(Level.TRACE, "Label: "+label);
        if (lastCollectionModifiedFields.size()>0){
            // si se ha agregado un collectionModifiedField, instrumentar add del campo
            LOGGER.log(Level.TRACE, "Modificaciones detectadas!! Agregar los campos a la lista.");
            printStack();
            insertDirtyCollectionsFields();
            
            // resetear el campo
            lastCollectionModifiedFields.clear();
            LOGGER.log(Level.TRACE, " --------------------------------------------------");
        }
        super.visitLabel(label); 
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        if (this.activate && opcode == Opcodes.GOTO) {
            // si hay colleciones agregadas, incluirlas como dirty antes de retornar. 
            if (lastCollectionModifiedFields.size()>0) {
                insertDirtyCollectionsFields();
                lastCollectionModifiedFields.clear();
            }
        } 
        super.visitJumpInsn(opcode, label); 
    }
    
    
    @Override
    public void visitEnd() {
        LOGGER.log(Level.TRACE, "fin MethodVisitor -------------------------------------");
//        mv.visitMaxs(0, 0);
        super.visitEnd();
    }

    //=================================================================================================
    private void printStack() {
        if (LOGGER.isEnabled(Level.TRACE)) {
            if (this.stack != null) {
                System.out.println("stack size:" + this.stack.size());

                for (int i = 0; i < this.stack.size(); i++) {
                    Object o = this.stack.get(i);
                    System.out.println("" + o.getClass() + " :  " + o + " --> " + this.stackToField.get("" + i));
                }
                System.out.println("--------------");
            } else {
                System.out.println("stack size: NULL <<<<<<<<<<<<<<<<<<<<<<< ");
            }
        }
    }

    /**
     * Insert all field registered in the lastCollectionFields hashset.
     */
    private void insertDirtyCollectionsFields() {
        for (String lastCollectionModifiedField : lastCollectionModifiedFields) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, className, "this$0", "L"+outerClass+";");
            mv.visitFieldInsn(Opcodes.GETFIELD, outerClass, MODIFIEDFIELDS, "Ljava/util/Set;");
            mv.visitLdcInsn(lastCollectionModifiedField);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Set", "add", "(Ljava/lang/Object;)Z", true);
            mv.visitInsn(Opcodes.POP); // Descartar el resultado booleano de add
        }
    }

    private void insertDirtyField(String name) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, className, "this$0", "L"+outerClass+";");
        mv.visitFieldInsn(Opcodes.GETFIELD, outerClass, MODIFIEDFIELDS, "Ljava/util/Set;");
        mv.visitLdcInsn(name);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Set", "add", "(Ljava/lang/Object;)Z", true);
        mv.visitInsn(Opcodes.POP); // Descartar el resultado booleano de add
    }
}
