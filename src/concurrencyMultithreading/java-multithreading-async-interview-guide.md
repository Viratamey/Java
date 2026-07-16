# Java Multithreading & Async Programming — Interview Guide

Built for senior/lead-level interviews (FAANG, fintech, investment banks). Given your Spring Boot + Kafka + payments background, examples lean toward realistic backend scenarios where relevant.

---

## 1. Core Fundamentals

### Process vs Thread
- **Process**: independent memory space, own resources. Heavyweight.
- **Thread**: lightweight, shares heap/memory with other threads in the same process, has its own stack, program counter, and local variables.

### Creating Threads
```java
// 1. Extend Thread
class Worker extends Thread {
    public void run() { System.out.println("running"); }
}

// 2. Implement Runnable (preferred — allows extending other classes, better for DI/Spring)
class Worker implements Runnable {
    public void run() { System.out.println("running"); }
}
new Thread(new Worker()).start();

// 3. Callable (returns a value, can throw checked exceptions)
Callable<Integer> task = () -> 42;

// 4. Lambda (Runnable is a functional interface)
new Thread(() -> System.out.println("running")).start();
```
**Interview trap:** calling `run()` directly executes on the *current* thread — no new thread is spawned. Only `start()` creates a new thread.

### Thread Lifecycle
```
NEW → RUNNABLE → (BLOCKED / WAITING / TIMED_WAITING) → TERMINATED
```
- **BLOCKED**: waiting to acquire a monitor lock (e.g., blocked on `synchronized`).
- **WAITING**: waiting indefinitely for another thread's signal (`Object.wait()`, `Thread.join()`, `LockSupport.park()`).
- **TIMED_WAITING**: same as above but with a timeout (`sleep()`, `wait(timeout)`).

Common gotcha: `sleep()` does **not** release the lock; `wait()` **does** release the lock it's called on.

---

## 2. Synchronization Fundamentals

### `synchronized`
- Intrinsic lock (monitor) — one per object.
- **Method-level**: locks on `this` (instance) or the `Class` object (static method).
- **Block-level**: locks on a specified object — preferred, since it limits lock scope and lets you choose the lock granularity.

```java
public class Counter {
    private int count = 0;
    private final Object lock = new Object();

    public void increment() {
        synchronized (lock) { count++; }
    }
}
```

**Why prefer a private final lock object over `synchronized` on `this`?**
Locking on `this` exposes your lock to external code — anyone holding a reference to your object can synchronize on it too, causing unexpected contention or deadlock. A private lock object encapsulates locking as an implementation detail.



### Deep Dive: How `synchronized` Works (Revision Notes)

#### Why Synchronization?
When multiple threads access shared mutable data simultaneously, **race conditions** can occur.

Example:
```java
count++;
```

Although it looks like one statement, it is actually three operations:

```
1. Read count
2. Increment value
3. Write updated value
```

If two threads interleave these steps, one update can overwrite the other (**lost update**).

---

#### What does `synchronized` do?

`synchronized` guarantees that **only one thread at a time** can execute the protected critical section for a given lock.

```java
public synchronized void increment() {
    count++;
}
```

If one thread is inside `increment()`, every other thread attempting to acquire the same lock must wait.

---

#### Intrinsic Lock (Monitor)

Every Java object automatically contains an **intrinsic lock (monitor)**.

```
Counter Object
--------------
count = 0
Monitor (Lock)
--------------
```

The monitor is invisible to programmers but is automatically used by the `synchronized` keyword.

Whenever Java executes:

```java
synchronized(obj) {
        // critical section
        }
```

it performs:

```
Acquire obj's monitor
↓
Execute critical section
↓
Release monitor
```

Only one thread can own a monitor at any given time.

---

#### Instance Method Synchronization

```java
public synchronized void increment() {
    count++;
}
```

is exactly equivalent to:

```java
public void increment() {
    synchronized (this) {
        count++;
    }
}
```

The monitor of the current object (`this`) is used.

---

#### Static Method Synchronization

```java
public static synchronized void print() {
}
```

is equivalent to:

```java
public static void print() {
    synchronized (Counter.class) {
    }
}
```

Static synchronized methods lock the **Class object**, not any instance.

---

#### Instance Lock vs Class Lock

```java
Counter c1 = new Counter();
Counter c2 = new Counter();
```

- `c1.increment()` locks only `c1`
- `c2.increment()` locks only `c2`
- Both can execute simultaneously because they use different monitors.

Static synchronized methods always lock `Counter.class`, so only one thread across the JVM can execute that static synchronized method at a time.

---

#### Why Prefer Block-Level Synchronization?

Instead of locking an entire method:

```java
public synchronized void process() {
    slowTask();
    count++;
    print();
}
```

prefer locking only the shared state:

```java
public void process() {
    slowTask();

    synchronized (lock) {
        count++;
    }

    print();
}
```

Benefits:

- Smaller critical section
- Less contention
- Better scalability
- Better lock granularity

---

#### Why Use a `private final` Lock Object?

```java
private final Object lock = new Object();

public void increment() {
    synchronized (lock) {
        count++;
    }
}
```

instead of

```java
synchronized (this) {
count++;
        }
```

**Reason:** `this` is visible to external code.

Someone else could accidentally write:

```java
synchronized(counter) {
    Thread.sleep(10000);
}
```

If your implementation also synchronizes on `this`, your methods become blocked by external code.

A private lock object hides the locking mechanism and prevents outside interference.

---

#### Why `final`?

```java
private final Object lock = new Object();
```

`final` guarantees that the lock reference never changes.

If the lock object were reassigned, different threads could synchronize on different objects, breaking thread safety.

---

#### Quick Revision Summary

| Concept | Lock Used |
|----------|-----------|
| `synchronized(this)` | Current object's monitor |
| `synchronized(lock)` | Custom lock object's monitor |
| `synchronized(obj)` | Specified object's monitor |
| Instance synchronized method | `this` |
| Static synchronized method | `ClassName.class` |
| Intrinsic lock | One hidden monitor per object (and per `Class` object) |


### `volatile`
- Guarantees **visibility** (writes are immediately visible to other threads) and prevents instruction reordering around the variable (happens-before).
- Does **NOT** guarantee atomicity. `volatile int counter; counter++;` is still a race condition (read-modify-write is 3 separate operations).
- Use for flags (`volatile boolean running`), or double-checked locking singleton pattern.

```java
class Singleton {
    private static volatile Singleton instance;
    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton(); // volatile prevents reordering of partially-constructed object being published
                }
            }
        }
        return instance;
    }
}
```
**Interview favorite:** explain *why* `volatile` is needed here — without it, the JIT/JVM could reorder the object construction and reference assignment, so another thread might see a non-null but not-fully-initialized instance.



### Deep Dive: Why `volatile` is Required in Double-Checked Locking (Interview Notes)

The classic singleton implementation uses **Double-Checked Locking (DCL)**:

```java
class Singleton {
    private static volatile Singleton instance;

    public static Singleton getInstance() {

        if (instance == null) {                 // First check (outside synchronization)

            synchronized (Singleton.class) {

                if (instance == null) {         // Second check (inside synchronization)
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

#### Why not synchronize the whole method?

```java
public static synchronized Singleton getInstance() {
    if (instance == null) {
        instance = new Singleton();
    }
    return instance;
}
```

This is thread-safe, but **every call acquires the lock**, even after the singleton has already been created.

Double-Checked Locking avoids this overhead by synchronizing only during the first initialization.

---

#### Why is `new Singleton()` not atomic?

The statement

```java
instance = new Singleton();
```

is conceptually three operations:

```
1. Allocate memory
2. Initialize the object (run constructor)
3. Assign the reference to instance
```

For optimization, the JVM/CPU may reorder steps 2 and 3:

```
1. Allocate memory
2. Assign reference to instance
3. Run constructor
```

Now `instance` becomes non-null **before the constructor finishes**.

---

#### If object creation is synchronized, how can another thread see a partial object?

This is the most common interview question.

The key observation is that **the first null check is outside the synchronized block.**

Timeline:

```
Thread A
---------
instance == null
↓
Enters synchronized block
↓
Allocates memory
↓
instance now points to allocated memory
↓
Constructor still running...

