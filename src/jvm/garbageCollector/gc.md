How Java Garbage Collection Works
---------------------------------

Java garbage collection is an automatic process. The programmer does not need to explicitly mark objects to be deleted. The garbage collection implementation lives in the JVM. Every JVM can implement garbage collection however it pleases. The only requirement is that it should meet the JVM specification. Although there are many JVMs, Oracle's HotSpot is by far the most common. It offers a robust and mature set of garbage collection options.

What are the Various Steps During the Garbage Collection?
---------------------------------------------------------

While HotSpot has multiple garbage collectors that are optimized for various use cases, all its garbage collectors follow the same basic process. In the first step, unreferenced objects are identified and marked as ready for garbage collection. In the second step, marked objects are deleted. Optionally, memory can be compacted after the garbage collector deletes objects, so remaining objects are in a contiguous block at the start of the heap. The compaction process makes it easier to allocate memory to new objects sequentially after the JVM allocates the memory blocks to existing objects.

Garbage Collector is the program running in the background that looks into all the objects in the memory and find out objects that are not referenced by any part of the program. All these unreferenced objects are deleted and space is reclaimed for allocation to other objects.

One of the basic ways of garbage collection involves three steps:

1.  Marking: This is the first step where garbage collector identifies which objects are in use and which ones are not in use.
2.  Normal Deletion: Garbage Collector removes the unused objects and reclaim the free space to be allocated to other objects.
3.  Deletion with Compacting: For better performance, after deleting unused objects, all the survived objects can be moved to be together. This will increase the performance of allocation of memory to newer objects.

There are two problems with a simple mark and delete approach.

1.  First one is that it's not efficient because most of the newly created objects will become unused
2.  Secondly objects that are in-use for multiple garbage collection cycle are most likely to be in-use for future cycles too.

How Generational Garbage Collection Strategy Works
--------------------------------------------------

All of HotSpot's garbage collectors implement a generational garbage collection strategy that categorizes objects by age. The rationale behind generational garbage collection is that most objects are short-lived and will be ready for garbage collection soon after creation.

