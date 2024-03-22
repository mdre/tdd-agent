package net.dirtydetector.agent;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public interface ITransparentDirtyDetectorDef {
    public final String DIRTYMARK = "___tdd___dirtyMark";
    public final String MODIFIEDFIELDS = "___tdd___modifiedFields";
    public final String GETMODIFIEDFIELDS = "___tdd___getModifiedFields";
    public final String ISDIRTY = "___tdd___isDirty";
    public final String SETDIRTY = "___tdd___setDirty";
    public final String CLEARDIRTY = "___tdd___clearDirty";
}