Thread B
---------
Calls getInstance()
↓
if (instance == null)
↓
FALSE
↓
Returns instance immediately
```

Notice that **Thread B never enters the synchronized block** because it already sees a non-null reference.

It therefore never waits for Thread A to finish initialization.

As a result, Thread B can observe a **partially initialized object**.

---

#### How does `volatile` fix this?

`volatile` provides two guarantees:

1. **Visibility** – writes made by one thread become immediately visible to other threads.
2. **Prevents instruction reordering** for volatile reads/writes.

With `volatile`, the JVM must execute:

```
Allocate memory
↓
Run constructor
↓
Publish reference (volatile write)
```

The reference cannot become visible until the object is fully initialized.

---

#### Why doesn't `synchronized` alone solve the problem?

`synchronized` protects only the code inside the synchronized block.

After the singleton has been created, future threads execute:

```java
if (instance != null) {
    return instance;
}
```

They never acquire the lock.

Without `volatile`, this unsynchronized read may observe a reference published before construction completed because of instruction reordering.

Therefore:

- `synchronized` ensures **only one thread creates the object**.
- `volatile` ensures **every thread sees a fully initialized object**.

---

#### Interview One-Liner

> Double-Checked Locking requires `volatile` because the first null check occurs outside the synchronized block. Without `volatile`, the JVM may publish the object reference before the constructor finishes, allowing another thread to skip synchronization and observe a partially initialized object.




### Deep Dive: Other Uses of `volatile` (Revision Notes)

Many developers associate `volatile` only with the Double-Checked Locking singleton pattern. While that is a common interview topic, **its primary purpose is to guarantee visibility between threads.**

#### 1. Visibility Guarantee (Primary Purpose)

Without `volatile`, each thread may cache a variable locally instead of reading it from main memory.

Example:

```java
class Server {

    private boolean running = true;

    public void start() {
        while (running) {
            processRequest();
        }
    }

    public void stop() {
        running = false;
    }
}
```

Thread A may cache `running = true` and continue looping forever, even after Thread B executes:

```java
running = false;
```

Making the variable volatile fixes the problem:

```java
private volatile boolean running = true;
```

Now every write is immediately visible to other threads.

---

#### CPU Cache Illustration

Without `volatile`

```
           Main Memory
                |
      ---------------------
      |                   |
 CPU Cache A         CPU Cache B
      |                   |
 Thread A            Thread B

Thread A keeps reading cached value.
```

With `volatile`

```
Every write
        ↓
Main Memory

Every read
        ↓
Latest value from main memory
```

This guarantees **visibility**.

---

#### 2. Happens-Before Guarantee

A write to a volatile variable establishes a **happens-before** relationship with every subsequent read of that variable.

Example:

```java
int data = 0;
volatile boolean ready = false;

// Thread A
data = 100;
ready = true;

// Thread B
if (ready) {
    System.out.println(data);
}
```

If Thread B sees:

```java
ready == true
```

it is guaranteed to also see:

```java
data == 100
```

The write to `ready` makes all previous writes visible.

---

#### 3. Prevents Instruction Reordering

`volatile` also prevents the JVM/CPU from reordering instructions around volatile reads and writes.

This is why Double-Checked Locking works correctly when the singleton instance is declared volatile.

---

#### What `volatile` Does NOT Do

`volatile` does **not** make compound operations atomic.

Example:

```java
volatile int counter = 0;

counter++;
```

Internally:

```
Read
↓
Increment
↓
Write
```

Two threads can still interleave these operations, causing lost updates.

Use `AtomicInteger`, `synchronized`, or `ReentrantLock` for atomic updates.

---

#### When Should You Use `volatile`?

Use `volatile` when:

- A variable is written by one thread and read by many others.
- You need immediate visibility of updates.
- Operations are simple reads/writes (not read-modify-write).

Common examples:

```java
volatile boolean running;
volatile boolean shutdown;
volatile boolean cancelled;
volatile int status;
```

---

#### When Should You NOT Use `volatile`?

Do **not** rely on `volatile` for compound operations like:

```java
counter++;
balance += amount;
list.add(item);
map.put(key, value);
```

These require synchronization or atomic classes.

---

#### Quick Revision Summary

| Feature | `volatile` |
|---------|------------|
| Visibility | ✅ Yes |
| Happens-before guarantee | ✅ Yes |
| Prevents instruction reordering | ✅ Yes |
| Atomicity | ❌ No |
| Mutual exclusion | ❌ No |
| Locking | ❌ No |

**Interview One-Liner**

> The primary purpose of `volatile` is visibility. It ensures that writes made by one thread become immediately visible to other threads and establishes a happens-before relationship. It also prevents instruction reordering around the volatile variable, but it does not provide atomicity or locking.


### `synchronized` vs `volatile` vs `Lock`
| | synchronized | volatile | ReentrantLock |
|---|---|---|---|
| Atomicity | Yes | No | Yes |
| Visibility | Yes | Yes | Yes |
| Blocking | Yes | No | Yes |
| Interruptible wait | No | N/A | Yes (`lockInterruptibly`) |
| Try-lock / timeout | No | N/A | Yes (`tryLock`) |
| Fairness option | No | N/A | Yes |
| Condition variables | one (wait/notify) | N/A | multiple (`newCondition()`) |

---

## 3. java.util.concurrent.locks

```java
private final ReentrantLock lock = new ReentrantLock();

public void doWork() {
    lock.lock();
    try {
        // critical section
    } finally {
        lock.unlock(); // ALWAYS in finally
    }
}
```

- **ReentrantLock**: same thread can re-acquire it (reentrant), tracks hold count.
- **ReentrantReadWriteLock**: multiple readers OR one writer. Good for read-heavy caches.
- **StampedLock** (Java 8+): adds an *optimistic read* mode — faster than `ReadWriteLock` for read-mostly workloads, but non-reentrant and trickier to use correctly.

```java
StampedLock sl = new StampedLock();
long stamp = sl.tryOptimisticRead();
int value = data; // read without locking
if (!sl.validate(stamp)) {
    stamp = sl.readLock(); // fall back to real lock
    try { value = data; } finally { sl.unlockRead(stamp); }
}
```

---

### Deep Dive: `ReentrantLock` (Revision Notes)

#### Why does `ReentrantLock` exist when `synchronized` already provides mutual exclusion?

`synchronized` is simple but rigid:
- No way to **try** for a lock without blocking forever.
- No way to **time out** while waiting.
- No way to **interrupt** a thread that's stuck waiting for the lock.
- No way to inspect whether the lock is held, or by how many threads are waiting.
- Only one implicit condition per monitor (`wait`/`notify`).

`ReentrantLock` is an explicit, object-based lock that solves all of the above, at the cost of the programmer having to manage lock/unlock manually (no automatic release on block exit).

#### Basic usage — the mandatory pattern

```java
private final ReentrantLock lock = new ReentrantLock();

public void updateBalance() {
    lock.lock();              // acquire BEFORE the try block
    try {
        // critical section — same guarantees as synchronized
        balance = balance + 100;
    } finally {
        lock.unlock();         // MUST be in finally — otherwise a thrown
    }                          // exception leaves the lock held forever
}
```

**Interview trap:** `lock.lock()` must be called *before* the `try` block, not inside it. If `lock()` itself were inside the `try` and it threw before acquiring (rare, but conceptually), the `finally` would call `unlock()` on a lock the thread never acquired — throwing `IllegalMonitorStateException`. Always: `lock(); try { ... } finally { unlock(); }`.

#### What does "reentrant" actually mean?

The **same thread** can acquire the same lock multiple times without deadlocking itself. The lock keeps an internal **hold count**, incremented on every `lock()` call by the owning thread and decremented on every `unlock()`. The lock is only truly released when the hold count reaches zero.

```java
class Vault {
    private final ReentrantLock lock = new ReentrantLock();

    public void outer() {
        lock.lock();           // hold count = 1
        try {
            inner();            // same thread re-enters
        } finally {
            lock.unlock();      // hold count = 0
        }
    }

