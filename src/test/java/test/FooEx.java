/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import net.odbogm.annotations.Entity;

/**
 *
 * @author mdre
 */
@Entity
public class FooEx extends Foo {

    private String s2;

    public FooEx(String s) {
        super(s);
    }
    
    public String getS2() {
        return s2;
    }

    public void setS2(String s2) {
        this.s2 = s2;
    }
}
