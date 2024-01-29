/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.odbogm.annotations.Entity;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
@Entity
public class ExAbsClass extends AbstractClass {
    private final static Logger LOGGER = Logger.getLogger(ExAbsClass.class .getName());
    static {
        if (LOGGER.getLevel() == null) {
            LOGGER.setLevel(Level.INFO);
        }
    }

    private String s;
    private transient int iTransient;
    
    public ExAbsClass() {
        
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }
    
    public void setTransient() {
        iTransient = 1;
    }

    public void shouldBeDirty() {
        // esto no dería cambiar el estado
        this.iTransient = 1;
        // pero esto sí
        this.s = "trash";
    }
}
