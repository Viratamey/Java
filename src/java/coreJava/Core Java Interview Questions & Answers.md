# Core Java Interview Questions & Answers

A complete revision guide covering JVM, OOP, Strings, Exceptions, Collections, Multithreading, Java 8+ and tricky questions. Read the answers, then try to explain each one in your own words.

## 1. Java Basics & JVM

**Q1. What are the main features of Java?** Platform independent (write once, run anywhere via bytecode), object-oriented, robust (strong typing, exception handling, garbage collection), secure (no explicit pointers, bytecode verifier), multithreaded, portable and high performance (JIT compiler).

**Q2. Difference between JDK, JRE and JVM?**

- **JVM**: runs bytecode; it is platform specific and provides memory management and GC.
- **JRE**: JVM plus core libraries needed to run programs.
- **JDK**: JRE plus development tools (javac, jar, javadoc, debugger).

**Q3. Why is Java platform independent but the JVM is not?** `javac` compiles source into bytecode (.class), which is the same on every OS. Each OS has its own JVM that translates bytecode to native instructions.

**Q4. What is the JIT compiler?** Part of the JVM that compiles frequently executed bytecode (hot spots) into native machine code at runtime so repeated execution is much faster.

**Q5. Explain the JVM memory areas.**

- **Heap**: all objects and arrays; shared by threads; garbage collected. Split into Young (Eden, S0, S1) and Old generation.
- **Metaspace** (Java 8+, replaced PermGen): class metadata, in native memory.
- **Stack**: one per thread; holds stack frames with local variables, partial results and method calls.
- **PC register**: address of the current instruction per thread.
- **Native method stack**: for native (C/C++) methods.

**Q6. What is the class loader and its types?** Loads classes into the JVM at runtime. Types: Bootstrap (core classes, rt.jar/java.base), Platform/Extension, and Application (classpath) class loaders. It follows the **parent delegation model**: a loader first asks its parent before loading itself.

**Q7. What are the primitive data types and their sizes?** byte (1 byte), short (2), int (4), long (8), float (4), double (8), char (2, Unicode), boolean (JVM dependent, logically 1 bit). Default values: 0 for numbers, `false` for boolean, `'\u0000'` for char, `null` for references.

**Q8. What is autoboxing and unboxing?** Automatic conversion between primitives and wrapper classes (`int` to `Integer` and back). Pitfall: unboxing a `null` wrapper throws `NullPointerException`, and `Integer` values between -128 and 127 are cached, so `==` works for them but not for larger values (use `equals`).

**Q9. Difference between `==` and `equals()`?** `==` compares primitive values or object references (same object in memory). `equals()` compares logical content, if overridden. The default `Object.equals()` behaves like `==`.

**Q10. Pass by value or pass by reference?** Java is **always pass by value**. For objects, the value of the reference is copied, so you can modify the object's state through it, but reassigning the parameter does not change the caller's variable.

## 2. Object-Oriented Programming

**Q11. What are the four pillars of OOP?**

- **Encapsulation**: bundling data and methods, hiding data using private fields and getters/setters.
- **Inheritance**: a class acquires properties of another (`extends`) for reuse.
- **Polymorphism**: one interface, many forms (overloading at compile time, overriding at runtime).
- **Abstraction**: exposing only essential behaviour via abstract classes and interfaces.

**Q12. Method overloading vs overriding?**

- **Overloading**: same name, different parameters, same class, resolved at compile time. Return type alone cannot differentiate.
- **Overriding**: subclass redefines a parent method with the same signature, resolved at runtime. Access cannot be reduced, return type must be same or covariant, and it cannot throw broader checked exceptions. `static`, `final` and `private` methods cannot be overridden.

**Q13. Abstract class vs interface?**

| Abstract class | Interface |
| --- | --- |
| Can have state (instance fields), constructors | Only `public static final` constants, no constructors |
| Single inheritance | A class can implement many |
| Any access modifiers | Methods public by default |
| Use for "is-a" with shared code | Use for capability/contract |

Since Java 8 interfaces can have `default` and `static` methods; Java 9 added `private` methods.

**Q14. Can we instantiate an abstract class or interface?** No. But you can create anonymous subclasses or use lambdas for functional interfaces.

**Q15. What is a constructor? Can it be inherited, overridden or private?** A special method with the class name and no return type, called on object creation. Constructors are not inherited and cannot be overridden, but can be overloaded. A private constructor is valid (used in Singleton and utility classes). If you write none, the compiler adds a default no-arg constructor.

