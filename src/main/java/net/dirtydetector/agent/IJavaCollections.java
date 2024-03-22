/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package net.dirtydetector.agent;

import java.util.HashSet;

/**
 *
 * @author mdre
 */
public interface IJavaCollections {
    
    HashSet<String> javaCollections = new HashSet<>();
    HashSet<String> javaCollectionsDirtyMethods = new HashSet<>();
    
    default HashSet<String> getJavaCollections() {
        if (javaCollections.size()==0){
//            javaCollections.add(ISDIRTY);
            javaCollections.add("Ljava/util/ArrayList;");
            javaCollections.add("Ljava/util/Collection;");
            javaCollections.add("Ljava/util/Deque;");
            javaCollections.add("Ljava/util/HashMap;");
            javaCollections.add("Ljava/util/HashSet;");
            javaCollections.add("Ljava/util/LinkedHashMap;");
            javaCollections.add("Ljava/util/LinkedHashSet;");
            javaCollections.add("Ljava/util/LinkedList;");
            javaCollections.add("Ljava/util/List;");
            javaCollections.add("Ljava/util/Map;");
            javaCollections.add("Ljava/util/NavigableMap;");
            javaCollections.add("Ljava/util/NavigableSet;");
            javaCollections.add("Ljava/util/PriorityQueue;");
            javaCollections.add("Ljava/util/Queue;");
            javaCollections.add("Ljava/util/Set;");
            javaCollections.add("Ljava/util/SortedMap;");
            javaCollections.add("Ljava/util/SortedSet;");
            javaCollections.add("Ljava/util/TreeMap;");
            javaCollections.add("Ljava/util/TreeSet;");
            javaCollections.add("Ljava/util/Vector;");    
        }
        return javaCollections;
    }
    
    default HashSet<String> getJavaCollectionsDirtyMethods() {
        if (javaCollectionsDirtyMethods.size()==0){
            javaCollectionsDirtyMethods.add("add");
            javaCollectionsDirtyMethods.add("addAll");
            javaCollectionsDirtyMethods.add("addElement");
            javaCollectionsDirtyMethods.add("addFirst");
            javaCollectionsDirtyMethods.add("addLast");
            javaCollectionsDirtyMethods.add("clear");
            javaCollectionsDirtyMethods.add("ensureCapacity");
            javaCollectionsDirtyMethods.add("fastRemove");
            javaCollectionsDirtyMethods.add("grow");
            javaCollectionsDirtyMethods.add("merge");
            javaCollectionsDirtyMethods.add("offer");
            javaCollectionsDirtyMethods.add("offerFirst");
            javaCollectionsDirtyMethods.add("offerLast");
            javaCollectionsDirtyMethods.add("poll");
            javaCollectionsDirtyMethods.add("pollFirst");
            javaCollectionsDirtyMethods.add("pollFirstEntry");
            javaCollectionsDirtyMethods.add("pollLast");
            javaCollectionsDirtyMethods.add("pollLastEntry");
            javaCollectionsDirtyMethods.add("pop");
            javaCollectionsDirtyMethods.add("push");
            javaCollectionsDirtyMethods.add("put");
            javaCollectionsDirtyMethods.add("putAll");
            javaCollectionsDirtyMethods.add("putIfAbsent");
            javaCollectionsDirtyMethods.add("readObject");
            javaCollectionsDirtyMethods.add("remove");
            javaCollectionsDirtyMethods.add("removeAll");
            javaCollectionsDirtyMethods.add("removeAllElements");
            javaCollectionsDirtyMethods.add("removeElement");
            javaCollectionsDirtyMethods.add("removeElementAt");
            javaCollectionsDirtyMethods.add("removeFirst");
            javaCollectionsDirtyMethods.add("removeFirstOccurrence");
            javaCollectionsDirtyMethods.add("removeIf");
            javaCollectionsDirtyMethods.add("removeLast");
            javaCollectionsDirtyMethods.add("removeLastOccurrence");
            javaCollectionsDirtyMethods.add("removeRange");
            javaCollectionsDirtyMethods.add("replace");
            javaCollectionsDirtyMethods.add("replaceAll");
            javaCollectionsDirtyMethods.add("retainAll");
            javaCollectionsDirtyMethods.add("set");
            javaCollectionsDirtyMethods.add("setElementAt");
            javaCollectionsDirtyMethods.add("setSize");
            javaCollectionsDirtyMethods.add("sort");
            javaCollectionsDirtyMethods.add("trimToSize");
        }
        return javaCollectionsDirtyMethods;
    }
}
