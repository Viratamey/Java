package collections.map;

import collections.Person;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class mapImpl {
    public static void main(String[] args) {

        // Hashmap as map

        System.out.println("started");

        Map<Person, Integer> map = new HashMap<>();

        System.out.println("started 1");

        Person p1 = new Person(1,"one");
        Person p2 = new Person(2,"two");
        Person p3 = new Person(3,"three");
        Person p4 = new Person(4,"four");
        Person p5 = new Person(5,"five");
        Person p6 = new Person(6,"six");
        Person p7 = new Person(7,"seven");
        Person p8 = new Person(8,"eight");

//        System.out.println(map.put(p1,1));
//        System.out.println(map);
//        System.out.println(map.put(p1,2));
//        System.out.println(map);
//        System.out.println(map.put(p2,1));
//        System.out.println(map);
//        System.out.println(map.put(p1,1));
//        System.out.println(map);
//        map.clear();
//        System.out.println();
//
//        // both put and putIfAbsent returns existing value if present but latter dont update the value with replacing existing one
//
//        System.out.println(map.putIfAbsent(p1,1));
//        System.out.println(map);
//        System.out.println(map.putIfAbsent(p1,2));
//        System.out.println(map);
//        System.out.println(map.putIfAbsent(p2,1));
//        System.out.println(map);
//        System.out.println(map.putIfAbsent(p1,3));
//        System.out.println(map);
//        System.out.println();
//
//        System.out.println(map.size());
//        System.out.println(map.isEmpty());
//        Set<Map.Entry<Person, Integer>> uniques = map.entrySet();
//        System.out.println(map.entrySet());
//        System.out.println();
//
//        map.compute(p1 , (k,v) -> Integer.valueOf(7*k.getAge()));
//        System.out.println(map);
//        System.out.println();
//
//        map.clear();
//        map.put(p1, 3);
//        map.put(p2, 4);
//        map.computeIfPresent(p1, (k,v) -> v = v*8);
//        System.out.println(map);
//        map.computeIfPresent(p1, (k,v) -> v = v*2);
//        System.out.println(map);
//        map.computeIfAbsent(p1, (k) -> k.getAge()*10);
//        System.out.println(map);
//        map.computeIfAbsent(p3, (k) -> k.getAge()*10);
//        System.out.println(map);
//        System.out.println();
//
//        System.out.println(map);
//        System.out.println(map.containsKey(p4));
//        System.out.println(map.containsKey(p1));
//        System.out.println(map.containsValue(30));
//        System.out.println(map.containsValue(36));
//        System.out.println(map.get(p1));
//        System.out.println(map.getOrDefault(p4, 19));
//        System.out.println(map.replace(p3, 13));
//        System.out.println(map);
//        System.out.println();
//
//        System.out.println(map);
//        System.out.println(map.values());
//        System.out.println(map.keySet());
//        System.out.println(map.entrySet());
//        System.out.println();
//
//        map.forEach((k,v) -> System.out.println(k));
//


        // LinkedHashmap as map

        System.out.println("started 2");

        Map<Person, Integer> linkedMap = new LinkedHashMap();
        linkedMap.put(p4, 4);
        linkedMap.put(p5, 5);
        linkedMap.put(p6, 6);
        linkedMap.put(p7, 7);
        linkedMap.put(p1, 1);
        linkedMap.put(p2, 2);
        linkedMap.put(p3, 3);
        linkedMap.put(p8, 8);
        System.out.println(linkedMap);
    }
}
