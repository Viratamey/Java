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

## 4. Atomic Variables & CAS

`java.util.concurrent.atomic` — lock-free thread safety using **Compare-And-Swap (CAS)** at the hardware level.

```java
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet();
counter.compareAndSet(5, 10); // set to 10 only if current value is 5
```

**Why CAS over locks?** No blocking, no context switching, better throughput under low-to-moderate contention. Under very high contention, CAS retries can spin and hurt performance — that's when `LongAdder` (which stripes counters across cells to reduce contention, then sums on read) beats `AtomicLong`.

**Interview question: how does CAS work under the hood?**
CPU instruction `cmpxchg` (x86) atomically compares memory value to expected value, and if equal, swaps in the new value — all as one atomic hardware operation. If another thread changed the value in between, CAS fails and Java code typically retries in a loop.

**ABA problem**: value changes A→B→A between your read and CAS — CAS succeeds even though the value *did* change in between. Solved with `AtomicStampedReference` (adds a version stamp).

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

**CountDownLatch vs CyclicBarrier:** latch is one-time-use and threads don't wait on each other symmetrically (some just count down without waiting); barrier is reusable and *all* parties wait for each other every cycle.

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
