package net.dirtydetector.agent;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import test.ColClass;
import test.CollectionsTest;
import test.ExAbsClass;
import test.FinalClass;
import test.Foo;
import test.FooEx;
import test.FooExEx;
import test.Outer;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class TransparentDirtyDetectorTest {
    
    public TransparentDirtyDetectorTest() {
        
    }
    
    @BeforeClass
    public static void setUpClass() {
        
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws InterruptedException {
        try {
            System.out.println("****************************");
            System.out.println("Inicializando los tests.....");
            System.out.println("****************************\n\n\n\n");
            TransparentDirtyDetectorAgent.initialize();
            TransparentDirtyDetectorAgent.get()
                    .addDetector("net.odbogm.annotations.Entity")
                    .addIgnore("org.junit") 
                    .enableDumpDebugDirectory("/tmp/1/asm")
                    // .setClassLevelLog(TransparentDirtyDetectorInstrumentator.class, Level.FINEST)
                    ;
            Thread.sleep(1000);
        } catch (TDDAgentInitializationException ex) {
            Logger.getLogger(TransparentDirtyDetectorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @After
    public void tearDown() {
    }

//
//    private void verifyAgentLoaded(String agentName) {
//    }
//
//    
//    @Test
//    public void verifyAgentLoaded() {
//        verifyAgentLoaded("tdd-agent");
//    }
//    
    
    /**
     * Test of initialize method, of class TransparentDirtyDetectorAgent.
     */
    @Test
    public void testObjects() {
        System.out.println("");
        System.out.println("testObjects() ---------------------------------------------------");
        System.out.println("");
        System.out.println("Detectors: "+TransparentDirtyDetectorAgent.get().getDetectors());
        
        ExAbsClass eac = new ExAbsClass();
        assertTrue(eac instanceof ITransparentDirtyDetector);
        
        System.out.println("validar clases con colecciones de interfaces");
        ColClass cc = new ColClass();
        assertTrue(cc instanceof ITransparentDirtyDetector);
        
        System.out.println("\n\n\n\ninstanciando un FINAL");
        FinalClass fc = new FinalClass();
        
        Class cfc = fc.getClass();
        System.out.println("Modifiers : "+(Modifier.isFinal(cfc.getModifiers())));
        System.out.println("Interfaces:");
        for (Class ic : cfc.getInterfaces()) {
            System.out.println("::"+ic.getName());
        }
        assertTrue((Object)fc instanceof ITransparentDirtyDetector);
    }
    
    @Test
    public void transientFieldTest() throws Exception {
        System.out.println("\n\n\n\n\n");
        System.out.println("Transient field Test");
        System.out.println("\n\n\n\n\n");

        ExAbsClass eac = new ExAbsClass();
        System.out.println("verificando que sea instancia de ITransparentDirtyDetector");
        assertTrue(eac instanceof ITransparentDirtyDetector);
        ITransparentDirtyDetector itddEac = (ITransparentDirtyDetector) eac;
        System.out.println("verificando que no esté sucio");
        assertTrue( !itddEac.___tdd___isDirty());
        System.out.println("perturbar el campo transiet y verificando que no pase a dirty");
        eac.setTransient();
        assertTrue( !itddEac.___tdd___isDirty());
        System.out.println("verificar que los campos comunes aún funcionan.");
        eac.shouldBeDirty();
        assertTrue( itddEac.___tdd___isDirty());
        System.out.println(""+itddEac.___tdd___getModifiedFields());

    }
    
    @Test
    public void innerClass() throws Exception {
        System.out.println("");
        System.out.println("innerClass() ---------------------------------------------------");
        System.out.println("");
        
        Outer outer = new Outer("test");
        assertTrue(outer instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___tdd___isDirty());
        
        Outer.Inner inner = outer.new Inner();
        assertFalse(inner instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___tdd___isDirty());
        
        inner.touch();
        assertTrue(inner.isTouched());
        assertFalse(((ITransparentDirtyDetector)outer).___tdd___isDirty());
        
        inner.setOuterMember("modified");
        assertEquals("modified", outer.getMember());
        assertTrue(((ITransparentDirtyDetector)outer).___tdd___isDirty());
    }
    
    
    @Test
    public void anonClass() throws Exception {
        System.out.println("");
        System.out.println("anonClass() ---------------------------------------------------");
        System.out.println("");
        
        System.out.println("1");
        Outer outer = new Outer("test");
        assertTrue(outer instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___tdd___isDirty());
        
        System.out.println("2");
        outer.anon();
        assertEquals("run", outer.getMember());
        assertTrue(((ITransparentDirtyDetector)outer).___tdd___isDirty());
        
        System.out.println("fin anonClass() ---------------------------------------------------");
    }
    
    
    @Test
    public void lambda() throws Exception {
        System.out.println("");
        System.out.println("lambda() ---------------------------------------------------");
        System.out.println("");
        
        Outer outer = new Outer("test");
        assertTrue(outer instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___tdd___isDirty());
        
        outer.lambda();
        assertEquals("touched", outer.getMember());
        assertTrue(((ITransparentDirtyDetector)outer).___tdd___isDirty());
    }
    
    
   @Test
    public void lambda2() throws Exception {
        System.out.println("");
        System.out.println("lambda2() ---------------------------------------------------");
        System.out.println("");
        
        Outer outer = new Outer("test");
        assertTrue(outer instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___tdd___isDirty());
        
        outer.lambda2();
        assertEquals("lambda2", outer.getMember());
        assertTrue(((ITransparentDirtyDetector)outer).___tdd___isDirty());
    }
    
    
    @Test
    public void otherThread() throws Exception {
        System.out.println("");
        System.out.println("ohterThread() ---------------------------------------------------");
        System.out.println("");
        
        Outer outer = new Outer("test");
        assertTrue(outer instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___tdd___isDirty());
        
        outer.threaded();
        assertEquals("from thread", outer.getMember());
        assertTrue(((ITransparentDirtyDetector)outer).___tdd___isDirty());
    }
    
    
    @Test
    public void publicMembers() throws Exception {
        System.out.println("");
        System.out.println("publicMember() ---------------------------------------------------");
        System.out.println("");
        
        Outer outer = new Outer("test");
        assertTrue(outer instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___tdd___isDirty());
        
        //las modificaciones de este tipo no son detectadas por el agente
        outer.publicMember = "editeddd";
        System.out.println("Public member, dirty? " + 
                ((ITransparentDirtyDetector)outer).___tdd___isDirty());
    }
    
    
    @Test
    public void finalClass() throws Exception {
        System.out.println("");
        System.out.println("finalClass() ---------------------------------------------------");
        System.out.println("");
        
        FinalClass fc = new FinalClass();
        Object fco = (Object)fc;
        assertTrue(fco instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)fco).___tdd___isDirty());
        
        fc.setData("change");
        assertTrue(((ITransparentDirtyDetector)fco).___tdd___isDirty());
        assertEquals("change", fc.getData());
        assertFalse(Modifier.isFinal(FinalClass.class.getModifiers()));
    }
    
    
    @Test
    public void finalMethods() throws Exception {
        System.out.println("");
        System.out.println("finalMethods() ---------------------------------------------------");
        System.out.println("");
        
        Outer outer = new Outer("test");
        assertTrue(outer instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___tdd___isDirty());
        
        outer.finalMethod();
        assertEquals("final", outer.getMember());
        assertTrue(((ITransparentDirtyDetector)outer).___tdd___isDirty());
        
        Method m = outer.getClass().getDeclaredMethod("finalMethod");
        assertNotNull(m);
        assertFalse(Modifier.isFinal(m.getModifiers()));
    }
    
    @Test
    public void collectionTest() throws Exception {
        CollectionsTest ct = new CollectionsTest();
        ct.addTest();
        
        System.out.println("Modificados: "+ ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields());
        assertEquals(((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().size(), 3);
        System.out.println("isDirty: "+ ((ITransparentDirtyDetector)ct).___tdd___isDirty());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___isDirty());
        
        // quitar la marca de dirty
        ((ITransparentDirtyDetector)ct).___tdd___clearDirty();
        assertFalse(((ITransparentDirtyDetector)ct).___tdd___isDirty());
        
        System.out.println("\nCaso 1");
        ct.caso1();
        System.out.println("Modificados: "+ ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields());
        assertEquals(((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().size(), 1);
        System.out.println("isDirty: "+ ((ITransparentDirtyDetector)ct).___tdd___isDirty());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___isDirty());
        // quitar la marca de dirty
        ((ITransparentDirtyDetector)ct).___tdd___clearDirty();
        
        System.out.println("\nCaso 2");
        ct.caso2();
        System.out.println("Modificados: "+ ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields());
        assertEquals(1, ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().size());
        System.out.println("isDirty: "+ ((ITransparentDirtyDetector)ct).___tdd___isDirty());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___isDirty());
        // quitar la marca de dirty
        ((ITransparentDirtyDetector)ct).___tdd___clearDirty();
        
        System.out.println("\nCaso 3");
        ct.caso3();
        System.out.println("Modificados: "+ ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields());
        assertEquals(1, ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().size());
        System.out.println("isDirty: "+ ((ITransparentDirtyDetector)ct).___tdd___isDirty());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___isDirty());
        // quitar la marca de dirty
        ((ITransparentDirtyDetector)ct).___tdd___clearDirty();
        
        System.out.println("\nCaso 4");
        ct.caso4();
        System.out.println("Modificados: "+ ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields());
        assertEquals(1, ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().size());
        System.out.println("isDirty: "+ ((ITransparentDirtyDetector)ct).___tdd___isDirty());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___isDirty());
        // quitar la marca de dirty
        ((ITransparentDirtyDetector)ct).___tdd___clearDirty();
        
        System.out.println("\nCaso 5");
        ct.caso5();
        System.out.println("Modificados: "+ ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields());
        assertEquals(1, ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().size());
        System.out.println("isDirty: "+ ((ITransparentDirtyDetector)ct).___tdd___isDirty());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___isDirty());
        // quitar la marca de dirty
        ((ITransparentDirtyDetector)ct).___tdd___clearDirty();
        
        System.out.println("\nCaso 6");
        ct.caso6();
        System.out.println("Modificados: "+ ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields());
        assertEquals(1, ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().size());
        System.out.println("isDirty: "+ ((ITransparentDirtyDetector)ct).___tdd___isDirty());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___isDirty());
        // quitar la marca de dirty
        ((ITransparentDirtyDetector)ct).___tdd___clearDirty();
        
        System.out.println("\nCaso 7");
        ct.caso7("123");
        System.out.println("Modificados: "+ ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().contains("lista"));
        assertEquals(1, ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().size());
        System.out.println("isDirty: "+ ((ITransparentDirtyDetector)ct).___tdd___isDirty());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___isDirty());
        // quitar la marca de dirty
        ((ITransparentDirtyDetector)ct).___tdd___clearDirty();
        
        ct.caso7("456");
        System.out.println("Modificados: "+ ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().contains("hmap"));
        assertEquals(1, ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().size());
        System.out.println("isDirty: "+ ((ITransparentDirtyDetector)ct).___tdd___isDirty());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___isDirty());
        // quitar la marca de dirty
        ((ITransparentDirtyDetector)ct).___tdd___clearDirty();
        
        System.out.println("\nCaso 8");
        ct.caso8(10);
        System.out.println("Modificados: "+ ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().contains("lista"));
        assertEquals(1, ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().size());
        System.out.println("isDirty: "+ ((ITransparentDirtyDetector)ct).___tdd___isDirty());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___isDirty());
        // quitar la marca de dirty
        ((ITransparentDirtyDetector)ct).___tdd___clearDirty();
        
        
        System.out.println("\nCaso 9");
        ct.caso9("lista");
        System.out.println("Modificados: "+ ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().contains("lista"));
        assertEquals(1, ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().size());
        System.out.println("isDirty: "+ ((ITransparentDirtyDetector)ct).___tdd___isDirty());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___isDirty());
        // quitar la marca de dirty
        ((ITransparentDirtyDetector)ct).___tdd___clearDirty();
        
        ct.caso9("hmap");
        System.out.println("Modificados: "+ ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().contains("hmap"));
        assertEquals(1, ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().size());
        System.out.println("isDirty: "+ ((ITransparentDirtyDetector)ct).___tdd___isDirty());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___isDirty());
        // quitar la marca de dirty
        ((ITransparentDirtyDetector)ct).___tdd___clearDirty();
        
        System.out.println("caso 10");
        ct.caso10();
        System.out.println("Modificados: "+ ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().contains("hmap"));
        assertEquals(17, ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().size());
        System.out.println("isDirty: "+ ((ITransparentDirtyDetector)ct).___tdd___isDirty());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___isDirty());
        // quitar la marca de dirty
        ((ITransparentDirtyDetector)ct).___tdd___clearDirty();
        
        
        
    }
    
    @Test
    public void collectionClearTest() throws Exception {
        CollectionsTest ct = new CollectionsTest();
        ct.caso11();
        System.out.println("Modificados: "+ ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().contains("list"));
        assertEquals(1, ((ITransparentDirtyDetector)ct).___tdd___getModifiedFields().size());
        System.out.println("isDirty: "+ ((ITransparentDirtyDetector)ct).___tdd___isDirty());
        assertTrue(((ITransparentDirtyDetector)ct).___tdd___isDirty());
        // quitar la marca de dirty
        ((ITransparentDirtyDetector)ct).___tdd___clearDirty();
        
    }
    
    @Test
    public void subClassTest() {
        Foo f = new Foo("s1");
        FooEx fx = new FooEx("s2");
        FooExEx fxx = new FooExEx("s3");
        System.out.println("Level 1");
        System.out.println("fx isDirty: "+ ((ITransparentDirtyDetector)fx).___tdd___isDirty());
        assertFalse(((ITransparentDirtyDetector)fx).___tdd___isDirty());
        System.out.println("\n\n\nModificar un campo");
        fx.setS2("s2");
        assertTrue(((ITransparentDirtyDetector)fx).___tdd___isDirty());
        System.out.println("isDirty: "+ ((ITransparentDirtyDetector)fx).___tdd___isDirty());
        System.out.println("Modificados: "+ ((ITransparentDirtyDetector)fx).___tdd___getModifiedFields());
        assertTrue(((ITransparentDirtyDetector)fx).___tdd___isDirty());
        assertTrue(((ITransparentDirtyDetector)fx).___tdd___getModifiedFields().contains("s2"));
        
        // modificar un campo del objeto superior
        System.out.println("\nLimpiar las marcas...");
        ((ITransparentDirtyDetector)fx).___tdd___clearDirty();
        System.out.println("isDirty: "+ ((ITransparentDirtyDetector)fx).___tdd___isDirty());
        System.out.println("Modificados: "+ ((ITransparentDirtyDetector)fx).___tdd___getModifiedFields());
        assertFalse(((ITransparentDirtyDetector)fx).___tdd___isDirty());
        assertEquals(0, ((ITransparentDirtyDetector)fx).___tdd___getModifiedFields().size());
        
        System.out.println("modificar en el objeto superior...");
        fx.setS("fx s");
        System.out.println("isDirty: "+ ((ITransparentDirtyDetector)fx).___tdd___isDirty());
        System.out.println("Modificados: "+ ((ITransparentDirtyDetector)fx).___tdd___getModifiedFields());
        assertTrue(((ITransparentDirtyDetector)fx).___tdd___isDirty());
        assertTrue(((ITransparentDirtyDetector)fx).___tdd___getModifiedFields().contains("s"));
        
        fx.setS2("fx s2");
        assertTrue(((ITransparentDirtyDetector)fx).___tdd___getModifiedFields().contains("s2"));
        
        System.out.println("Pruebas sobre Ex Ex...");
        System.out.println("\nLimpiar las marcas...");
        ((ITransparentDirtyDetector)fxx).___tdd___clearDirty();
        System.out.println("isDirty: "+ ((ITransparentDirtyDetector)fxx).___tdd___isDirty());
        System.out.println("Modificados: "+ ((ITransparentDirtyDetector)fxx).___tdd___getModifiedFields());
        assertFalse(((ITransparentDirtyDetector)fxx).___tdd___isDirty());
        assertEquals(0, ((ITransparentDirtyDetector)fxx).___tdd___getModifiedFields().size());

        fxx.setS("fxx s");
        System.out.println("Modificado s: "+ ((ITransparentDirtyDetector)fxx).___tdd___getModifiedFields());
        fxx.setS2("fxx s2");
        System.out.println("Modificado s2: "+ ((ITransparentDirtyDetector)fxx).___tdd___getModifiedFields());
        fxx.setS3("fxx s3");
        System.out.println("Modificado s3: "+ ((ITransparentDirtyDetector)fxx).___tdd___getModifiedFields());
        assertTrue(((ITransparentDirtyDetector)fxx).___tdd___isDirty());
        assertTrue(((ITransparentDirtyDetector)fxx).___tdd___getModifiedFields().contains("s"));
        assertTrue(((ITransparentDirtyDetector)fxx).___tdd___getModifiedFields().contains("s2"));
        assertTrue(((ITransparentDirtyDetector)fxx).___tdd___getModifiedFields().contains("s3"));
        
    }
    
    
