
package test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.odbogm.annotations.Entity;

/**
 * Clases con colecciones de interfaces. 
 * 
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
@Entity
public class ColClass {
    private final static Logger LOGGER = Logger.getLogger(ColClass.class .getName());
    static {
        if (LOGGER.getLevel() == null) {
            LOGGER.setLevel(Level.INFO);
        }
    }

    List<InterfaceTest> ilist;
    FinalClass fc;
    public ColClass() {
        
    }
    
    public void addItem(InterfaceTest it) {
        if (this.ilist == null) {
            this.ilist = new ArrayList<>();
        }
        this.ilist.add(it);
    }
}
