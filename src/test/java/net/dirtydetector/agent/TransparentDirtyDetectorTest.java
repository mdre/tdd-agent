package net.dirtydetector.agent;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import test.ColClass;
import test.ExAbsClass;
import test.FinalClass;
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
    public void setUp() {
        try {
            TransparentDirtyDetectorAgent.initialize();
            TransparentDirtyDetectorAgent.get().addDetector("net.odbogm.annotations.Entity") ;
            
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
        assertTrue( !itddEac.___ogm___isDirty());
        System.out.println("perturbar el campo transiet y verificando que no pase a dirty");
        eac.setTransient();
        assertTrue( !itddEac.___ogm___isDirty());
        System.out.println("verificar que los campos comunes aún funcionan.");
        eac.shouldBeDirty();
        assertTrue( itddEac.___ogm___isDirty());

    }
    
    @Test
    public void innerClass() throws Exception {
        System.out.println("");
        System.out.println("innerClass() ---------------------------------------------------");
        System.out.println("");
        
        Outer outer = new Outer("test");
        assertTrue(outer instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___ogm___isDirty());
        
        Outer.Inner inner = outer.new Inner();
        assertFalse(inner instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___ogm___isDirty());
        
        inner.touch();
        assertTrue(inner.isTouched());
        assertFalse(((ITransparentDirtyDetector)outer).___ogm___isDirty());
        
        inner.setOuterMember("modified");
        assertEquals("modified", outer.getMember());
        assertTrue(((ITransparentDirtyDetector)outer).___ogm___isDirty());
    }
    
    
    @Test
    public void anonClass() throws Exception {
        System.out.println("");
        System.out.println("anonClass() ---------------------------------------------------");
        System.out.println("");
        
        System.out.println("1");
        Outer outer = new Outer("test");
        assertTrue(outer instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___ogm___isDirty());
        
        System.out.println("2");
        outer.anon();
        assertEquals("run", outer.getMember());
        assertTrue(((ITransparentDirtyDetector)outer).___ogm___isDirty());
        
        System.out.println("fin anonClass() ---------------------------------------------------");
    }
    
    
    @Test
    public void lambda() throws Exception {
        System.out.println("");
        System.out.println("lambda() ---------------------------------------------------");
        System.out.println("");
        
        Outer outer = new Outer("test");
        assertTrue(outer instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___ogm___isDirty());
        
        outer.lambda();
        assertEquals("touched", outer.getMember());
        assertTrue(((ITransparentDirtyDetector)outer).___ogm___isDirty());
    }
    
    
   @Test
    public void lambda2() throws Exception {
        System.out.println("");
        System.out.println("lambda2() ---------------------------------------------------");
        System.out.println("");
        
        Outer outer = new Outer("test");
        assertTrue(outer instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___ogm___isDirty());
        
        outer.lambda2();
        assertEquals("lambda2", outer.getMember());
        assertTrue(((ITransparentDirtyDetector)outer).___ogm___isDirty());
    }
    
    
    @Test
    public void otherThread() throws Exception {
        System.out.println("");
        System.out.println("ohterThread() ---------------------------------------------------");
        System.out.println("");
        
        Outer outer = new Outer("test");
        assertTrue(outer instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___ogm___isDirty());
        
        outer.threaded();
        assertEquals("from thread", outer.getMember());
        assertTrue(((ITransparentDirtyDetector)outer).___ogm___isDirty());
    }
    
    
    @Test
    public void publicMembers() throws Exception {
        System.out.println("");
        System.out.println("publicMember() ---------------------------------------------------");
        System.out.println("");
        
        Outer outer = new Outer("test");
        assertTrue(outer instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___ogm___isDirty());
        
        //las modificaciones de este tipo no son detectadas por el agente
        outer.publicMember = "editeddd";
        System.out.println("Public member, dirty? " + 
                ((ITransparentDirtyDetector)outer).___ogm___isDirty());
    }
    
    
    @Test
    public void finalClass() throws Exception {
        System.out.println("");
        System.out.println("finalClass() ---------------------------------------------------");
        System.out.println("");
        
        FinalClass fc = new FinalClass();
        Object fco = (Object)fc;
        assertTrue(fco instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)fco).___ogm___isDirty());
        
        fc.setData("change");
        assertTrue(((ITransparentDirtyDetector)fco).___ogm___isDirty());
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
        assertFalse(((ITransparentDirtyDetector)outer).___ogm___isDirty());
        
        outer.finalMethod();
        assertEquals("final", outer.getMember());
        assertTrue(((ITransparentDirtyDetector)outer).___ogm___isDirty());
        
        Method m = outer.getClass().getDeclaredMethod("finalMethod");
        assertNotNull(m);
        assertFalse(Modifier.isFinal(m.getModifiers()));
    }
    
}