    public void inner() {
        lock.lock();           // hold count = 2 (same thread, no deadlock)
        try {
            // ...
        } finally {
            lock.unlock();      // hold count = 1
        }
    }
}
```
Without reentrancy, `outer()` calling `inner()` (which locks the same lock) would deadlock the thread against itself. `synchronized` is also reentrant for the same reason — this is expected, standard Java locking behavior, not a special feature.

**Rule to remember:** every `lock()` must be matched by exactly one `unlock()` — if you lock twice, you must unlock twice, or the lock never becomes free for other threads.

#### `tryLock()` — non-blocking acquisition

```java
if (lock.tryLock()) {
    try {
        // got the lock immediately — proceed
    } finally {
        lock.unlock();
    }
} else {
    // didn't get the lock — do something else instead of blocking
    log.warn("resource busy, skipping");
}
```
Useful when blocking indefinitely is unacceptable — e.g., a background cleanup task that should skip its run rather than queue up behind a slow one.

#### `tryLock(timeout, unit)` — bounded wait

```java
try {
    if (lock.tryLock(2, TimeUnit.SECONDS)) {
        try {
            // proceed
        } finally {
            lock.unlock();
        }
    } else {
        // timed out — avoid unbounded thread pile-up, fail fast
        throw new TimeoutException("could not acquire lock in time");
    }
} catch (InterruptedException e) {
    Thread.currentThread().interrupt(); // restore interrupt status
}
```
This is a genuinely valuable production pattern in payment/order-processing systems: instead of a request thread blocking forever behind lock contention (which can cascade into thread-pool exhaustion under load), you fail fast with a clear timeout and let the caller retry or return a 5xx/backpressure response.

#### `lockInterruptibly()` — cancellable waiting

```java
public void process() throws InterruptedException {
    lock.lockInterruptibly(); // can be interrupted while WAITING for the lock
    try {
        // ...
    } finally {
        lock.unlock();
    }
}
```
With `synchronized`, a thread blocked waiting to enter a monitor **cannot** be interrupted — it just sits there until it gets the lock. `lockInterruptibly()` lets another thread call `.interrupt()` on the waiting thread to cancel the wait, throwing `InterruptedException` instead of acquiring the lock. Useful for responsive cancellation (e.g., a task cancelled because a user aborted the request, or a shutdown sequence that needs to unblock stuck threads).

#### Fairness

```java
ReentrantLock fairLock = new ReentrantLock(true); // fair mode
```
- **Unfair (default, `new ReentrantLock()`)**: a thread that just requested the lock can "barge" ahead of threads that have been waiting longer, if the lock happens to be free at that instant. Higher throughput, but risks **starvation** of long-waiting threads under sustained contention.
- **Fair (`new ReentrantLock(true)`)**: threads acquire the lock strictly in the order they requested it (FIFO via an internal queue). No starvation, but noticeably lower throughput due to context-switch/queueing overhead — every thread has to be woken up and check its turn, even when the lock is otherwise free.

**Interview one-liner:** "Fairness in `ReentrantLock` trades throughput for starvation-freedom — default to unfair unless you have evidence of a real starvation problem."

#### `Condition` — multiple wait-sets per lock

With `synchronized`, `wait()`/`notify()` operate on a single implicit condition per monitor. `ReentrantLock` lets you create **multiple independent conditions**, which is exactly what you need for producer-consumer where "not full" and "not empty" are logically different waits.

```java
class BoundedBuffer<T> {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;

    BoundedBuffer(int capacity) { this.capacity = capacity; }

    public void put(T item) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == capacity) {
                notFull.await();          // releases lock, waits on THIS condition only
            }
            queue.add(item);
            notEmpty.signal();            // wakes only a thread waiting on notEmpty
        } finally {
            lock.unlock();
        }
    }

    public T take() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                notEmpty.await();
            }
            T item = queue.poll();
            notFull.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }
}
```
Compare this to the `synchronized`/`wait`/`notifyAll` version from Section 5: with a single monitor, `notifyAll()` wakes up **every** waiter (producers and consumers alike), and each has to re-check its own condition — wasteful when the two groups are waiting for opposite things. With separate `Condition` objects, `notFull.signal()` wakes only a producer, and `notEmpty.signal()` wakes only a consumer — much less unnecessary wake-and-recheck churn under high contention.

#### Introspection methods (not available with `synchronized`)

```java
lock.isLocked();          // is anyone currently holding it?
lock.isHeldByCurrentThread(); // does the calling thread hold it?
lock.getHoldCount();      // how many times has the current thread re-entered?
lock.getQueueLength();    // estimate of threads waiting to acquire
```
Handy for diagnostics/metrics/logging in production systems — e.g., exposing lock contention as a monitoring metric, something you simply cannot do with implicit monitors.

#### Quick Revision Summary — `ReentrantLock`

| Feature | `synchronized` | `ReentrantLock` |
|---|---|---|
| Automatic release on block exit | Yes | No — must call `unlock()` manually |
| Try without blocking | No | `tryLock()` |
| Bounded wait | No | `tryLock(timeout, unit)` |
| Interruptible wait | No | `lockInterruptibly()` |
| Fairness option | No | `new ReentrantLock(true)` |
| Multiple condition queues | No (one wait-set) | Yes (`newCondition()`, as many as needed) |
| Reentrant | Yes | Yes |
| Introspection (`isLocked`, hold count, queue length) | No | Yes |

---

### Deep Dive: `ReentrantReadWriteLock` (Revision Notes)

#### The problem it solves

A plain `ReentrantLock` or `synchronized` block allows only **one thread at a time**, period — even if ten threads only want to *read* shared data and none of them are writing. For read-heavy workloads (e.g., a cache, a config object, reference/lookup data refreshed occasionally but read constantly), that's needless serialization of reads that don't actually conflict with each other.

`ReentrantReadWriteLock` splits locking into two cooperating locks backed by the same underlying state:
- **Read lock**: can be held by **multiple threads simultaneously**, as long as no thread holds the write lock.
- **Write lock**: **exclusive** — only one thread, and no readers, can hold it at the same time.

```
Compatibility matrix:
             Read lock held   Write lock held
Read lock:   ALLOWED           BLOCKED
Write lock:  BLOCKED           BLOCKED
```

#### Basic usage

```java
class Cache<K, V> {
    private final Map<K, V> map = new HashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    public V get(K key) {
        readLock.lock();
        try {
            return map.get(key);          // multiple threads can be in here concurrently
        } finally {
            readLock.unlock();
        }
    }

    public void put(K key, V value) {
        writeLock.lock();
        try {
            map.put(key, value);          // exclusive — no readers or other writers allowed in
        } finally {
            writeLock.unlock();
        }
    }
}
```
Notice both `readLock()` and `writeLock()` return a plain `Lock` — same `lock()`/`unlock()`/`tryLock()`/`lockInterruptibly()` API as `ReentrantLock`, just backed by shared read/write semantics.

#### Why is plain `HashMap` unsafe here, and why not just use `ConcurrentHashMap` instead?

This is a very common interview follow-up. `ConcurrentHashMap` is usually the *better* choice for a simple key-value cache — it's purpose-built, more finely-grained, and battle-tested. `ReentrantReadWriteLock` earns its place when:
- You need to protect a **multi-step compound operation** across several data structures atomically as one read or one write (e.g., reading two related maps consistently together — `ConcurrentHashMap`'s atomicity only covers single-map single-operation atomicity).
- You're protecting a custom data structure that isn't a simple map (e.g., a read-heavy in-memory index, graph, or cache with derived/computed fields that must stay consistent as a unit).

**Interview one-liner:** "`ConcurrentHashMap` gives you atomicity per-operation on a single map; `ReadWriteLock` gives you atomicity across an arbitrary block of code involving any shared state — reach for `ReadWriteLock` when your invariant spans more than what a single concurrent collection method call can atomically guarantee."

#### Reentrancy details

- The **write lock is reentrant** for the write-holding thread, same as `ReentrantLock`.
- The **read lock is reentrant** too, for a thread that already holds the read lock.
- **Lock downgrading is supported**: a thread holding the write lock can acquire the read lock *before* releasing the write lock, then release the write lock — this "downgrades" to a read lock without ever allowing another writer to sneak in between.

```java
public void updateAndRead() {
    writeLock.lock();
    try {
        data = recompute();     // mutate under write lock
        readLock.lock();        // acquire read lock WHILE still holding write lock (downgrade)
        try {
            writeLock.unlock(); // safe to release write lock now — we already hold read lock
            return data;         // continue reading with only the read lock held
        } finally {
            readLock.unlock();
        }
    } finally {
        // no-op if already unlocked above; shown for illustration only
    }
}
```
**Lock upgrading is NOT supported**: a thread holding only the read lock cannot acquire the write lock while still holding the read — attempting to do so will deadlock the thread against itself, because the write lock will never become available while any read lock (including its own) is held. If you need to go from reading to writing, you must fully release the read lock first, then acquire the write lock (accepting that another writer could get in between — re-check your condition after acquiring).

#### Fairness and starvation considerations

```java
new ReentrantReadWriteLock(true); // fair mode
```
- **Unfair (default)**: better throughput; a write request can be repeatedly overtaken by a steady stream of new readers if reads keep arriving — a classic **writer starvation** risk in read-heavy, high-throughput systems.
- **Fair mode**: readers arriving after a waiting writer will queue behind that writer rather than jump ahead of it — prevents writer starvation, at some cost to raw read throughput.

**Interview flag:** if asked "can a `ReentrantReadWriteLock` starve a writer?" — yes, in unfair mode under sustained read pressure, which is exactly the scenario `StampedLock`'s optimistic reads were later designed to help with differently (see below), and exactly why fair mode exists as a mitigation.

#### When to reach for it vs plain `ReentrantLock`

Use `ReentrantReadWriteLock` when reads significantly outnumber writes **and** the critical section does real work (not just a single primitive read) — the locking overhead of `ReadWriteLock` itself is slightly higher than a plain lock, so for trivial critical sections on rarely-contended data, a plain `ReentrantLock` or even `synchronized` can outperform it. Always the answer to "it depends, benchmark it" if pushed on exact thresholds — but the conceptual trigger is **read:write ratio + criticality/cost of the shared computation**.

#### Quick Revision Summary — `ReentrantReadWriteLock`

| Aspect | Behavior |
|---|---|
| Multiple readers | Allowed concurrently |
| Reader + writer together | Not allowed |
| Multiple writers | Not allowed (exclusive) |
| Read lock reentrant | Yes |
| Write lock reentrant | Yes |
| Write → read (downgrade) | Allowed |
| Read → write (upgrade) | **Not allowed** — deadlocks if attempted |
| Fairness option | Yes — prevents writer starvation, costs throughput |

---

### Deep Dive: `StampedLock` (Revision Notes)

#### Why does `StampedLock` exist when `ReentrantReadWriteLock` already supports concurrent readers?

`ReentrantReadWriteLock`'s read lock is still a **real lock** — every reader has to perform an actual acquire/release (with associated memory synchronization and, under contention, thread park/unpark overhead). For workloads that are *overwhelmingly* reads with only occasional writes, even that read-lock bookkeeping is measurable overhead at high throughput.

`StampedLock` (Java 8+) introduces a third mode — **optimistic reading** — that involves **no locking at all** for the common case: a reader just reads the data and then checks afterward whether a write happened concurrently. If nothing changed, the reader is done, with none of the acquire/release cost of a real lock.

#### The three modes

```java
StampedLock sl = new StampedLock();