![Java Garbage Collection Heaps](https://stackify.com/wp-content/uploads/2017/05/Java-Garbage-Collection.png)

*Image via [Wikipedia](https://de.wikipedia.org/wiki/Datei:JavaGCgenerations.png)*

### What are Different Classification of Objects by Garbage Collector?

We can divide the heap into [three sections](https://plumbr.eu/handbook/garbage-collection-in-java):

-   Young Generation: Newly created objects start in the Young Generation. The garbage collector further subdivides Young Generation into an Eden space, where all new objects start, and two Survivor spaces, where it moves objects from Eden after surviving one garbage collection cycle. When objects are garbage collected from the Young Generation, it is a minor garbage collection event.
-   Old Generation: Eventually, the garbage collector moves the long-lived objects from the Young Generation to the Old Generation. When objects are garbage collected from the Old Generation, it is a major garbage collection event.
-   Permanent Generation: The JVM stores the metadata, such as classes and methods, in the Permanent Generation. JVM garbage collects the classes from the Permanent Generation that are no longer in use.

During a full garbage collection event, unused objects from all generations are garbage collected.

### What are Different Types of Garbage Collector?

HotSpot has four garbage collectors:

-   Serial: All garbage collection events are conducted serially in one thread. JVM executes the compaction after each garbage collection.
-   Parallel: JVM uses multiple threads for minor garbage collection. It uses a single thread for major garbage collection and Old Generation compaction. Alternatively, the Parallel Old variant uses multiple threads for major garbage collection and Old Generation compaction.
-   CMS (Concurrent Mark Sweep): Multiple threads are used for minor garbage collection using the same algorithm as Parallel. Major garbage collection is multi-threaded, like Parallel Old. Still CMS runs concurrently alongside application processes to minimize "stop the world" events (i.e., when the garbage collector running stops the application). Here, the JVM does not perform compaction of memory.
-   G1 (Garbage First): The newest garbage collector is intended as a replacement for CMS. It is parallel and concurrent, like CMS. However, it works quite differently under the hood than older garbage collectors.

Benefits of Java Garbage Collection
-----------------------------------

The biggest benefit of Java garbage collection is that it automatically handles the deletion of unused objects or objects that are [out of reach](http://beginnersbook.com/2013/04/java-garbage-collection/) to free up vital memory resources. Programmers working in languages without garbage collection (like C and C++) must implement manual memory management in their code.

Despite the extra work required, some programmers argue in favor of manual memory management over garbage collection, primarily for reasons of control and performance. While the debate over memory management approaches continues to rage on, garbage collection is now a standard component of many popular programming languages. For scenarios in which the garbage collector is negatively impacting performance, Java offers many options for tuning the garbage collector to improve its efficiency.

What Triggers Garbage Collection?
---------------------------------

The Garbage Collection process is triggered by a variety of events that signal to the Garbage Collector that memory needs to be reclaimed.

Here are some common events that trigger Java Garbage Collection:

1.  Allocation Failure: When an object cannot be allocated in the heap because there is not enough contiguous free space available, the JVM triggers the Garbage Collection to free up memory.
2.  Heap Size: When the heap reaches a certain capacity threshold, the JVM triggers Garbage Collection to reclaim memory and prevent an OutOfMemoryError.
3.  System.gc(): Calling the System.gc()  method can trigger Garbage Collection, although it does not guarantee that Garbage Collection will occur.
4.  Time-Based: Some Garbage Collection algorithms, such as G1 Garbage Collection, use time-based triggers to initiate Garbage Collection.

Ways for requesting JVM to run Garbage Collector
------------------------------------------------

There are several ways to request the JVM to run Garbage Collector in a Java application:

### `System.gc()` method:

Calling this method is the most common way to request Garbage Collection in a Java application. However, it does not guarantee that Garbage Collection will occur as it is only a suggestion to the JVM.

### `Runtime.getRuntime().gc()` method:

This method provides another way to request Garbage Collection in a Java application. This method is similar to the `System.gc()` method, and it also suggests that the JVM should run Garbage Collector, but again it does not guarantee that Garbage Collection will occur.

### JConsole or VisualVM:

JConsole or VisualVM is a profiling tool that is included with the Java Development Kit. These tools provide a graphical user interface that allows developers to monitor the memory usage of their Java application in real-time. They also provide a way to request Garbage Collection on-demand by clicking a button.

### Command-Line Options:

The JVM can be configured with various command-line options to control Garbage Collection. For example, the `-Xmx` option can be used to specify the maximum heap size, which can affect the frequency and duration of Garbage Collection events. The `-XX:+DisableExplicitGC` option can be used to disable explicit calls to `System.gc()` or `Runtime.getRuntime().gc()`.

### Heap Dumps:

Heap dumps are snapshots of the Java heap that can be taken at any time during the application's execution. They can be analyzed to identify memory leaks or other memory-related issues. Heap dumps can be requested using command-line options or profiling tools.

It is worth noting that requesting Garbage Collection too frequently can negatively impact the performance of the application. It is important to monitor the memory usage of the application and only request Garbage Collection when it is necessary. By using profiling tools and selecting appropriate Garbage Collection algorithms, developers can ensure that Garbage Collection is triggered in a way that minimizes the impact on the application's performance.

Why Does a Programmer need to Understand Garbage Collection?
------------------------------------------------------------

For many simple applications, Java garbage collection is not something that a programmer needs to consciously consider. However, for programmers who want to advance their Java skills, it is important to understand how Java garbage collection works and the ways in which it can be tuned.

Besides the basic mechanisms of garbage collection, one of the most important points to understand about garbage collection in Java is that it is non-deterministic, and there is no way to predict when garbage collection will occur at run time. It is possible to include a hint in the code to run the garbage collector with the System.gc()  or Runtime.getRuntime().gc()  methods, but they provide no guarantee that the garbage collector will actually run.


# Java Garbage Collection (GC) Object Lifecycle

## JVM Heap Structure

```
                        HEAP
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│                 Young Generation (-Xmn)                      │
│                                                              │
│  ┌────────────┬─────────────┬─────────────┐                  │
│  │   Eden     │ Survivor 0  │ Survivor 1  │                  │
│  │            │    (S0)      │    (S1)      │                  │
│  └────────────┴─────────────┴─────────────┘                  │
│                                                              │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│                Old Generation (Tenured)                      │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

# Object Lifecycle

```
New Object
    │
    ▼
Eden
    │
Minor GC
    ▼
Survivor 0
    │
Minor GC
    ▼
Survivor 1
    │
Minor GC
    ▼
Survivor 0
    │
Minor GC
    ▼
Survivor 1
    │
Age reaches threshold
    ▼
Old (Tenured) Generation
```

---

# Step 1 - Object Creation

Every newly created object is allocated in **Eden**.

```java
Employee e1 = new Employee();
Employee e2 = new Employee();
Employee e3 = new Employee();
```

```
Eden

┌──────────────────────┐
│ e1                   │
│ e2                   │
│ e3                   │
└──────────────────────┘

S0 Empty
S1 Empty
Old Empty
```

---

# Step 2 - Eden Becomes Full

More objects are created.

```
Eden

A
B
C
D
E
F

(Eden Full)
```

A **Minor GC** is triggered.

---

# Step 3 - Minor GC

GC checks which objects are still reachable.

Suppose

```
Alive

B
C
E

Dead

A
D
F
```

Dead objects are discarded.

Only live objects survive.

---

# Step 4 - Surviving Objects Move to Survivor Space

The surviving objects are copied into **Survivor 0**.

```
Eden

Empty

S0

B(age=1)
C(age=1)
E(age=1)

S1

Empty
```

Every surviving object gets

```
Age = 1
```

---

# Step 5 - New Objects Are Created

Application continues.

```
Eden

G
H
I
J
```

---

# Step 6 - Second Minor GC

Suppose

From S0

```
Alive

B
E

Dead

C
```

From Eden

```
Alive

G
H
```

Instead of modifying S0,

JVM copies all surviving objects into **S1**.

```
S1

B(age=2)
E(age=2)
G(age=1)
H(age=1)
```

Then

```
S0

Empty
```

---

# Step 7 - Third Minor GC

Now S1 becomes the source.

Suppose

```
Alive

B
G
```

New Eden objects

```
K
L
```

JVM copies them into S0.

```
S0

B(age=3)
G(age=2)
K(age=1)
L(age=1)
```

S1 becomes empty.

---

# Why Two Survivor Spaces?

## Without Two Survivor Spaces

Suppose Survivor contains

```
A
B
C
```

After GC

```
Alive

A
C

Dead

B

New Objects

D
E
```

Survivor should become

```
A(age2)
C(age2)
D(age1)
E(age1)
```

This requires

- deleting B
- compacting memory
- shifting objects
- inserting new objects

Very expensive.

---

## With Two Survivor Spaces

Current

```
S0

A
B
C
```

Destination

```
S1

Empty
```

GC copies only live objects.

```
S1

A(age2)
C(age2)
D(age1)
E(age1)
```

Now

```
S0

Discard Entirely
```

No deletion.

No shifting.

No compaction.

Simply discard the old Survivor space.

---

# Survivor Space Swap

```
Minor GC #1

Eden
 ↓
S0

------------------------

Minor GC #2

S0 + Eden
 ↓
S1

------------------------

Minor GC #3

S1 + Eden
 ↓
S0

------------------------

Minor GC #4

S0 + Eden
 ↓
S1
```

Only one Survivor space is active at a time.

The other one is the destination.

---

# Object Aging

Every successful Minor GC increases object age.

```
Minor GC 1

A(age=1)

↓

Minor GC 2

A(age=2)

↓

Minor GC 3

A(age=3)

↓

...

↓

Age = 15 (default threshold)

↓

Promote to Old Generation
```

---

# Promotion (Tenuring)

When an object's age reaches the JVM threshold
(default is usually **15**),

it is promoted to the **Old Generation**.

```
Old Generation

A
E
```

This process is called

- Promotion
- Tenuring

---

# Complete GC Flow

```
                    New Object
                         │
                         ▼
                ┌─────────────────┐
                │      Eden       │
                └─────────────────┘
                         │
                 Minor Garbage Collection
                         │
          ┌──────────────┴──────────────┐
          │                             │
          ▼                             ▼
     Object Dead                  Object Alive
          │                             │
          ▼                             ▼
      Collected                 Survivor Space
                                 (S0 or S1)
                                       │
                               Minor GC Again
                                       │
                                 Age Increases
                                       │
                            Age >= Threshold?
                                       │
                    No ─────────────────┘
                                       │
                                      Yes
                                       ▼
                         Old (Tenured) Generation
```

---

# GC Terminology

| Term | Meaning |
|------|----------|
| Eden | Where all new objects are created |
| Minor GC | Garbage collection of Young Generation |
| Survivor Space | Temporary storage for surviving objects |
| Age | Number of Minor GCs survived |
| Promotion | Moving object to Old Generation |
| Tenuring | Another name for Promotion |
| Old Generation | Stores long-lived objects |
| Major GC / Full GC | Cleans Old Generation |

---

# Memory Movement Summary

```
New Object
      │
      ▼
   Eden
      │
Minor GC
      │
      ▼
 Survivor 0
      │
Minor GC
      │
      ▼
 Survivor 1
      │
Minor GC
      │
      ▼
 Survivor 0
      │
Minor GC
      │
      ▼
 Survivor 1
      │
Age >= Threshold
      │
      ▼
Old Generation
```

---

# Why Two Survivor Spaces?

✅ Copy only live objects

✅ Avoid deleting dead objects individually

✅ No memory compaction needed

✅ Faster Minor Garbage Collection

One Survivor space is always the **Source**.

The other Survivor space is always the **Destination**.

After every Minor GC they swap roles.

---

# Interview One-Liner

> Every new object is created in **Eden**. When Eden fills up, a **Minor GC** occurs. Live objects are copied to one Survivor space while dead objects are discarded. During subsequent Minor GCs, live objects are copied back and forth between the two Survivor spaces, increasing their age each time. Once an object survives enough Minor GCs (typically age 15), it is **promoted (tenured)** to the **Old Generation**. Two Survivor spaces are used so that the JVM can copy only live objects into an empty space instead of deleting dead objects and compacting memory, making Minor GC efficient.