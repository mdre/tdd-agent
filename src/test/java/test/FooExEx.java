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
public class FooExEx extends FooEx {

    private String s3;

    public FooExEx(String s) {
        super(s);
    }
    
    public String getS3() {
        return s3;
    }

    public void setS3(String s3) {
        this.s3 = s3;
    }
    
}