// 1. Write lock — exclusive, same idea as ReentrantReadWriteLock's write lock
long writeStamp = sl.writeLock();
try {
    x = 10; y = 20;
} finally {
    sl.unlockWrite(writeStamp);
}

// 2. Pessimistic read lock — like a normal shared read lock (blocks writers)
long readStamp = sl.readLock();
try {
    int a = x, b = y;
} finally {
    sl.unlockRead(readStamp);
}

// 3. Optimistic read — NO locking, just a version check
long stamp = sl.tryOptimisticRead();   // returns a "stamp" (version number), doesn't block anyone
int a = x, b = y;                       // read shared data WITHOUT holding any lock
if (!sl.validate(stamp)) {              // did a write happen since we got the stamp?
    // yes — our read may be inconsistent, fall back to a real read lock
    stamp = sl.readLock();
    try {
        a = x; b = y;
    } finally {
        sl.unlockRead(stamp);
    }
}
// use a, b — now guaranteed consistent
```

#### How does the "stamp" actually work?

Every successful `writeLock()` (and its `unlockWrite()`) bumps an internal version/sequence number. `tryOptimisticRead()` just snapshots the current version and immediately returns — it never blocks and never prevents a writer from proceeding. After the reader finishes reading the shared fields, it calls `validate(stamp)`, which checks: *"has the version changed since I took my snapshot, or is a write currently in progress?"* If the version is unchanged, the reader's data is guaranteed consistent — no write squeezed in between the read operations. If the version changed, the optimistic read result must be discarded and treated as if the read never validly happened — retry with a real (pessimistic) `readLock()`.

**This is the crux interview point:** `tryOptimisticRead()` does not actually protect your read — you must re-verify with `validate()` after reading, and be ready to fall back. Skipping the `validate()` call defeats the entire mechanism and silently reintroduces race conditions.

#### Full realistic example — a coordinate holder

```java
class Point {
    private double x, y;
    private final StampedLock sl = new StampedLock();

    void move(double deltaX, double deltaY) {
        long stamp = sl.writeLock();
        try {
            x += deltaX;
            y += deltaY;
        } finally {
            sl.unlockWrite(stamp);
        }
    }