**Q16. What is constructor chaining?** Calling one constructor from another using `this(...)` in the same class or `super(...)` for the parent. It must be the first statement. If not written, the compiler inserts `super()` automatically.

**Q17. What is the `super` keyword?** Refers to the parent class: `super.method()` calls the parent's version, `super.field` accesses the parent's field, `super(...)` calls the parent constructor.

**Q18. What is the difference between composition, aggregation and inheritance?** Inheritance is "is-a". Composition is a strong "has-a" (part cannot exist without the whole, e.g. House and Room). Aggregation is a weak "has-a" (part can exist independently, e.g. Team and Player). Prefer composition over inheritance for flexibility.

**Q19. Does Java support multiple inheritance?** Not with classes (avoids the diamond problem), but yes through interfaces. If two interfaces have the same default method, the implementing class must override it and can pick one with `InterfaceName.super.method()`.

**Q20. What is polymorphism in practice (dynamic dispatch)?**

```java
Animal a = new Dog();
a.sound(); // Dog's sound() runs, decided at runtime
```

Methods are resolved by the actual object type; fields are resolved by the reference type.

**Q21. What is covariant return type?** An overriding method may return a subtype of the parent method's return type.

**Q22. What is the `instanceof` operator?** Checks whether an object is an instance of a class or interface. Java 16+ supports pattern matching: `if (obj instanceof String s) { ... }`.

## 3. Important Keywords

**Q23. Explain `static`.** Belongs to the class, not instances. Static variables are shared by all objects; static methods can be called without an object and cannot access instance members directly; static blocks run once when the class loads; static nested classes do not need an outer instance.

**Q24. Explain `final`.**

- Variable: value cannot be reassigned (for objects, the reference is fixed, but state can change).
- Method: cannot be overridden.
- Class: cannot be extended (e.g. `String`).

**Q25. final vs finally vs finalize?** `final` is a modifier. `finally` is a block that always runs after try/catch (except `System.exit()` or JVM crash). `finalize()` was called by GC before collecting an object; it is **deprecated** and should not be used.

**Q26. Can we override a static method?** No. A static method with the same signature in a subclass **hides** the parent method (method hiding).

**Q27. Can the `main` method be overloaded or overridden? What if it is not static?** It can be overloaded, and static methods cannot be overridden. The JVM looks for `public static void main(String[] args)`. Without `static`, the program compiles but fails at runtime with an error that the main method is not found (in newer Java versions with instance main preview this may differ).

**Q28. What is the `transient` keyword?** Marks a field to be skipped during serialization.

**Q29. What is the `volatile` keyword?** Guarantees visibility (writes by one thread are immediately visible to others) and prevents instruction reordering for that variable. It does **not** guarantee atomicity (`count++` is still unsafe).

**Q30. What is the `synchronized` keyword?** Provides mutual exclusion and visibility. It can be applied to a method or a block, using the object's intrinsic lock (monitor). Static synchronized uses the class-level lock.

**Q31. Access modifiers?** `private` (same class), default/package-private (same package), `protected` (same package plus subclasses), `public` (everywhere).

**Q32. What is the `this` keyword?** Reference to the current object. Used to resolve name clashes, call another constructor, or pass the current object.

## 4. Strings

**Q33. Why is `String` immutable?** For security (used in class loading, network connections, file paths), thread safety, caching of hash code, and enabling the String Pool. Once created, its value cannot change; operations create new objects.

**Q34. What is the String Pool?** A special area in the heap where string literals are stored and reused. `String a = "hi"; String b = "hi";` gives `a == b` true. `new String("hi")` always creates a new heap object, and `intern()` returns the pooled instance.

**Q35. How many objects are created by `String s = new String("abc")`?** Up to two: one in the String Pool (if "abc" is not already there) and one in the heap.

**Q36. String vs StringBuilder vs StringBuffer?**

|  | String | StringBuilder | StringBuffer |
| --- | --- | --- | --- |
| Mutable | No | Yes | Yes |
| Thread safe | Yes (immutable) | No | Yes (synchronized) |
| Speed | Slow for concatenation in loops | Fastest | Slower than StringBuilder |

**Q37. How do you reverse a string / check palindrome?**

```java
String rev = new StringBuilder(s).reverse().toString();
boolean isPalindrome = s.equals(rev);
```

