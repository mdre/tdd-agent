package test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.odbogm.annotations.Entity;

/**
 *
 * @author jbertinetti
 */
@Entity
public class Outer {
    
    private String member;
    
    public String publicMember;


    public Outer(String member) {
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
    
    public final void finalMethod() {
        this.member = "final";
    }
    

    public class Inner {
        private boolean touched = false;
        public void touch() { this.touched = true; }
        public boolean isTouched() { return touched; }
        public void setOuterMember(String member) {
            Outer.this.member = member;
        }
    }

    public static class StaticInner {
        public void setOuterMember(Outer outer, String member) {
            outer.member = member;
        }
    }
    
    public static class StaticInnerBuilder {
        private Outer o = new Outer("StaticInnerBuilder");
        private String s;
        private List<String> sl;
        
        public StaticInnerBuilder setMemberBuilder(String m) {
            o.setMember(m);
            s = "test";
            return this;
        }
        
        public StaticInnerBuilder setList(List<String> lista) {
            sl = new ArrayList<>();
            for (String s: lista) {
                sl.add(s);
            }
            return this;
        }
        
        public Outer build() {
            return o;
        }
        
    }
}
