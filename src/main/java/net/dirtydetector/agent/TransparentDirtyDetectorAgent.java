package net.dirtydetector.agent;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class TransparentDirtyDetectorAgent {

    private final static Logger LOGGER = Logger.getLogger(TransparentDirtyDetectorAgent.class.getName());

    static {
        if (LOGGER.getLevel() == null) {
            LOGGER.setLevel(LogginProperties.TransparentDirtyDetectorAgent);
        }
    }

    private static Instrumentation instrumentation;
    private static TransparentDirtyDetectorInstrumentator tddi;
    
    /**
     * Agente para manipulación de las clases.
     */
    public TransparentDirtyDetectorAgent() {
        // revisar el siguiente código para imitar la forma de carga del agente
        // https://jar-download.com/artifacts/net.bytebuddy/byte-buddy-agent/1.7.11/source-code/net/bytebuddy/agent/ByteBuddyAgent.java
        // método: installExternal
    }

    /**
     * Retrieve a reference todo the agent intrumentator. From that you can add
     * class detectos to the agent dinamically. 
     * 
     * @return TransparentDirtyDetectorInstrumentator
     */
    public static TransparentDirtyDetectorInstrumentator get() {
        return tddi;
    }
    /**
     * JVM hook to statically load the javaagent at startup.
     *
     * After the Java Virtual Machine (JVM) has initialized, the premain method will be called. Then the real application main method will be called.
     *
     * @param args args
     * @param inst inst throws Exception ex
     */
    public static void premain(String args, Instrumentation inst) {
        LOGGER.log(Level.INFO, "");
        LOGGER.log(Level.INFO, "===============================================");
        LOGGER.log(Level.INFO, "Transparent Dirty Detector Agent is loading... ");
        LOGGER.log(Level.INFO, "===============================================");
        LOGGER.log(Level.FINER, "premain method invoked with args: {0} and inst: {1}", new Object[]{args, inst});
        LOGGER.log(Level.INFO, "");
        instrumentation = inst;
        tddi = new TransparentDirtyDetectorInstrumentator(args);
        instrumentation.addTransformer(tddi);
        LOGGER.log(Level.INFO, "================= Agente cargado ==============");
    }

    /**
     * JVM hook to dynamically load javaagent at runtime.
     *
     * The agent class may have an agentmain method for use when the agent is started after VM startup.
     *
     * @param args args
     * @param inst inst throws Exception ex
     */
    public static void agentmain(String args, Instrumentation inst) {
        LOGGER.log(Level.INFO, "");
        LOGGER.log(Level.INFO, "===============================================");
        LOGGER.log(Level.INFO, "Transparent Dirty Detector Agent is loading... ");
        LOGGER.log(Level.INFO, "===============================================");
        LOGGER.log(Level.FINER, "agentmain method invoked with args: {0} and inst: {1}", new Object[]{args, inst});
        LOGGER.log(Level.INFO, "");
        instrumentation = inst;
        tddi = new TransparentDirtyDetectorInstrumentator(args);
        instrumentation.addTransformer(tddi);
        LOGGER.log(Level.INFO, "================= Agente cargado ==============");
        
    }

    /**
     * Programmatic hook to dynamically load javaagent at runtime. It could be load with JVM parameters. Ej:
     * -javaagent:/path-to-glassfish/domains/domain1/lib/ext/odbogm-agent-x.x.x.jar
     */
    public static void initialize() throws TDDAgentInitializationException {
        if (instrumentation == null) {
            try {
                LOGGER.log(Level.INFO, "Dynamically loading java agent...");
                String pathToAgent = TransparentDirtyDetectorAgent.class
                        .getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
                LOGGER.log(Level.INFO, "path: {0}", pathToAgent);
                if (pathToAgent.endsWith(".jar")) {
                    loadAgent(pathToAgent, null);
                } else {
                    loadAgentClass(TransparentDirtyDetectorAgent.class.getName(),
                            null, null, true, true, true);
                }
            } catch (URISyntaxException | AttachNotSupportedException | IOException |
                    AgentLoadException | AgentInitializationException ex) {
                Logger.getLogger(TransparentDirtyDetectorAgent.class.getName()).log(Level.SEVERE, null, ex);
                throw new TDDAgentInitializationException(ex);
            }
        }
    }

    private static void loadAgent(String agentJar, String options) throws AttachNotSupportedException, IOException, AgentLoadException, AgentInitializationException {
        long pid = ProcessHandle.current().pid();
        VirtualMachine vm = VirtualMachine.attach("" + pid);
        vm.loadAgent(agentJar, null);
    }
    
    /**
     * Creates loads the agent class directly. The agent class must be visible from the system class loader.
     * <p/>
     * This method creates a temporary jar with the proper manifest and loads the agent using the jvm attach facilities.
     * <p/>
     * This will not work if the agent class can't be loaded by the system class loader.
     * <br>
     * This can be worked around like by adding the specific class and any other dependencies to the system class loader:
     * <pre><code>
     *     if(MyAgent.class.getClassLoader() != ClassLoader.getSystemClassLoader()) {
     *         ClassPathUtils.appendToSystemPath(ClassPathUtils.getClassPathFor(MyAgent.class));
     *         ClassPathUtils.appendToSystemPath(ClassPathUtils.getClassPathFor(OtherDepenencies.class));
     *     }
     *     loadAgent(MyAgent.class.getName(), null, null, true, true, false);
     * </code></pre>
     *
     * @param agentClassName the agent class name
     * @param options        options that will be passed back to the agent, can be null
     */
    private static void loadAgentClass(String agentClassName, String options)
    {
        loadAgentClass(agentClassName, options, null, true, true, false);
    }


    /**
     * Creates loads the agent class directly.
     * <p/>
     * This method creates a temporary jar with the proper manifest and loads the agent using the jvm attach facilities.
     * <p/>
     * This will not work if the agent class can't be loaded by the system class loader.
     * <br>
     * This can be worked around like by adding the specific class and any other dependencies to the system class loader:
     * <pre><code>
     *     if(MyAgent.class.getClassLoader() != ClassLoader.getSystemClassLoader()) {
     *         ClassPathUtils.appendToSystemPath(ClassPathUtils.getClassPathFor(MyAgent.class));
     *         ClassPathUtils.appendToSystemPath(ClassPathUtils.getClassPathFor(OtherDepenencies.class));
     *     }
     *     loadAgent(MyAgent.class.getName(), null, null, true, true, false);
     * </code></pre>
     *
     * @param agentClass               the agent class
     * @param options                  options that will be passed back to the agent, can be null
     * @param bootClassPath            list of jars to be loaded with the agent, can be null
     * @param canRedefineClasses       if the ability to redefine classes is need by the agent, suggested default: false
     * @param canRetransformClasses    if the ability to retransform classes is need by the agent, suggested default: false
     * @param canSetNativeMethodPrefix if the ability to set native method prefix is need by the agent, suggested default: false
     * @see ClassPathUtils
     * @see java.lang.instrument.Instrumentation
     */
    private static void loadAgentClass(
            final String agentClass,
            final String options,
            final String bootClassPath,
            final boolean canRedefineClasses,
            final boolean canRetransformClasses,
            final boolean canSetNativeMethodPrefix)
    {
        try
        {
            final File jarFile;
            try
            {
                jarFile = createTemporaryAgentJar(agentClass, bootClassPath, canRedefineClasses, canRetransformClasses, canSetNativeMethodPrefix);
            }
            catch (IOException ex)
            {
                throw new RuntimeException("Can't write jar file for agent:" + agentClass, ex);
            }
            
            loadAgent(jarFile.getPath(), options);
        }
        catch (AttachNotSupportedException | IOException | AgentLoadException | AgentInitializationException ex)
        {
            Logger.getLogger(TransparentDirtyDetectorAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Creates a jar in runtime with the proper manifest file to start the javaagent.
     * <p/>
     * This method is convenient to java agent developers since they can test their agents without creating a jar first.
     *
     * @param agentClass               the agent class
     * @param bootClassPath            list of jars to be loaded with the agent, can be null
     * @param canRedefineClasses       if the ability to redefine classes is need by the agent, suggested default: false
     * @param canRetransformClasses    if the ability to retransform classes is need by the agent, suggested default: false
     * @param canSetNativeMethodPrefix if the ability to set native method prefix is need by the agent, suggested default: false
     */
    private static File createTemporaryAgentJar(
            final String agentClass,
            final String bootClassPath,
            final boolean canRedefineClasses,
            final boolean canRetransformClasses,
            final boolean canSetNativeMethodPrefix) throws IOException
    {
        final File jarFile = File.createTempFile("javaagent." + agentClass, ".jar");
        jarFile.deleteOnExit();
        createAgentJar(new FileOutputStream(jarFile),
                agentClass,
                bootClassPath,
                canRedefineClasses,
                canRetransformClasses,
                canSetNativeMethodPrefix);
        return jarFile;
    }

    /**
     * Creates an agent jar with the proper manifest file to start a javaagent.
     *
     * @param agentClass               the agent class
     * @param bootClassPath            list of jars to be loaded with the agent, can be null
     * @param canRedefineClasses       if the ability to redefine classes is need by the agent, suggested default: false
     * @param canRetransformClasses    if the ability to retransform classes is need by the agent, suggested default: false
     * @param canSetNativeMethodPrefix if the ability to set native method prefix is need by the agent, suggested default: false
     */
    private static void createAgentJar(
            final OutputStream out,
            final String agentClass,
            final String bootClassPath,
            final boolean canRedefineClasses,
            final boolean canRetransformClasses,
            final boolean canSetNativeMethodPrefix) throws IOException
    {
        final Manifest man = new Manifest();
        man.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        man.getMainAttributes().putValue("Agent-Class", agentClass);
        if (bootClassPath != null)
        {
            man.getMainAttributes().putValue("Boot-Class-Path", bootClassPath);
        }
        man.getMainAttributes().putValue("Can-Redefine-Classes", Boolean.toString(canRedefineClasses));
        man.getMainAttributes().putValue("Can-Retransform-Classes", Boolean.toString(canRetransformClasses));
        man.getMainAttributes().putValue("Can-Set-Native-Method-Prefix", Boolean.toString(canSetNativeMethodPrefix));
        final JarOutputStream jarOut = new JarOutputStream(out, man);
        jarOut.flush();
        jarOut.close();
    }

    
}