**Q38. Why is `char[]` preferred for passwords?** Strings stay in the pool until GC and can show up in memory dumps; a char array can be wiped after use.

**Q39. Can a String be used in a switch?** Yes, since Java 7.

## 5. Exception Handling

**Q40. Describe the exception hierarchy.** `Throwable` has two branches: `Error` (serious JVM problems like `OutOfMemoryError`, `StackOverflowError`; not meant to be caught) and `Exception`. Under `Exception`: **checked** (compile-time checked, e.g. `IOException`, `SQLException`) and **unchecked** `RuntimeException` (e.g. `NullPointerException`, `ArrayIndexOutOfBoundsException`, `ClassCastException`, `IllegalArgumentException`).

**Q41. Checked vs unchecked exceptions?** Checked ones must be caught or declared with `throws`. Unchecked ones need not be, and usually indicate programming bugs.

**Q42. throw vs throws?** `throw` actually throws an exception object inside a method body. `throws` declares in the method signature which exceptions it may pass to the caller.

**Q43. Will `finally` execute if there is a `return` in try?** Yes. `finally` runs before the method returns. If `finally` itself returns, it overrides the try's return value (bad practice).

**Q44. Can we have try without catch?** Yes, if there is a `finally` or it is try-with-resources.

**Q45. What is try-with-resources?** Automatically closes resources that implement `AutoCloseable` (Java 7+).

```java
try (BufferedReader br = new BufferedReader(new FileReader(f))) {
    return br.readLine();
}
```

Suppressed exceptions from `close()` are attached to the primary exception.

**Q46. How do you create a custom exception?** Extend `Exception` (checked) or `RuntimeException` (unchecked), and provide constructors taking a message and a cause.

**Q47. Rules for overriding with exceptions?** The overriding method can throw the same, narrower or no checked exceptions, but not broader ones. It may freely throw any unchecked exceptions.

**Q48. Order of multiple catch blocks?** Most specific first, then general. Otherwise the compiler reports unreachable code. Multi-catch: `catch (IOException | SQLException e)`.

**Q49. StackOverflowError vs OutOfMemoryError?** StackOverflowError: the thread stack is full, usually from infinite or too deep recursion. OutOfMemoryError: the heap (or Metaspace) cannot allocate more, usually from memory leaks or huge data.

## 6. Collections Framework

**Q50. Describe the Collection hierarchy.** `Iterable` then `Collection`, which splits into `List` (ArrayList, LinkedList, Vector), `Set` (HashSet, LinkedHashSet, TreeSet) and `Queue`/`Deque` (PriorityQueue, ArrayDeque). `Map` (HashMap, LinkedHashMap, TreeMap, Hashtable, ConcurrentHashMap) is **separate** and does not extend `Collection`.

**Q51. ArrayList vs LinkedList?** ArrayList uses a dynamic array: O(1) random access, O(n) insert/delete in the middle, good cache locality. LinkedList uses a doubly linked list: O(n) access, O(1) insert/delete at a known node, more memory per element. ArrayList is the better default.

**Q52. ArrayList vs Vector?** Vector is synchronized (legacy) and grows by 100%; ArrayList is not synchronized and grows by about 50%.

**Q53. How does HashMap work internally?** It uses an array of buckets. On `put(k, v)`: compute `hashCode()`, spread the hash, find the index `(n-1) & hash`. If the bucket is empty, store a node. On collision, compare using `equals()`; the same key replaces the value, otherwise the node is added to a linked list. Since Java 8, a bucket converts to a **red-black tree** when it has 8 or more nodes (and the table is at least 64), improving worst-case lookup from O(n) to O(log n). Default capacity is 16 and load factor is 0.75; when size exceeds capacity times load factor the table **doubles and rehashes**.

**Q54. What is the equals() and hashCode() contract?**

- If `a.equals(b)` is true, then `a.hashCode() == b.hashCode()`.
- Equal hash codes do not imply equal objects.
- `equals` must be reflexive, symmetric, transitive, consistent, and `x.equals(null)` is false.

If you override `equals` without `hashCode`, hash-based collections will misbehave (duplicates, failed lookups).

**Q55. Can a mutable object be a HashMap key?** Technically yes, but if its fields used in `hashCode` change after insertion, the entry becomes unreachable. Use immutable keys such as `String` or `Integer`.

**Q56. HashMap vs Hashtable vs ConcurrentHashMap?**