    double distanceFromOrigin() {
        long stamp = sl.tryOptimisticRead();
        double currentX = x, currentY = y;      // optimistic, unlocked read
        if (!sl.validate(stamp)) {              // a write happened concurrently — retry safely
            stamp = sl.readLock();
            try {
                currentX = x;
                currentY = y;
            } finally {
                sl.unlockRead(stamp);
            }
        }
        return Math.sqrt(currentX * currentX + currentY * currentY);
    }
}
```
Under low write contention, `distanceFromOrigin()` almost never has to fall back to `readLock()` — most calls complete with zero locking overhead at all, which is the entire performance win over `ReentrantReadWriteLock`.

#### Why is `StampedLock` non-reentrant, and why does that matter?

Unlike `ReentrantLock`/`ReentrantReadWriteLock`, **`StampedLock` is NOT reentrant**. If a thread already holding the write lock calls `writeLock()` again (even indirectly, through a nested method call), it will **deadlock itself** — there's no hold-count tracking.

```java
// DEADLOCK-PRONE — do not do this
void outer() {
    long stamp = sl.writeLock();
    try {
        inner(); // if inner() also calls sl.writeLock(), the thread deadlocks against itself
    } finally {
        sl.unlockWrite(stamp);
    }
}
```
**Interview flag:** always mention this trade-off — `StampedLock` gives you better throughput than `ReadWriteLock` for read-heavy workloads, but you give up reentrancy, so it's riskier in codebases with nested calls, recursive methods, or any code path where you can't easily guarantee a lock is never re-acquired by the same thread.

#### Other important caveats

- **Not compatible with `Condition`** — `StampedLock` has no `newCondition()` equivalent for the read/write modes.
- **No producer-consumer-style wait/notify support** — if you need that, use `ReentrantLock` + `Condition`, or a `BlockingQueue`.
- **The stamp returned by `writeLock()`/`readLock()` must be kept and passed back into the matching `unlockWrite()`/`unlockRead()`** — unlike `ReentrantLock`, there's no ambient "current lock state," so you must always thread the `long` stamp value through your method correctly.
- **Interruption**: `StampedLock` does not implement the standard `Lock` interface, so `lockInterruptibly()` isn't available in the same form; separate interruptible variants (`readLockInterruptibly()`, `writeLockInterruptibly()`) exist if needed.
- Conversion methods exist — `tryConvertToWriteLock(stamp)`, `tryConvertToReadLock(stamp)` — to attempt upgrading/downgrading without fully releasing and reacquiring, but these can fail (return `0`) and require careful handling.

#### `ReentrantReadWriteLock` vs `StampedLock` — the interview comparison table

| Aspect | `ReentrantReadWriteLock` | `StampedLock` |
|---|---|---|
| Reentrant | Yes (both read & write) | **No** — self-deadlock risk on re-entry |
| Optimistic (lock-free) read mode | No | **Yes** (`tryOptimisticRead` + `validate`) |
| Best for | Read-heavy, moderate contention, need reentrancy | Read-*dominant*, very high throughput, no recursive/nested locking |
| `Condition` support | Yes | No |
| API style | Standard `Lock` interface (`lock()`/`unlock()`) | Stamp-based (`long` stamp must be tracked and passed back) |
| Writer starvation risk | Yes in unfair mode (mitigated by fair mode) | Writers still exclusive/blocking — optimistic readers don't starve writers, but readers must handle retries |
| Complexity to use correctly | Lower — familiar lock/unlock pattern | Higher — must remember to `validate()`, non-reentrant traps |

#### Interview one-liner to tie all three together

> "`ReentrantLock` gives you a flexible exclusive lock with timeouts, interruptibility, and multiple conditions. `ReentrantReadWriteLock` extends that idea to let concurrent readers coexist, at the cost of possible writer starvation. `StampedLock` pushes further for read-dominant workloads by adding a lock-free optimistic read mode — at the cost of giving up reentrancy and condition support, so it needs more careful, disciplined usage."

---

## 4. Atomic Variables & CAS

`java.util.concurrent.atomic` — lock-free thread safety using **Compare-And-Swap (CAS)** at the hardware level.

```java
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet();
counter.compareAndSet(5, 10); // set to 10 only if current value is 5
```

### Why do we even need atomics? (`count++` is not one instruction)

```java
class Counter {
    int count = 0;
    void increment() { count++; }
}
```
`count++` looks like a single step but is actually **three operations**: read → add 1 → write back. If two threads both read `count = 5` before either writes back, both compute `6` and one increment is silently lost — a **race condition** (`5 → +1 → +1` should give `7`, but you get `6`).

**Traditional fix — `synchronized`:** works, but every blocked thread has to be parked and later woken by the OS scheduler — a real context-switch cost (save registers → switch to kernel → restore the next thread → resume). Under heavy contention with many threads queued on one lock, this adds up fast.

### What CAS does instead

Rather than blocking other threads out, CAS asks the CPU to make a conditional update in one atomic hardware step:

> *"Change this memory location to the new value, but only if it still holds the value I expect. If someone else already changed it, tell me it failed — don't touch anything."*

```
if (memory == expectedValue) {
    memory = newValue;
    return true;
}
return false;
```
This whole check-and-swap is a **single atomic CPU instruction** (`cmpxchg` on x86) — no other core can observe a half-done state, so no lock is needed.

**How `incrementAndGet()` actually works internally** — a retry loop around CAS:
```java
do {
    current = get();          // read current value
    next = current + 1;       // compute new value
} while (!compareAndSet(current, next)); // retry if someone else changed it first
return next;
```
If another thread updated the value between the read and the CAS, `compareAndSet` fails (returns `false`) and the loop simply retries with the latest value — no blocking, no OS involvement.

### Lock-free ≠ retry-free (contention still costs something)

Under low contention, CAS almost always succeeds on the first try — very fast. Under **high contention** (hundreds of threads hammering one `AtomicInteger`), most CAS attempts fail and immediately retry (**spinning**) — the CPU burns cycles retrying instead of doing useful work, and throughput can degrade.

**`LongAdder` fixes this** by splitting one counter into multiple internal **cells**. Different threads update different cells (much less collision), and the total is only summed on demand:
```java
LongAdder hits = new LongAdder();
hits.increment();      // goes to one of several internal cells
long total = hits.sum(); // sums all cells — use this only when you need the total
```
Use `AtomicLong`/`AtomicInteger` for low-to-moderate contention or when you need the value on every read; use `LongAdder` for write-heavy counters (metrics, hit counts) updated by many threads, where you rarely need the exact running total on every single update.

### The ABA problem

CAS only compares the **current value** to the expected value — it can't tell if the value changed and then changed back before your CAS ran:
```
Thread 1 reads:      top = A
Thread 2 does:        A → B → A   (pops A, pushes B, pops B, pushes A back)
Thread 1's CAS:       expected=A, memory=A → SUCCEEDS
```
CAS reports success, but the underlying structure (e.g., a lock-free stack's internal links) may have changed twice in between — Thread 1's assumptions about what's "underneath" A are now stale, which can silently corrupt the data structure.

**Fix — `AtomicStampedReference`**: pair every value with a version stamp, e.g. `(A, 1)`. Even if the value returns to `A`, the stamp will have moved on to `3`, so a CAS expecting `(A, 1)` correctly fails.
```java
AtomicStampedReference<String> ref = new AtomicStampedReference<>("A", 1);
ref.compareAndSet("A", "B", 1, 2); // succeeds only if value=="A" AND stamp==1
```

### Summary Table

| Feature | `synchronized` | `AtomicInteger` (CAS) | `LongAdder` |
|---|---|---|---|
| Uses locks | Yes | No | No |
| Blocks threads | Yes | No | No |
| Retries on conflict | No | Yes | Yes (spread across cells) |
| Best under low contention | Good | Excellent | Very good |
| Best under high contention | Degrades (blocking) | Degrades (CAS-failure spinning) | Excellent |
| Handles ABA problem | N/A | No | No (use `AtomicStampedReference`) |

### Concise CAS Summary (say this out loud in an interview)

> CAS (Compare-And-Swap) is a single atomic CPU instruction that updates a memory location **only if** it still holds the value the thread expects to see; otherwise it does nothing and reports failure. Java's atomic classes wrap this in a loop: read the value, compute the new one, try the CAS, and retry automatically if another thread got there first. This gives thread safety **without locking or blocking** — no thread ever sleeps or gets context-switched, so it's much cheaper than `synchronized` under low-to-moderate contention. The trade-off is that under very high contention, many threads keep failing and retrying ("spinning"), which wastes CPU — that's what `LongAdder` fixes, by splitting one counter into several independently-updated cells and summing them only when the total is needed. CAS's one blind spot is the **ABA problem** — it can't detect a value changing away and back before the CAS runs — solved by attaching a version stamp with `AtomicStampedReference`.

---

## 5. Producer-Consumer & Wait/Notify

Classic interview whiteboard problem.

```java
class BoundedBuffer<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;
    private final Object lock = new Object();

    BoundedBuffer(int capacity) { this.capacity = capacity; }

    public void put(T item) throws InterruptedException {
        synchronized (lock) {
            while (queue.size() == capacity) {
                lock.wait(); // releases lock, waits to be notified
            }
            queue.add(item);
            lock.notifyAll(); // wake up waiting consumers
        }
    }

    public T take() throws InterruptedException {
        synchronized (lock) {
            while (queue.isEmpty()) {
                lock.wait();
            }
            T item = queue.poll();
            lock.notifyAll();
            return item;
        }
    }
}
```

**Key points interviewers probe:**
- Always use `while`, never `if`, to re-check the condition after waking (guards against **spurious wakeups** and missed-signal races when multiple consumers/producers exist).
- `notify()` vs `notifyAll()`: `notify()` wakes one arbitrary waiting thread — risky if threads are waiting for different conditions (can cause missed signals/deadlock). `notifyAll()` is safer default; use `notify()` only when you're certain all waiters are interchangeable and only one should proceed.
- In production code, prefer `BlockingQueue` (`ArrayBlockingQueue`, `LinkedBlockingQueue`) over hand-rolled wait/notify — same semantics, far less error-prone.

```java
BlockingQueue<Order> queue = new ArrayBlockingQueue<>(1000);
queue.put(order);  // blocks if full
Order o = queue.take(); // blocks if empty
```

---

## 6. Executor Framework & Thread Pools

Never manually manage raw `Thread` objects in production — use `ExecutorService`.

```java
ExecutorService executor = Executors.newFixedThreadPool(10);
executor.submit(() -> processPayment(order));
executor.shutdown();
executor.awaitTermination(30, TimeUnit.SECONDS);
```

### Standard factory methods (and why senior interviewers push back on them)
| Factory | Backing pool | Problem in production |
|---|---|---|
| `newFixedThreadPool(n)` | fixed threads, **unbounded** `LinkedBlockingQueue` | Queue can grow unbounded → OOM under sustained load spikes |
| `newCachedThreadPool()` | 0 to `Integer.MAX_VALUE` threads | Unbounded thread creation → can exhaust resources |
| `newSingleThreadExecutor()` | 1 thread | Fine for serialized sequential tasks |
| `newScheduledThreadPool(n)` | fixed + delay/periodic scheduling | — |

**This is a well-known senior interview gotcha**: Josh Bloch/Brian Goetz style — the `Executors` convenience methods are discouraged in production precisely because of unbounded queues/threads. The recommended approach is constructing `ThreadPoolExecutor` directly with **bounded** queues and an explicit rejection policy:

```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    10,                              // core pool size
    20,                              // max pool size
    60L, TimeUnit.SECONDS,           // idle thread keep-alive
    new ArrayBlockingQueue<>(500),   // BOUNDED queue
    new ThreadPoolExecutor.CallerRunsPolicy() // backpressure: caller thread runs task if saturated
);
```


### ThreadPoolExecutor Execution Flow (Interview Revision)

**Configuration**
```java
corePoolSize = 10
maximumPoolSize = 20
queue = new ArrayBlockingQueue<>(500);
```

**How tasks are accepted (this order is important):**

```
New task
   │
   ▼
Current threads < corePoolSize ?
   │
 Yes ──► Create a new thread and execute task immediately
   │
  No
   ▼
Queue has space ?
   │
 Yes ──► Put task into queue
   │
  No
   ▼
Current threads < maximumPoolSize ?
   │
 Yes ──► Create a new NON-CORE thread and execute
          the NEW incoming task immediately
   │
  No
   ▼
Reject task (RejectedExecutionHandler)
```

#### Example (`core=10`, `max=20`, `queue=500`)

```
Tasks 1-10
→ Thread1...Thread10 execute immediately.

Tasks 11-510
→ Stored in queue (500 waiting tasks).

Task 511
→ Queue is FULL.
→ Create Thread11.
→ Thread11 executes Task511 immediately.

Task 512
→ Create Thread12.
→ Executes Task512 immediately.

...

Task 520
→ Create Thread20.

