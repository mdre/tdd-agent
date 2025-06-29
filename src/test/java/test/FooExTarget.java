/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.util.HashSet;
import java.util.Set;
import net.dirtydetector.agent.ITransparentDirtyDetector;
import net.odbogm.annotations.Entity;

/**
 *
 * @author mdre
 */
@Entity
public class FooExTarget extends FooTarget implements ITransparentDirtyDetector {
    private String s2;

    public boolean ___tdd___dirtyMark;
    public Set<String> ___tdd___modifiedFields = new HashSet<>();
    
    public FooExTarget(String s) {
        super(s);
    }

    public String getS2() {
        return s2;
    }

    public FooTarget setS2(String s) {
        this.s2 = s;
        this.___tdd___addModifiedField("s");
        this.___tdd___setDirty();
        return this;
    }
    
    @Override
    public boolean ___tdd___isDirty() {
        return ___tdd___dirtyMark;
    }

    @Override
    public void ___tdd___setDirty(){
        this.___tdd___dirtyMark = true;
    }

    @Override
    public void ___tdd___clearDirty() {
        this.___tdd___dirtyMark = false;
        this.___tdd___modifiedFields.clear();
    }
    
    @Override
    public Set<String> ___tdd___getModifiedFields() {
        return this.___tdd___modifiedFields;
    }
    
    @Override
    public void ___tdd___addModifiedField(String f) {
        this.___tdd___modifiedFields.add(f);
    }
}
