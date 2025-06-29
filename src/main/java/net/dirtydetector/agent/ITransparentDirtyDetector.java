package net.dirtydetector.agent;

import java.util.Set;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public interface ITransparentDirtyDetector {
    public boolean ___tdd___isDirty();
    public void ___tdd___setDirty();
    public void ___tdd___clearDirty();
    public Set<String> ___tdd___getModifiedFields();
    public void ___tdd___addModifiedField(String f);
}
