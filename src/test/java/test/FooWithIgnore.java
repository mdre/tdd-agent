/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import net.dirtydetector.agent.TDDIgnore;
import net.odbogm.annotations.Entity;

/**
 *
 * @author mdre
 */
@Entity
public class FooWithIgnore {
    private String s;

    @TDDIgnore
    private int ignoreThis = 0;
    
    public FooWithIgnore(String s) {
        this.s = s;
    }

    
    public FooWithIgnore setS(String s) {
        this.s = s;
        return this;
    }
    
    public String getS() {
        return s;
    }

    public FooWithIgnore setIgnore(int i) {
        this.ignoreThis = i;
        return this;
    }
    
}
