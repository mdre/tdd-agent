# TransparentDirtyDrector-Agent
Transparent Dirty Drector Agent

This java agent force the implementation of the ITransparentDirtyDetector interface on-the-fly. I could be load at runtime or as a parameter of the JVM.

I have found that in certains case, when you use EJB, some classes are loaded before the aplication itself is initiated, so in that case is recomended to set the agent in the JVM parameters.

For example, to add it to Glassfish/Payara, just copy the tdd-agent-all-x.x.x.jar to the lib/ext dir of the domain and set in Configurations > server-config > JVM Settings > JVM Options an opetion with:
  
-javaagent:/opt/payara/glassfish/domains/domain1/lib/ext/tdd-agent-all-x.x.x.jar

and restart the server.

To detect the classes, the agent must be loaded with the full path of the target annotations. The agent instrument all method of the classes that are annotated. It add a few method to catch when the internal state of an instance change.

For example, to load dinamically the agent from the app and instrument all classes that are annotated with "net.odbogm.annotations.Entity":

```Java
try {
    TransparentDirtyDetectorAgent.initialize();
    TransparentDirtyDetectorAgent.get().addDetector("net.odbogm.annotations.Entity") ;

} catch (TDDAgentInitializationException ex) {
    Logger.getLogger(TransparentDirtyDetectorTest.class.getName()).log(Level.SEVERE, null, ex);
}
```

If the agent was loaded from command line, you can set the class to detect as a parameter:
```
-javaagent:/opt/payara/glassfish/domains/domain1/lib/ext/tdd-agent-all-x.x.x.jar=net.odbogm.annotations.Entity
```