Task 521
→ Threads = 20 (max)
→ Queue = 500 (full)
→ Rejected.
```

**Interview gotcha:** New worker threads **do not** first drain the queue. The task that triggered the new thread (e.g., Task511) is handed directly to that thread. Older queued tasks (Task11...Task510) remain in the queue until an existing worker finishes and calls `queue.take()`.

This means **strict global FIFO execution is NOT guaranteed**. FIFO is guaranteed only **within the queue**. During pool expansion, newer tasks may start executing before older queued tasks. This is an intentional design choice to improve throughput and keep the implementation simple.

**Quick revision table**

| Number | Meaning |
|---|---|
| `10` | Core threads kept alive |
| `20` | Maximum threads that may exist |
| `500` | Maximum **waiting tasks** (not threads) in the queue |

**One-line interview answer**

> `ThreadPoolExecutor` first creates core threads, then queues tasks, then creates extra threads only after the queue is full, and finally rejects tasks when both the queue and maximum thread limit are exhausted.



### Rejection Policies (Interview Deep Dive)

A rejection policy is invoked **only when**:

1. Core threads are busy.
2. Queue is full.
3. Maximum thread count has been reached.

Example configuration:

```java
core = 2
max = 4
queue = 2
```

State before rejection:

```
Running:
Thread1 -> Task1
Thread2 -> Task2
Thread3 -> Task5
Thread4 -> Task6

Queue:
Task3
Task4

Incoming:
Task7
```

At this point the pool cannot:
- create another thread
- enqueue another task

So it invokes the configured `RejectedExecutionHandler`.

#### 1. AbortPolicy (Default)

```
Task7
   ↓
Pool Full
   ↓
RejectedExecutionException
```

- Throws `RejectedExecutionException`
- Caller immediately knows the system is overloaded.
- Best for critical business operations where silently losing work is unacceptable.

Typical use:
- Payments
- Orders
- Banking transactions

---

#### 2. CallerRunsPolicy ⭐ (Most common production choice)

```
Client Thread
      │
submit(Task7)
      │
Pool Full
      │
Caller executes Task7 itself
```

Instead of rejecting the task, the **submitting thread executes it**.

This naturally slows down the producer because it becomes busy doing work instead of submitting more tasks.

This is called **backpressure**.

Example:

```
Tomcat Request Thread
        │
submit(payment)
        │
Pool Full
        │
Tomcat thread processes payment itself
```

While processing the payment, that request thread cannot accept another request, so incoming traffic slows naturally instead of overwhelming the server.

Excellent choice for:
- REST APIs
- Payment services
- Kafka producers
- High-throughput backend services

---

#### 3. DiscardPolicy

```
Task7
   │
Pool Full
   │
Dropped
```

- Silently discards the task.
- No exception.
- Caller does not know the task was lost.

Use only when occasional data loss is acceptable.

Examples:
- Metrics
- Telemetry
- Debug logging

Never use for payments or orders.

---

#### 4. DiscardOldestPolicy

Queue before:

```
Task3
Task4
```

New task:

```
Task7
```

Executor removes:

```
Task3
```

Queue becomes:

```
Task4
Task7
```

The oldest queued task is discarded so the newest task is accepted.

Useful when newer data is more valuable than stale data.

Examples:
- Live dashboards
- Stock prices
- Sensor readings
- Monitoring systems

---

### Quick Revision Table

| Policy | What happens? | Best Use Case |
|---|---|---|
| AbortPolicy | Throws `RejectedExecutionException` | Payments, banking, orders |
| CallerRunsPolicy | Caller thread executes task (backpressure) | APIs, payment systems, Kafka |
| DiscardPolicy | Silently drops task | Metrics, telemetry |
| DiscardOldestPolicy | Drops oldest queued task, queues newest | Dashboards, live updates |

### Interview One-Liner

> **CallerRunsPolicy** is usually the preferred production choice because it introduces **backpressure**—instead of crashing or letting the queue grow indefinitely, the submitting thread performs the work, naturally slowing incoming traffic. For critical workflows where failures must be explicit, use **AbortPolicy**. Avoid **DiscardPolicy** and **DiscardOldestPolicy** for business-critical operations because they intentionally drop work.


### Rejection policies (`RejectedExecutionHandler`)
- `AbortPolicy` (default) — throws `RejectedExecutionException`.
- `CallerRunsPolicy` — task runs on the submitting thread, naturally throttles producers. Good for backpressure in payment/order-flow systems.
- `DiscardPolicy` — silently drops the task.
- `DiscardOldestPolicy` — drops the oldest queued task and retries.

### Thread pool sizing rule of thumb
- **CPU-bound work**: `threads ≈ N_cpu + 1`
- **I/O-bound work**: `threads ≈ N_cpu * (1 + wait_time/compute_time)` — because threads spend most time blocked on I/O (DB calls, HTTP calls, Kafka produce acks), you can run many more threads than cores.

---

## 7. Future, CompletableFuture & Async Programming

### `Future` — the old, limited way
```java
Future<Integer> future = executor.submit(() -> computeSomething());
Integer result = future.get(); // BLOCKS — no way to chain or compose without get()
```
Limitations: no callback/composition support, `get()` blocks, no easy way to combine multiple futures, manual exception handling.

### `CompletableFuture` (Java 8+) — the modern async toolkit
```java
CompletableFuture<Order> future = CompletableFuture
    .supplyAsync(() -> fetchOrder(orderId), executor)   // async computation, runs on given executor
    .thenApply(order -> enrichOrder(order))              // transform result (sync, same thread)
    .thenApplyAsync(order -> validate(order), executor)   // transform result (async, on executor)
    .exceptionally(ex -> fallbackOrder())                 // handle exception, provide default
    .thenAccept(order -> log.info("processed {}", order)); // consume, no return value
```

**Key methods interviewers expect you to know cold:**

| Method | Purpose |
|---|---|
| `supplyAsync` / `runAsync` | start async task (returns value / no value) |
| `thenApply` | transform result — **runs on completing thread** |
| `thenApplyAsync` | transform result — **runs on ForkJoinPool.commonPool() or supplied executor** |
| `thenCompose` | flatten nested futures (like `flatMap`) — use when your function returns another `CompletableFuture` |
| `thenCombine` | combine results of two independent futures |
| `allOf` | wait for all futures to complete (returns `Void`) |
| `anyOf` | completes when the first of several futures completes |
| `exceptionally` | recover from exception with a fallback value |
| `handle` | see both result and exception, decide outcome either way |
| `whenComplete` | side-effect callback, doesn't change result |



### Deep Dive: `map()` vs `flatMap()` → `thenApply()` vs `thenCompose()` (Interview Revision)

**Golden analogy**

| Streams | CompletableFuture |
|---|---|
| `map()` | `thenApply()` |
| `flatMap()` | `thenCompose()` |

### `map()`
Transforms **one object into one object**.

```
Employee
   │
map(getName)
   │
String
```

If the mapping function returns a collection:

```
Employee
   │
map(getPhones)
   │
List<String>
```

Result:

```
List<List<String>>
```

because `map()` simply collects whatever your function returns.

---

### `flatMap()`

`flatMap()` = **map + flatten**

```
Employee
   │
flatMap(emp -> emp.getPhones().stream())
   │
Stream<String>
```

Result:

```
111
222
333
444
555
```

instead of

```
[
 [111,222],
 [333,444],
 [555]
]
```

---

### `thenApply()`

Use when the lambda returns a **plain value**.

```java
fetchUserAsync(id)
    .thenApply(user -> user.getName())
```

Type flow

```
CompletableFuture<User>
        ↓
       User
        ↓
      String
        ↓
CompletableFuture<String>
```

---

### `thenCompose()`

Use when the lambda returns another **CompletableFuture**.

```java
fetchUserAsync(id)
    .thenCompose(this::fetchOrderAsync)
```

Type flow

```
CompletableFuture<User>
        ↓
       User
        ↓
CompletableFuture<Order>
        ↓
     Flatten
        ↓
CompletableFuture<Order>
```

---

### Multi-stage chain

Correct:

```
Future<User>
      ↓
Future<Order>
      ↓
Future<Payment>
      ↓
Future<Invoice>
```

Wrong (using `thenApply` for async methods):

```
Future<User>
      ↓
Future<Future<Order>>
      ↓
Future<Future<Future<Payment>>>
      ↓
Future<Future<Future<Future<Invoice>>>>
```

---

### Interview Rules

| Lambda returns | Use |
|---|---|
| `T` | `thenApply()` |
| `CompletableFuture<T>` | `thenCompose()` |

| Lambda returns | thenApply | thenCompose |
|---|---|---|
| `T` | ✅ `CompletableFuture<T>` | ❌ Compile error |
| `CompletableFuture<T>` | ✅ `CompletableFuture<CompletableFuture<T>>` | ✅ `CompletableFuture<T>` |

---

### Memory Trick

Think of every `CompletableFuture` as a **box**.

- `thenApply()` **wraps** whatever the lambda returns.
- `thenCompose()` **unwraps one Future layer** if the lambda already returned a `CompletableFuture`.

```
thenApply  = wrap
thenCompose = wrap + unwrap(one Future)
```

### One-liners

> `map()` ↔ `thenApply()`

> `flatMap()` ↔ `thenCompose()`

> `flatMap = map + flatten`

> `thenCompose = thenApply + flatten`

**`thenApply` vs `thenCompose` — classic interview question:**
```java
// thenApply: use when the function returns a plain value
CompletableFuture<Integer> a = f.thenApply(x -> x + 1);

