package net.dirtydetector.agent;

import org.apache.logging.log4j.Level;

/**
 * Configuración de los loggers de cada clase.
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class LogginProperties {

    public static final Level TransparentDirtyDetectorAdapter            = Level.INFO;
    public static final Level TransparentDirtyDetectorInstrumentator     = Level.INFO;
    public static final Level TransparentDirtyDetectorAgent              = Level.INFO;
    public static final Level InstrumentableClassDetector                = Level.INFO;
    public static final Level WriteAccessActivatorAdapter                = Level.INFO;
    public static final Level WriteAccessActivatorInnerClassAdapter      = Level.INFO;
    public static final Level WriteConstructorAccessActivatorAdapter     = Level.INFO;
    
}
