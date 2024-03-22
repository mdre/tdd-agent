package net.dirtydetector.agent;

import java.util.logging.Level;

/**
 * Configuración de los loggers de cada clase.
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class LogginProperties {

    public static Level TransparentDirtyDetectorAdapter            = Level.FINEST;
    public static Level TransparentDirtyDetectorInstrumentator     = Level.FINEST;
    public static Level TransparentDirtyDetectorAgent              = Level.FINEST;
    public static Level InstrumentableClassDetector                = Level.FINEST;
    public static Level WriteAccessActivatorAdapter                = Level.FINEST;
    public static Level WriteAccessActivatorInnerClassAdapter      = Level.FINEST;
    public static Level WriteConstructorAccessActivatorAdapter     = Level.FINEST;
    
}
