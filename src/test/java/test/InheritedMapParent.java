package test;

import java.util.HashMap;
import java.util.Map;
import net.odbogm.annotations.Entity;

@Entity
public abstract class InheritedMapParent {

    private final Map<String, Integer> values = new HashMap<>();

    public final InheritedMapParent putValue(String key, int value) {
        values.put(key, value);
        return this;
    }

    public final Map<String, Integer> getValues() {
        return new HashMap<>(values);
    }
}
