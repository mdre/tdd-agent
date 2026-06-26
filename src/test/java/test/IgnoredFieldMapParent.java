package test;

import java.util.HashMap;
import java.util.Map;
import net.dirtydetector.agent.TDDIgnore;
import net.odbogm.annotations.Entity;

@Entity
public abstract class IgnoredFieldMapParent {

    private final Map<String, Integer> values = new HashMap<>();

    @TDDIgnore
    private int ignoredValue;

    public final IgnoredFieldMapParent putValue(String key, int value) {
        values.put(key, value);
        return this;
    }

    public final Map<String, Integer> getValues() {
        return new HashMap<>(values);
    }

    public final void setIgnoredValue(int ignoredValue) {
        this.ignoredValue = ignoredValue;
    }

    public final int getIgnoredValue() {
        return ignoredValue;
    }
}
