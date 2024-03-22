/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.function.Consumer;
import net.odbogm.annotations.Entity;



/**
 *
 * @author mdre
 */
@Entity
public class CollectionsTest {
    Collection<String> col;
    Set<String> set;
    SortedSet<String> sset;
    NavigableSet<String> nset;
    TreeSet<String> tset;
    HashSet<String> hset;
    LinkedHashSet<String>lhset;
    List<String> list;
    ArrayList<Foo> alist;
    Vector<String> vector;
    Queue<String> queue;
    Deque<String> dqueue;
    LinkedList<String> llist;
    PriorityQueue<String> pqueue;
    Map<String,String> map;
    SortedMap<String,String> smap;
    HashMap<String,String> hmap;
    NavigableMap<String,String> nmap;
    LinkedHashMap<String,String> lhmap;
    TreeMap<String,String> tmap;

    private String txt = "test";
    public CollectionsTest() {
        col = new ArrayList<>();
        set = new HashSet<>();
        sset = new TreeSet<>();
        nset = new TreeSet<>();
        lhset = new LinkedHashSet<>();
        list = new ArrayList<>();
        alist = new ArrayList<>();
        vector = new Vector<>();
        queue = new LinkedList<>();
        dqueue = new LinkedList<>();
        llist = new LinkedList<>();
        pqueue = new PriorityQueue<>();
        map = new HashMap<>();
        hmap= new HashMap<>();
        nmap = new TreeMap<>();
        lhmap = new LinkedHashMap<>();
        tmap = new TreeMap<>();
                
    }
    
    public void addTest() {
//        List<String> l = new ArrayList<>();
//        l.add("xx");
        this.txt = " txt txt";
        alist.add(new Foo(this.txt+"text").setS("otro text"));
        
        hmap.put("key", "value");
    }
    
    private List lista = new ArrayList();
    
    public void caso1() {
        List.of(1, 2, 3).stream().forEach(n -> this.lista.add(n));
    }

    public void caso2() {
        List.of(1, 2, 3).stream().forEach(this.lista::add);
    }

    public void caso3() {
        Consumer<String> c = this.lista::add;
        String s = "hola";
        c.accept(s);
    }

    public void caso4() {
        List.of(1, 2, 3).stream().filter(this.lista::add).map(n -> n + " agregado").forEach(System.out::println);
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
            //List.of(1, 2, 3).stream().forEach(this.lista::add);
            this.lista.add("dddd");
        } else {
            this.hmap.put("val", t);
        }
    }
    
    public void caso8(int i) {
        for (int j = 0; j < i; j++) {
            if (i%2==0) {
                this.lista.add("i:"+i);            
            }
        }
        
    }
    
    
    public void caso9(String test) {
        switch(test){
            case "lista":
                this.lista.add("switch");
                break;
            case "hmap":
                this.hmap.put("switch", "hmap");
                break;
        }
        
    }
    
    public void caso10() {
        col.add("txt");
        set.addAll(col);
        sset.add("sset");
        nset.add("nset");
        lhset.add("lhset");
        list.clear(); //add("list");
        alist.add(new Foo("foo"));
        vector.addElement("element");
        queue.add("queue");
        dqueue.addFirst("dqueue");
        llist.addLast("last");
        pqueue.add("pqueue");
        map.put("k", "v");
        hmap.put("k", "v");
        nmap.put("k", "v");
        lhmap.put("k", "v");
        tmap.clear();
    }
    
    public void caso11() {
        
        list.clear(); //add("list");
        
    }
}





















