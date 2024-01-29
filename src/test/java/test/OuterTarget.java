package test;

import java.util.stream.Stream;
import net.dirtydetector.agent.ITransparentDirtyDetector;
import net.odbogm.annotations.Entity;

/**
 *
 * @author jbertinetti
 */
@Entity
public class OuterTarget implements ITransparentDirtyDetector {
    
    private String member;
    
    public String publicMember;

    public boolean ___ogm___dirtyMark;

    public OuterTarget(String member) {
        this.member = member;
        this.___ogm___setDirty(true);
    }


    public void setMember(String member) {
        this.member = member;
        this.___ogm___setDirty(true);
    }


    public String getMember() {
        return member;
    }
    
    
    public void anon() {
        new Runnable() {
            @Override
            public void run() {
                member = "run";
                ___ogm___setDirty(true);
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


    public class Inner {
        public void setOuterMember(String member) {
            OuterTarget.this.member = member;
            ___ogm___setDirty(true);
        }
    }
    
    
    public final void finalMethod() {
        this.member = "final";
    }
    
    @Override
    public boolean ___ogm___isDirty() {
        return ___ogm___dirtyMark;
    }

    @Override
    public void ___ogm___setDirty(boolean b) {
        this.___ogm___dirtyMark = b;
    }
    
}