- HashMap: not thread safe, allows one null key and many null values.
- Hashtable: legacy, all methods synchronized, no nulls.
- ConcurrentHashMap: thread safe with fine-grained locking/CAS (bins), better concurrency, no null keys or values.

**Q57. HashMap vs LinkedHashMap vs TreeMap?** HashMap: no order. LinkedHashMap: insertion order (or access order, useful for LRU caches). TreeMap: sorted by key (red-black tree), O(log n), keys need `Comparable` or a `Comparator`, no null keys.

**Q58. HashSet vs TreeSet vs LinkedHashSet?** HashSet is backed by a HashMap, unordered, O(1). LinkedHashSet keeps insertion order. TreeSet is sorted, O(log n).

**Q59. How does HashSet ensure uniqueness?** Elements are stored as keys in an internal HashMap with a dummy value, so uniqueness depends on `hashCode()` and `equals()`.

**Q60. Comparable vs Comparator?** `Comparable` defines the natural order inside the class (`compareTo`), one ordering only. `Comparator` is an external, separate ordering (`compare`), and many can exist.

```java
list.sort(Comparator.comparing(Employee::getSalary).reversed());
```

**Q61. Fail-fast vs fail-safe iterators?** Fail-fast iterators (ArrayList, HashMap) throw `ConcurrentModificationException` if the collection is structurally modified during iteration, detected via `modCount`. Fail-safe ones (CopyOnWriteArrayList, ConcurrentHashMap) work on a copy or a weakly consistent view and don't throw. To remove safely, use `Iterator.remove()` or `removeIf()`.

**Q62. Iterator vs ListIterator?** Iterator moves forward only, on any collection. ListIterator works only on Lists, can go both directions, and can add/set elements.

**Q63. What is PriorityQueue?** A min-heap by default (head is the smallest element), O(log n) insert and remove, O(1) peek. It is not thread safe and does not guarantee order when iterating.

**Q64. Arrays.asList() pitfalls?** Returns a fixed-size list backed by the array: `add`/`remove` throw `UnsupportedOperationException`, though `set` works. `List.of()` returns an immutable list that disallows nulls.

**Q65. Difference between Collection and Collections?** `Collection` is the root interface. `Collections` is a utility class with static methods (`sort`, `reverse`, `unmodifiableList`, `synchronizedList`).

**Q66. How do you make a collection thread safe?** `Collections.synchronizedList(...)`, or concurrent classes: `CopyOnWriteArrayList`, `ConcurrentHashMap`, `ConcurrentLinkedQueue`, `BlockingQueue` implementations.

**Q67. How do you remove duplicates from a list?** `new ArrayList<>(new LinkedHashSet<>(list))` keeps order; or `list.stream().distinct().toList()`.

## 7. Multithreading & Concurrency

**Q68. Process vs thread?** A process is an independent program with its own memory. Threads are lightweight units within a process that share heap memory but have their own stack.

**Q69. Ways to create a thread?**

1. Extend `Thread` and override `run()`.
2. Implement `Runnable` (preferred, since the class can still extend another).
3. Implement `Callable` (returns a result, can throw checked exceptions) with `ExecutorService`.
4. Lambda: `new Thread(() -> ...)`.

**Q70. start() vs run()?** `start()` creates a new thread and invokes `run()` in it. Calling `run()` directly just executes it like a normal method on the current thread. Calling `start()` twice throws `IllegalThreadStateException`.

**Q71. Thread lifecycle states?** NEW, RUNNABLE, BLOCKED (waiting for a monitor lock), WAITING (`wait()`, `join()`), TIMED\_WAITING (`sleep(ms)`, `wait(ms)`), TERMINATED.

**Q72. sleep() vs wait()?** `sleep()` is a `Thread` static method, pauses the thread and does **not** release the lock. `wait()` is an `Object` method, must be called inside a synchronized block, **releases the lock**, and waits for `notify()`/`notifyAll()`.

**Q73. What is a race condition? A deadlock?** Race condition: the outcome depends on thread timing when shared data is accessed unsafely. Deadlock: two or more threads wait forever for locks held by each other.

**Q74. Four conditions for deadlock and how to avoid it?** Mutual exclusion, hold and wait, no preemption, circular wait. Avoid by acquiring locks in a consistent global order, using `tryLock` with timeouts, and minimizing lock scope.