//    @Test
//    public void enumTest() {
//        Enums v = new Enums();
//        v.enums.addAll(List.of(EnumTest.UNO, EnumTest.DOS, EnumTest.OTRO_MAS));
//        System.out.println("Modificados: "+ ((ITransparentDirtyDetector)v).___tdd___getModifiedFields());
//        assertTrue(((ITransparentDirtyDetector)v).___tdd___isDirty());
//        
//        ((ITransparentDirtyDetector)v).___tdd___clearDirty();
//        
//        v.enums.add(EnumTest.TRES);
//        assertEquals(3, v.enums.size());
//        assertTrue(((ITransparentDirtyDetector)v).___tdd___isDirty());
//        
//        assertEquals(3, v.enums.size());
//        assertTrue(v.enums.contains(EnumTest.UNO));
//        assertTrue(v.enums.contains(EnumTest.DOS));
//        assertTrue(v.enums.contains(EnumTest.OTRO_MAS));
//        
//        System.out.println("\n\n\n\n Test remove:");
//        ((ITransparentDirtyDetector)v).___tdd___clearDirty();
//        
//        v.enums.remove(EnumTest.OTRO_MAS);
//        assertEquals(2, v.enums.size());
//        assertTrue(((ITransparentDirtyDetector)v).___tdd___isDirty());
//        
//        System.out.println("dirty: "+((ITransparentDirtyDetector)v).___tdd___isDirty());
//        System.out.println("modified fields: "+ String.join(", ",((ITransparentDirtyDetector)v).___tdd___getModifiedFields()));
//        
//        ((ITransparentDirtyDetector)v).___tdd___clearDirty();
//        v.enums.clear();
//        assertTrue(v.enums.isEmpty());
//    }
}
