package test;

import net.odbogm.annotations.Entity;

@Entity
public class IgnoredFieldMapChild extends IgnoredFieldMapParent {

    private String marker;

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public String getMarker() {
        return marker;
    }
}