**Q75. What is thread safety and how do you achieve it?** Code behaves correctly under concurrent access. Techniques: `synchronized`, `Lock`, `volatile`, atomic classes (`AtomicInteger`), immutability, thread confinement (`ThreadLocal`), concurrent collections.

**Q76. synchronized vs ReentrantLock?** ReentrantLock gives `tryLock()`, timed and interruptible locking, fairness option, multiple `Condition`s, but you must unlock manually in `finally`. `synchronized` is simpler and auto-released.

**Q77. What are atomic classes?** `AtomicInteger`, `AtomicLong`, `AtomicReference` use CAS (compare-and-swap) hardware instructions for lock-free thread-safe updates.

**Q78. What is the Executor framework / thread pool?** Manages a pool of reusable threads to avoid creating a thread per task.

```java
ExecutorService ex = Executors.newFixedThreadPool(4);
Future<Integer> f = ex.submit(() -> 42);
ex.shutdown();
```

Types: fixed, cached, single, scheduled. In production prefer constructing `ThreadPoolExecutor` directly to control queue size and rejection policy. Java 21 adds virtual threads (`Executors.newVirtualThreadPerTaskExecutor()`).

**Q79. Runnable vs Callable? Future vs CompletableFuture?** Callable returns a value and may throw. `Future.get()` blocks. `CompletableFuture` supports non-blocking chaining (`thenApply`, `thenCompose`, `thenCombine`, `exceptionally`).

**Q80. What are CountDownLatch, CyclicBarrier, Semaphore?**

- CountDownLatch: one or more threads wait until a count reaches zero; one-time use.
- CyclicBarrier: threads wait for each other at a barrier point; reusable.
- Semaphore: limits the number of threads accessing a resource using permits.

**Q81. What is a daemon thread?** A background thread (GC is one). The JVM exits when only daemon threads remain. Set with `setDaemon(true)` before `start()`.

**Q82. What is ThreadLocal?** Gives each thread its own copy of a variable. Remember to call `remove()` in thread pools to avoid leaks.

**Q83. What is the Java Memory Model (happens-before)?** Defines when writes by one thread are guaranteed visible to another, e.g. unlock happens-before a later lock of the same monitor; a volatile write happens-before a later read; `Thread.start()` happens-before actions in that thread.

**Q84. What is a thread starvation / livelock?** Starvation: a thread never gets CPU or lock access. Livelock: threads keep responding to each other but make no progress.

**Q85. How does `join()` work?** `t.join()` makes the calling thread wait until `t` finishes.

## 8. Memory Management & Garbage Collection

**Q86. What is garbage collection?** Automatic reclaiming of memory from objects no longer reachable from GC roots (thread stacks, static fields, JNI references).

**Q87. Stack vs heap?** Stack: per thread, stores primitives and references in method frames, fast, auto-freed on method exit. Heap: shared, stores objects, managed by GC.

**Q88. How does generational GC work?** New objects go to Eden. Minor GC copies survivors to Survivor spaces and ages them; objects surviving enough cycles are promoted to the Old generation, collected by Major/Full GC. Most objects die young, which makes this efficient.

**Q89. Common GC algorithms?** Serial, Parallel (throughput), G1 (default since Java 9; region based, predictable pauses), ZGC and Shenandoah (very low pause, mostly concurrent).

**Q90. Can we force garbage collection?** `System.gc()` is only a hint; the JVM may ignore it.

**Q91. Can Java have memory leaks?** Yes: objects that are still referenced but unused, e.g. static collections that keep growing, unclosed resources, listeners not removed, `ThreadLocal` in pools, bad `hashCode` keys in caches.

**Q92. Strong, soft, weak and phantom references?** Strong: normal, never collected while reachable. Soft: collected when memory is low (caches). Weak: collected at next GC (`WeakHashMap`). Phantom: used for post-mortem cleanup actions.

**Q93. Useful JVM flags?** `-Xms` initial heap, `-Xmx` max heap, `-Xss` thread stack size, `-XX:+HeapDumpOnOutOfMemoryError`, `-XX:+UseG1GC`.

## 9. Java 8+ Features

**Q94. Main Java 8 features?** Lambda expressions, functional interfaces, Stream API, `Optional`, default/static interface methods, method references, new Date/Time API (`java.time`), `CompletableFuture`.