// thenCompose: use when the function itself returns a CompletableFuture
// (avoids CompletableFuture<CompletableFuture<T>> nesting — like flatMap vs map)
CompletableFuture<Order> b = fetchUserAsync(id)
    .thenCompose(user -> fetchOrderAsync(user.getId()));
```

**Combining independent async calls (very common in microservice fan-out):**
```java
CompletableFuture<User> userFuture = CompletableFuture.supplyAsync(() -> fetchUser(id));
CompletableFuture<List<Order>> ordersFuture = CompletableFuture.supplyAsync(() -> fetchOrders(id));

CompletableFuture<UserProfile> combined = userFuture.thenCombine(ordersFuture,
    (user, orders) -> new UserProfile(user, orders));

UserProfile profile = combined.get(5, TimeUnit.SECONDS); // always use a timeout in prod
```

**Custom executor is important**: without specifying one, `*Async` methods default to `ForkJoinPool.commonPool()`, which is shared JVM-wide. In a Spring Boot service under load, this can starve other parts of the app (including parallel streams!) that also rely on the common pool. **Always pass an explicit, bounded executor for production async work.**

```java
@Bean
public Executor paymentExecutor() {
    return new ThreadPoolExecutor(10, 20, 60, TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(200), new ThreadPoolExecutor.CallerRunsPolicy());
}
```

### Exception handling in async chains
```java
CompletableFuture.supplyAsync(() -> riskyCall())
    .handle((result, ex) -> {
        if (ex != null) {
            log.error("failed", ex);
            return fallback();
        }
        return result;
    });
```
Unhandled exceptions in a `CompletableFuture` chain are swallowed unless you call `.get()`/`.join()` (which rethrow wrapped in `ExecutionException`/`CompletionException`) or use `.exceptionally()`/`.handle()`.

---

## 8. Coordination Utilities

| Tool | Purpose | Typical use |
|---|---|---|
| `CountDownLatch` | one-time gate; N threads count down, waiters proceed at 0 | wait for N services to initialize before starting traffic |
| `CyclicBarrier` | reusable barrier; N threads wait for each other at a point, then all proceed together | parallel computation phases (map-reduce style) |
| `Semaphore` | limits concurrent access to a resource to N permits | rate-limiting concurrent DB connections / external API calls |
| `Phaser` | flexible, reusable, dynamic party count (advanced, less commonly used) | multi-phase pipelines with changing participants |
| `Exchanger` | two threads exchange objects at a sync point | pairwise handoff (rare in interviews) |

```java
// CountDownLatch
CountDownLatch latch = new CountDownLatch(3);
// each worker calls latch.countDown() when done
latch.await(); // main thread blocks until count reaches 0

// Semaphore — limit concurrent downstream calls
Semaphore semaphore = new Semaphore(5); // max 5 concurrent
semaphore.acquire();
try { callDownstreamService(); } finally { semaphore.release(); }
```

**

## 🧠 Coordination Utilities – Interview Revision Cheat Sheet

### 1. CountDownLatch 🔒
**Analogy:** 🚀 Rocket launch

```
Fuel ✔
Weather ✔
Navigation ✔
    ↓
 Launch
```

- One-time countdown.
- Workers call `countDown()`.
- Waiting thread(s) call `await()`.
- Releases everyone when count reaches **0**.
- **Not reusable.**

**Use:** Service startup, wait for initialization.

**Memory:** **Boss waits, workers don't.**

---

### 2. CyclicBarrier 🚧
**Analogy:** 🏃 Marathon checkpoint

```
T1 ----\
T2 -----> Barrier
T3 ----/

↓

All continue together
```

- All participating threads wait.
- Automatically resets after each round.
- Fixed number of participants.

**Use:** Parallel computation, simulations, Map-Reduce phases.

**Memory:** **Everyone waits for everyone.**

---

### 3. Semaphore 🚦
**Analogy:** 🅿️ Parking lot

```
5 Parking Spaces

🚗🚗🚗🚗🚗  Allowed

🚗 waits
```

- Limits concurrent access using permits.
- `acquire()` → take permit
- `release()` → return permit

**Use:** DB connections, REST APIs, printers, GPUs.

**Memory:** **Only N threads allowed simultaneously.**

---

### 4. Phaser 🔄
**Analogy:** 🎓 College course

```
Phase 1 : A B C

↓

C leaves

↓

Phase 2 : A B

↓

D joins

↓

Phase 3 : A B D
```

Think of it as:

> **CyclicBarrier + Dynamic participants**

- Reusable.
- Multiple phases.
- Threads can `register()`.
- Threads can `arriveAndDeregister()`.
- Tracks current phase.

**Use:** Multi-stage workflows, simulations, complex pipelines.

**Memory:** **Barrier where the team can grow or shrink.**

---

### 5. Exchanger 🤝
**Analogy:** Two chefs exchange trays

```
Chef A  ⇄  Chef B
Tray A     Tray B
```

- Exactly two threads exchange objects.
- Both wait until the other arrives.

**Use:** Producer/Consumer buffer swap.

**Memory:** **Two threads meet and swap objects.**

---

## ⭐ Quick Comparisons

### CountDownLatch vs CyclicBarrier

| CountDownLatch | CyclicBarrier |
|---|---|
| Boss waits | Everyone waits |
| One-time | Reusable |
| Workers don't wait | All wait together |
| Countdown to zero | Meet every round |

### CyclicBarrier vs Phaser

| CyclicBarrier | Phaser |
|---|---|
| Fixed participants | Dynamic participants |
| Reusable | Reusable |
| Same threads every round | Threads can join/leave |
| Barrier only | Barrier + Multiple phases |

## ⭐ 5-Second Recall

```
CountDownLatch → Wait until work finishes

CyclicBarrier  → Wait for each other

Semaphore      → Only N at a time

Phaser         → Barrier + changing team

Exchanger      → Two threads swap objects
```


CountDownLatch vs CyclicBarrier:** latch is one-time-use and threads don't wait on each other symmetrically (some just count down without waiting); barrier is reusable and *all* parties wait for each other every cycle.

---

## 9. Concurrent Collections

| Collection | Notes |
|---|---|
| `ConcurrentHashMap` | segment/bucket-level locking (Java 8+: CAS + synchronized on bin heads), no locking on reads. Never null keys/values. |
| `CopyOnWriteArrayList` | copies the whole array on every write — great for read-heavy, rarely-written lists (e.g., listener lists) |
| `ConcurrentLinkedQueue` | lock-free (CAS-based) unbounded queue |
| `BlockingQueue` impls | `ArrayBlockingQueue` (bounded, array-backed), `LinkedBlockingQueue` (optionally bounded), `PriorityBlockingQueue`, `DelayQueue`, `SynchronousQueue` (zero capacity — direct handoff, used in `newCachedThreadPool`) |

**`ConcurrentHashMap` interview deep-dive:**
- Java 7: segment-based locking (16 segments by default) — lock striping.
- Java 8+: no more segments; uses a synchronized block on the bin's first node (per-bucket lock) plus CAS for empty-bucket insert, so contention is even finer-grained.
- `compute`, `computeIfAbsent`, `merge` are atomic — useful for thread-safe counters/aggregations without external locking:
```java
map.merge(key, 1, Integer::sum); // atomic increment-or-insert
```
- **Gotcha**: `computeIfAbsent` must not be re-entrant (don't call another map operation on the same map inside the lambda) — can deadlock in some JDK versions/scenarios.

---

## 10. Deadlock, Livelock, Starvation

- **Deadlock**: two+ threads each hold a lock the other needs, and neither releases. Classic example: Thread A locks resource 1 then wants resource 2; Thread B locks resource 2 then wants resource 1.
```java
// Deadlock-prone
synchronized (lockA) {
    synchronized (lockB) { /* ... */ }
}
// Thread 2 does the reverse order → deadlock risk
```
**Fix: consistent lock ordering** — always acquire locks in the same global order (e.g., by object hash code or an assigned ID) across all code paths.

- **Livelock**: threads are actively responding to each other but making no progress (e.g., two people repeatedly stepping aside for each other in a hallway). Fix: introduce randomized backoff.

- **Starvation**: a thread never gets CPU time or a lock because other threads keep getting priority (e.g., unfair lock always favoring certain threads). Fix: use fair locks (`new ReentrantLock(true)`) if starvation is a real risk — at some throughput cost.

**How to detect deadlocks in production**: thread dump (`jstack <pid>`) — JVM explicitly reports "Found one Java-level deadlock" with the two threads and locks involved. Also detectable via `ThreadMXBean.findDeadlockedThreads()` programmatically.

---

## 11. ThreadLocal

Gives each thread its own independent copy of a variable — no synchronization needed since there's no sharing.

```java
private static final ThreadLocal<SimpleDateFormat> formatter =
    ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
