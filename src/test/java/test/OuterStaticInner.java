package test;

import java.util.stream.Stream;
import net.odbogm.annotations.Entity;

/**
 *
 * @author jbertinetti
 */
@Entity
public class OuterStaticInner {
    
    private String member;
    
    public String publicMember;


    public OuterStaticInner(String member) {
        this.member = member;
    }


    public void setMember(String member) {
        this.member = member;
    }


    public String getMember() {
        return member;
    }
    
    
    public void anon() {
        new Runnable() {
            @Override
            public void run() {
                member = "run";
            }
        }.run();
    }
    
    
    public void lambda() {
        Stream.generate(() -> member = "touched").limit(1).count();
    }
    
    
    public void lambda2() {
        Runnable r = () -> member = "lambda2";
        r.run();
    }
    
    
    public void threaded() {
        new Thread(() -> member = "from thread").run();
    }
    

    public static class StaticInner {
        private boolean touched = false;
        public void touch() { 
            this.touched = true; 
        }
        public boolean isTouched() { 
            return touched; 
        }
    }
    
    public final void finalMethod() {
        this.member = "final";
        
    }
    
}
