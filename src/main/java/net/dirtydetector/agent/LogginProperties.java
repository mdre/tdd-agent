package net.dirtydetector.agent;

import java.util.logging.Level;

/**
 * Configuración de los loggers de cada clase.
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class LogginProperties {

    public static Level TransparentDirtyDetectorAdapter            = Level.INFO;
    public static Level TransparentDirtyDetectorInstrumentator     = Level.INFO;
    public static Level TransparentDirtyDetectorAgent              = Level.INFO;
    public static Level InstrumentableClassDetector                = Level.INFO;
    public static Level WriteAccessActivatorAdapter                = Level.INFO;
    public static Level WriteAccessActivatorInnerClassAdapter      = Level.INFO;
    
}
