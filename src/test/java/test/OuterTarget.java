package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
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
    private List<Foo> listaFoo = new ArrayList<>();
    private List<List> listaList = new ArrayList<>();
    private HashMap<String, String> hmap = new HashMap<>();
    
    public String publicMember;

    public boolean ___tdd___dirtyMark;
    public Set<String> ___tdd___modifiedFields = new HashSet<>();

    public OuterTarget(String member) {
        this.member = member;
        this.___tdd___modifiedFields.add("member");
        this.___tdd___setDirty();
    }


    public void setMember(String member) {
        this.member = member;
        this.___tdd___modifiedFields.add("member");
        this.___tdd___setDirty();
    }


    public String getMember() {
        return member;
    }
    
    
    public void anon() {
        new Runnable() {
            @Override
            public void run() {
                member = "run";
                ___tdd___modifiedFields.add("member");
                ___tdd___setDirty();
            }
        }.run();
    }
    
    
    public void lambda() {
        Stream.generate(() -> {___tdd___modifiedFields.add("member"); member = "touched"; return member;}).limit(1).count();
    }
    
    
    public void lambda2() {
        Runnable r = () -> member = "lambda2";
        r.run();
    }
    
    
    public void threaded() {
        new Thread(() -> member = "from thread").run();
    }


    public class Inner {
        private boolean touched = false;
        public void setOuterMember(String member) {
            OuterTarget.this.member = member;
            OuterTarget.this.___tdd___modifiedFields.add("member");
            ___tdd___setDirty();
        }
        
        public void setOuterCollection(String text) {
            lista.add("lista");
            ___tdd___modifiedFields.add("lista");
            ___tdd___setDirty();
        }
        
    }
    
    
    public final void finalMethod() {
        this.member = "final";
    }
    
    public void testNativeCollections(){
        ArrayList<Foo> al = new ArrayList<>();
        Foo f = new Foo("text");
        al.add(f);

        boolean b = this.listaFoo.add(new Foo("text").setS("otro text"));
        
        this.listaList.add(al.subList(0, al.size()));
        
        List<Foo> streamEx = Arrays.asList(new Foo("aaaa"), new Foo("bbb"), new Foo("ccc"));
        streamEx.stream().forEach(o->listaFoo.add(o));
    }
    
    private List lista = new ArrayList();
    
    public void caso1() {
        List.of(1, 2, 3).stream().forEach(n -> this.lista.add(n));
        this.___tdd___modifiedFields.add("lista");
    }

    public void caso2() {
        List.of(1, 2, 3).stream().forEach(this.lista::add);
        this.___tdd___modifiedFields.add("lista");
    }

    public void caso3() {
        Consumer<String> c = this.lista::add;
        String s = "hola";
        c.accept(s);
        this.___tdd___modifiedFields.add("lista");
    }

    public void caso4() {
        List.of(1, 2, 3).stream().filter(this.lista::add).map(n -> n + " agregado").forEach(System.out::println);
        this.___tdd___modifiedFields.add("lista");
    }

    public void caso5() {
        Runnable r = () -> this.lista.add("mundo");
        r.run();
    }
    
    public boolean caso6() {
        return this.lista.add("mundo");
    }
    
    public void caso7(String t) {
        
        if (t.equals("123")){
            this.lista.add("dddd");
            this.___tdd___modifiedFields.add("lista");
        } else {
            this.hmap.put("val", t);
            this.___tdd___modifiedFields.add("hmap");
        }
    }
    
    @Override
    public boolean ___tdd___isDirty() {
        return ___tdd___dirtyMark;
    }

    @Override
    public void ___tdd___setDirty(){
        this.___tdd___dirtyMark = true;
    }

    @Override
    public void ___tdd___clearDirty() {
        this.___tdd___dirtyMark = false;
        this.___tdd___modifiedFields.clear();
    }
    
    @Override
    public Set<String> ___tdd___getModifiedFields() {
        return this.___tdd___modifiedFields;
    }
    
    
}