package test;


import java.util.ArrayList;
import java.util.List;
import net.odbogm.annotations.Entity;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author mdre
 */
@Entity
public class ColTarget {
    private ArrayList<String> fieldAL = new ArrayList();

    public ColTarget() {
    }
    
    public void fieldModif() {
        fieldAL.add("modificado");
    }
    
    public void localAL() {
        ArrayList<String> localAL = new ArrayList();
        localAL.add("local");
        
        localAL.addAll(this.fieldAL);
    }
    
    public List<String> localAddAfterFieldRead() {
        ArrayList<String> indirect = new ArrayList<>();
        for (Object value : this.fieldAL) {
            indirect.add(String.valueOf(value));
        }
        return indirect;
    }
}
