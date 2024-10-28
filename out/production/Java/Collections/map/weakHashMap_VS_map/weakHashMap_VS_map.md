Hashmap vs WeakHashMap in Java
==============================

[**HashMap**](https://www.geeksforgeeks.org/hashmap-treemap-java/)

Java.util.HashMap class is a Hashing based implementation. In HashMap, we have a key and a value pair.\
Even though the object is specified as key in hashmap, it does not have any reference and it is **not** eligible for garbage collection if it is associated with HashMap i.e. HashMap dominates over Garbage Collector.

-   Java

|

`// Java program to illustrate`

`// Hashmap`

`import` `java.util.*;`

`class` `HashMapDemo`

`{`

`public` `static` `void` `main(String args[])``throws` `Exception`

`{`

`HashMap m =` `new` `HashMap();`

`Demo d =` `new` `Demo();`

`// puts an entry into HashMap`

`m.put(d,``" Hi "``);`

`System.out.println(m);`

`d =` `null``;`

`// garbage collector is called`

`System.gc();`

`//thread sleeps for 4 sec`

`Thread.sleep(``4000``);`

`System.out.println(m);`

`}`

`}`

`class` `Demo`

`{`

`public` `String toString()`

`{`

`return` `"demo"``;`

`}`

`// finalize method`

`public` `void` `finalize()`

`{`

`System.out.println(``"Finalize method is called"``);`

`}`

`}`

 |

Output: 

{demo=Hi}
{demo=Hi}

**WeakHashMap**

WeakHashMap is an implementation of the Map interface. WeakHashMap is almost same as HashMap except in case of WeakHashMap, if object is specified as key doesn't contain any references- it is eligible for garbage collection even though it is associated with WeakHashMap. i.e Garbage Collector dominates over WeakHashMap.

-   Java

    |
    
    `// Java program to illustrate`
    
    `// WeakHashmap`
    
    `import` `java.util.*;`
    
    `class` `WeakHashMapDemo`
    
    `{`
    
    `public` `static` `void` `main(String args[])``throws` `Exception`
    
    `{`
    
    `WeakHashMap m =` `new` `WeakHashMap();`
    
    `Demo d =` `new` `Demo();`
    
    `// puts an entry into WeakHashMap`
    
    `m.put(d,``" Hi "``);`
    
    `System.out.println(m);`
    
    `d =` `null``;`
    
    `// garbage collector is called`
    
    `System.gc();`
    
    `// thread sleeps for 4 sec`
    
    `Thread.sleep(``4000``); .`
    
    `System.out.println(m);`
    
    `}`
    
    `}`
    
    `class` `Demo`
    
    `{`
    
    `public` `String toString()`
    
    `{`
    
    `return` `"demo"``;`
    
    `}`
    
    `// finalize method`
    
    `public` `void` `finalize()`
    
    `{`
    
    `System.out.println(``"finalize method is called"``);`
    
    `}`
    
    `}`
    
     |

Output: 

{demo = Hi}
finalize method is called
{ }

**Some more important differences between Hashmap and WeakHashmap: **

1.  [**Strong vs Weak References**](https://www.geeksforgeeks.org/types-references-java/): Weak Reference Objects are not the default type/class of Reference Object and they should be explicitly specified while using them. This type of reference is used in WeakHashMap to reference the entry objects.\
    Strong References: This is the default type/class of Reference Object. Any object which has an active strong reference are not eligible for garbage collection. In HashMap, key objects have strong references. 
2.  **Role of Garbage Collector:** Garbage Collected : In HashMap , entry object(entry object stores key-value pairs) is not eligible for garbage collection i.e Hashmap is dominant over Garbage Collector.\
    In WeakHashmap, When a key is discarded then its entry is automatically removed from the map, in other words, garbage collected.
3.  [**Clone method**](https://www.geeksforgeeks.org/clone-method-in-java-2/)** Implementation: **HashMap implements Cloneable interface.\
    WeakHashMap does not implement Cloneable interface, it only implements Map interface. Hence, there is no clone() method in the WeakHashMap class.
