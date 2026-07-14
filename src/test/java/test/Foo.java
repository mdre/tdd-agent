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
public class Foo {
    private String s;

    public Foo(String s) {
//        this.s = s;
        this.setS(s);
    }

    public String getS() {
        return s;
    }

    public Foo setS(String s) {
        this.s = s;
        return this;
    }
    
    
}