```

Common real use: storing request-scoped data (e.g., a correlation/trace ID, or the authenticated user) across a call chain without passing it through every method signature — MDC in logging frameworks (Logback/Log4j2) uses this internally.

**Critical production gotcha**: in thread-pooled environments (Tomcat, Spring's `@Async`, `ExecutorService`), threads are reused across requests. If you don't call `.remove()` in a `finally` block, stale data leaks into the next request handled by that pooled thread — a real and nasty bug class in payments/session-context handling.

```java
try {
        contextHolder.set(requestContext);
// process request
} finally {
        contextHolder.remove(); // mandatory in pooled-thread environments
}
```

**Also note**: `ThreadLocal` doesn't automatically propagate to child threads or async callbacks (e.g., inside a `CompletableFuture.supplyAsync`) — you must explicitly copy the value into the new thread/task if needed (`InheritableThreadLocal` handles parent→child, but not arbitrary pool threads).

---

## 12. Fork/Join Framework & Parallel Streams

```java
class SumTask extends RecursiveTask<Long> {
    private final int[] arr; private final int start, end;
    // ...
    protected Long compute() {
        if (end - start <= THRESHOLD) {
            long sum = 0;
            for (int i = start; i < end; i++) sum += arr[i];
            return sum;
        }
        int mid = (start + end) / 2;
        SumTask left = new SumTask(arr, start, mid);
        SumTask right = new SumTask(arr, mid, end);
        left.fork();                    // async
        long rightResult = right.compute(); // sync (compute directly, avoid fork overhead)
        long leftResult = left.join();
        return leftResult + rightResult;
    }
}
```
- **Work-stealing**: idle threads in the pool "steal" tasks from the queues of busy threads, improving load balancing for divide-and-conquer workloads.
- `parallelStream()` uses the common `ForkJoinPool` under the hood — same caveat as `CompletableFuture`: don't run long/blocking I/O tasks on it, since it can starve other parallel-stream usage across the JVM.

---

## 13. Java Memory Model (JMM) — the "why" behind everything above

- **Happens-before relationship**: guarantees ordering/visibility between actions across threads. Established by: `synchronized` block exit → next entry; `volatile` write → subsequent read; `Thread.start()` → actions in the new thread; thread termination → `Thread.join()` returning in the joining thread.
- Without a happens-before edge, the JVM/CPU/compiler are free to reorder instructions and cache values in registers — meaning one thread's writes may never become visible to another, or may appear out of order. This is *why* naive code without synchronization can behave correctly in testing but fail intermittently in production under different JIT optimization levels or CPU architectures.

**Good interview one-liner**: "Concurrency bugs are JMM visibility/ordering bugs as much as they are logic bugs — that's why they're often unreproducible in a debugger, since a debugger changes timing and often forces safe orderings."

---

## 14. Virtual Threads (Java 21+, Project Loom)

Increasingly asked about in senior interviews since Java 21 is now mainstream in enterprise.

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        executor.submit(() -> handleRequest());
        }
```
- Virtual threads are lightweight, JVM-managed threads (not 1:1 with OS threads) — you can spin up millions of them.
- Designed for **I/O-bound, blocking-style code** (e.g., blocking JDBC calls, blocking HTTP clients) — write simple synchronous-looking code, get async-level scalability, without needing `CompletableFuture` chains.
- **Do NOT use** for CPU-bound work — no benefit over platform threads there.
- **Pinning gotcha**: a virtual thread gets "pinned" to its carrier OS thread (loses the scalability benefit) when it executes inside a `synchronized` block/method while blocking, or during certain native calls. Prefer `ReentrantLock` over `synchronized` in virtual-thread-heavy code to avoid pinning.
- Structured Concurrency (`StructuredTaskScope`, preview API) — lets you treat a group of related subtasks (spawned in different threads) as a single unit of work with unified cancellation/error propagation. Worth mentioning if the interviewer goes deep on modern Java concurrency.

---

## 15. High-Value Interview Questions (with concise model answers)

**Q: Difference between `Runnable` and `Callable`?**
`Runnable.run()` returns nothing and can't throw checked exceptions; `Callable.call()` returns a value and can throw checked exceptions. Use `Callable` when you need a result via `Future`.

**Q: Why is `Vector` slower than `ArrayList` and why avoid it?**
`Vector` synchronizes every method call individually (coarse-grained), so even single-threaded use pays a locking cost, and it still doesn't protect compound operations (check-then-act) from race conditions. `Collections.synchronizedList()` or `CopyOnWriteArrayList`/`ConcurrentHashMap`-family are preferred for actual concurrent use.

**Q: What's a race condition vs a data race?**
Data race: two threads access the same memory location concurrently, at least one is a write, without synchronization (a JMM-level term). Race condition: a broader term — program correctness depends on timing/interleaving of operations, which may or may not involve an unsynchronized data race (e.g., check-then-act bugs even with individually-atomic operations).

**Q: How would you make a non-thread-safe class thread-safe with minimal contention?**
Options, roughly in preference order for read-heavy workloads: immutability first (best fix, no locking needed at all), then confinement (`ThreadLocal`), then lock-free (atomics/CAS), then fine-grained locking (`ReentrantLock`/`ReadWriteLock`/`StampedLock`), then coarse `synchronized` as a last resort.

**Q: How does `ExecutorService.shutdown()` differ from `shutdownNow()`?**
`shutdown()`: stops accepting new tasks, lets queued/running tasks finish. `shutdownNow()`: attempts to stop all actively executing tasks (via interrupt) and returns the list of tasks that never started; doesn't guarantee running tasks actually stop (depends on whether the task code checks `Thread.interrupted()`).

**Q: Why can't you extend `Thread` and implement `Runnable` be considered equally good practice?**
Extending `Thread` uses up your only superclass slot and conflates "being a thread" with "the task to run" — poor separation of concerns; a `Runnable` can be handed to any executor, reused, and composed. Nearly always prefer `Runnable`/`Callable` + executor.

**Q: What happens if an exception is thrown inside a thread pool task and you don't handle it?**
For `execute()`: the exception propagates to the thread's uncaught exception handler (usually just prints to stderr) and the thread dies but the pool creates a replacement worker thread — silent failure. For `submit()`: the exception is captured inside the returned `Future` and only surfaces when you call `.get()` — so if you never call `get()`, the failure is silently swallowed. This is a common production bug source: always call `get()` or add explicit exception handling in the task itself.

**Q: In a Spring Boot service, how do you safely run something async without breaking transaction context or request context?**
`@Async` methods run on a separate thread — they don't inherit the calling thread's transaction (`@Transactional` doesn't propagate across threads) or security/request context by default. You need to explicitly propagate what's needed (e.g., pass IDs/data as method parameters rather than relying on `ThreadLocal`-based context, or use a `TaskDecorator` to copy `MDC`/security context into the async thread).

---

## 16. Quick Decision Cheat-Sheet

- **Need atomic single-variable updates, low contention** → `AtomicInteger`/`AtomicLong`
- **Need atomic counters, very high contention** → `LongAdder`
- **Need mutual exclusion with simple semantics** → `synchronized`
- **Need tryLock/timeout/interruptible/fairness/multiple conditions** → `ReentrantLock`
- **Read-heavy, write-rare shared state** → `ReadWriteLock` or `StampedLock` (optimistic read)
- **Bounded producer-consumer handoff** → `BlockingQueue`
- **Fire-and-forget parallel tasks with results** → `ExecutorService` + `Future`/`CompletableFuture`
- **Chaining/composing async operations** → `CompletableFuture`
- **Wait for N async initializations, one-time** → `CountDownLatch`
- **Rate-limit concurrent access to a resource** → `Semaphore`
- **CPU-bound divide-and-conquer** → `ForkJoinPool` / parallel streams
- **I/O-bound, many concurrent blocking calls, Java 21+** → virtual threads
- **Per-thread context (trace IDs, current user)** → `ThreadLocal` (always clean up in `finally`)

---

*Given your payments background: a strong framing for "tell me about a concurrency problem you solved" is the trade-off between throughput and correctness under load — e.g., bounding thread pool queues to avoid OOM during traffic spikes while using `CallerRunsPolicy` for graceful backpressure, or using `ConcurrentHashMap.merge` for atomic idempotency-key tracking in a high-TPS transaction path.*