**Q95. What is a functional interface?** An interface with exactly one abstract method (may have default/static ones); annotate with `@FunctionalInterface`. Examples: `Runnable`, `Comparator`, `Function<T,R>`, `Predicate<T>`, `Consumer<T>`, `Supplier<T>`, `BiFunction`, `UnaryOperator`.

**Q96. What is a lambda expression?** An anonymous function implementing a functional interface: `(a, b) -> a + b`. Variables captured from outside must be **effectively final**.

**Q97. Types of method references?** Static (`Integer::parseInt`), instance of a particular object (`System.out::println`), instance of an arbitrary object of a type (`String::length`), constructor (`ArrayList::new`).

**Q98. What is the Stream API? Intermediate vs terminal operations?** Processes collections in a declarative pipeline; it does not store data or modify the source. **Intermediate** ops are lazy and return a stream (`filter`, `map`, `flatMap`, `sorted`, `distinct`, `limit`). **Terminal** ops trigger execution (`collect`, `forEach`, `reduce`, `count`, `findFirst`, `anyMatch`). A stream cannot be reused after a terminal operation.

**Q99. map() vs flatMap()?** `map` transforms each element one-to-one. `flatMap` transforms each element into a stream and flattens them into one stream (e.g. a list of lists into a single list).

**Q100. Example: group employees by department and count.**

```java
Map<String, Long> m = emps.stream()
    .collect(Collectors.groupingBy(Employee::getDept, Collectors.counting()));
```

**Q101. What is Optional?** A container to represent a possibly absent value and avoid `NullPointerException`: `Optional.ofNullable(x).map(...).orElse(default)`. Do not use it for fields or method parameters.

**Q102. Stream vs parallel stream?** `parallelStream()` splits work across the ForkJoinPool common pool. Useful for large CPU-bound, stateless, independent tasks; harmful for small data, IO, or shared mutable state.

**Q103. Default methods: why were they added?** To add new methods to interfaces (like `forEach` in `Iterable`) without breaking existing implementations.

**Q104. Notable newer features (Java 9 to 21)?** Modules (9), `var` local type inference (10), switch expressions (14), text blocks (15), records (16), sealed classes (17), pattern matching for instanceof (16) and switch (21), virtual threads (21), `List.of/Map.of`, `Stream.toList()`.

**Q105. What is a record?** A concise immutable data carrier: `record Point(int x, int y) {}` auto-generates constructor, accessors, `equals`, `hashCode` and `toString`. Records are implicitly final.

## 10. Generics, Inner Classes, Serialization & Misc

**Q106. What are generics and why are they used?** Type parameters (`List<String>`) giving compile-time type safety and removing casts.

**Q107. What is type erasure?** Generic type information exists only at compile time; the compiler removes it and inserts casts. Hence you cannot do `new T()`, `instanceof List<String>`, or have `List<int>`.

**Q108. Wildcards: `? extends T` vs `? super T`?** PECS: **P**roducer **E**xtends, **C**onsumer **S**uper. `List<? extends Number>` is for reading; `List<? super Integer>` is for writing. `List<Object>` is not a supertype of `List<String>`.

**Q109. Types of nested classes?** Static nested, inner (non-static, holds an outer reference), local (inside a method), anonymous.

**Q110. What is serialization?** Converting an object to a byte stream by implementing `Serializable` (a marker interface). `serialVersionUID` versions the class; `transient` and `static` fields are skipped. If the parent is not Serializable, its no-arg constructor runs on deserialization.

**Q111. What is reflection?** Inspecting and modifying classes, methods and fields at runtime (`Class.forName`, `getDeclaredMethods`). Powerful (frameworks like Spring use it) but slower and breaks encapsulation.

**Q112. What are annotations?** Metadata attached to code, e.g. `@Override`, `@Deprecated`, `@FunctionalInterface`. Retention policies: SOURCE, CLASS, RUNTIME.

**Q113. How do you create an immutable class?** Declare the class `final`, make all fields `private final`, no setters, initialize via constructor, and defensively copy mutable fields on the way in and out.

**Q114. How do you implement a thread-safe Singleton?**

```java
public class Singleton {
    private Singleton() {}
    private static class Holder { static final Singleton I = new Singleton(); }
    public static Singleton getInstance() { return Holder.I; }
}
```

Other options: enum singleton (best against reflection and serialization attacks), or double-checked locking with a `volatile` instance.

**Q115. What is the `clone()` method? Shallow vs deep copy?** Shallow copy duplicates the object but shares referenced objects; deep copy duplicates the whole graph. Prefer copy constructors over `Cloneable`.

**Q116. What does `Object` class provide?** `equals`, `hashCode`, `toString`, `getClass`, `clone`, `finalize`, `wait`, `notify`, `notifyAll`.

**Q117. What are common design patterns in Java?** Singleton, Factory, Builder, Observer, Strategy, Decorator, Proxy, Adapter, Template Method. Be ready to give one real example of each (e.g. `Collections.unmodifiableList` is a Decorator, `Iterator` is the Iterator pattern, `StringBuilder` is Builder-like).

**Q118. What are the SOLID principles?** Single responsibility, Open/closed, Liskov substitution, Interface segregation, Dependency inversion.

**Q119. Enum features?** Enums are type-safe constants, can have fields, constructors and methods, implement interfaces, and cannot extend classes. Use `values()`, `valueOf()`, `ordinal()`, and `EnumMap`/`EnumSet` for efficiency.

**Q120. What is the difference between `Error` and `Exception` handling in practice?** Catch `Exception` types you can recover from. Don't catch `Error`s (except for logging at the top level). Never swallow exceptions silently; log or rethrow with the cause.

## 11. Tricky Output & Concept Questions

**Q121. What is the output?**

```java
String a = "java"; String b = "java"; String c = new String("java");
System.out.println(a == b);       // true
System.out.println(a == c);       // false
System.out.println(a.equals(c));  // true
```

**Q122.**

```java
Integer x = 127, y = 127, p = 128, q = 128;
System.out.println(x == y); // true  (cached)
System.out.println(p == q); // false (new objects)
```

**Q123.**

```java
System.out.println(0.1 + 0.2 == 0.3); // false
```

Floating point cannot represent these exactly. Use `BigDecimal` (constructed from Strings) for money.

**Q124.**

```java
System.out.println(10 / 0);    // ArithmeticException
System.out.println(10.0 / 0);  // Infinity
System.out.println(0.0 / 0);   // NaN
```

**Q125.**

```java
int i = 5;
int r = i++ + ++i; // 5 + 7 = 12
```

**Q126. Does this compile?** `byte b = 10; b = b + 1;` No. `b + 1` is promoted to `int`. Use `b += 1` (implicit cast) or `b = (byte)(b + 1)`.

**Q127. What happens here?**

```java
String s = null;
System.out.println(s + "x"); // prints "nullx"
s.length();                  // NullPointerException
```

**Q128. Order of initialization?** Static variables/blocks (parent first, once at class load), then for each object: parent instance initializers and constructor, then child instance initializers and constructor.

**Q129. Can a constructor call an overridable method safely?** No. The overridden method in the subclass may run before the subclass fields are initialized and see default values.

**Q130. What happens if you call a method on `null` that is static?** It works, because static methods are resolved by the reference's declared type, not the object.

**Q131. Can you catch `StackOverflowError`?** Yes, since it is a Throwable, but you generally shouldn't.

**Q132. `HashMap` allows null key. How many null keys?** Only one (stored in bucket 0). Many null values are allowed.

**Q133. Why is `wait()` defined in Object, not Thread?** Because locks (monitors) belong to objects, so waiting and notification are operations on an object's monitor.

**Q134. Can we have multiple `public` classes in a single file?** No. At most one public top-level class, and the file name must match it.

**Q135. What is the difference between `int` and `Integer`?** `int` is a primitive (cannot be null, faster, no methods). `Integer` is an object wrapper (nullable, usable in generics and collections).

## 12. Last-Minute Checklist

- Be ready to explain **HashMap internals**, **equals/hashCode**, **String immutability and pool**, **JVM memory**, and **thread lifecycle** on a whiteboard. These are asked in almost every interview.
- Know the **time complexity** of common collection operations (ArrayList get O(1), HashMap get/put O(1) average, TreeMap O(log n), LinkedList get O(n)).
- Practice writing: reverse a string, find duplicates, first non-repeating character, producer-consumer with `BlockingQueue`, a thread-safe Singleton, LRU cache with `LinkedHashMap`, and stream-based grouping and sorting.
- If you don't know an answer, say what you do know about the topic and reason aloud. Interviewers value clear thinking more than memorized lines.
- Add a real project example for each major concept (where you used collections, concurrency, exceptions), since follow-up questions often start from your resume.

Good luck with your interviews!
