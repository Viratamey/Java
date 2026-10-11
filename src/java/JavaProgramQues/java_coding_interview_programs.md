# Most Frequently Asked Java Coding Interview Programs

These are the programs that keep showing up in Java interview prep lists, fresher MNC practical rounds, Java 8 coding repositories and multithreading guides. The tiers below come from the overlap across those sources, not from an official statistic, so use them as a practice order.

- **Tier 1 (asked almost everywhere):** palindrome, reverse a string or number, Fibonacci, factorial, prime check, anagram, swap two numbers without a third variable, odd/even check, duplicate characters, largest and second largest, **print odd and even numbers alternately with two threads**, thread-safe Singleton.
- **Tier 2 (very common):** producer-consumer, deadlock example, first non-repeating character, missing number, two sum, binary search, sorting, balanced parentheses, LRU cache, stream questions like second highest salary and group by.
- **Tier 3 (common for experienced roles):** linked list reversal and cycle detection, threads printing in round-robin, CompletableFuture chains, thread-safe counter with a thread pool.

Type each program yourself at least once. Interviewers often ask for time complexity and edge cases right after you finish.

---

## 1. Basic Logic Programs

**Program 1. Check even or odd.**
```java
boolean even = n % 2 == 0;
boolean odd  = n % 2 != 0;   // use != 0, because -3 % 2 is -1, not 1
```

**Program 2. Palindrome (string and number).**
```java
static boolean isPalindrome(String s) {
    int i = 0, j = s.length() - 1;
    while (i < j) if (s.charAt(i++) != s.charAt(j--)) return false;
    return true;
}

static boolean isPalindrome(int n) {
    int rev = 0, t = n;
    while (t > 0) { rev = rev * 10 + t % 10; t /= 10; }
    return n >= 0 && rev == n;
}
```

**Program 3. Reverse a string and a number.**
```java
String r1 = new StringBuilder(s).reverse().toString();

char[] c = s.toCharArray();
for (int i = 0, j = c.length - 1; i < j; i++, j--) { char t = c[i]; c[i] = c[j]; c[j] = t; }
String r2 = new String(c);

static String rev(String s) { return s.isEmpty() ? s : rev(s.substring(1)) + s.charAt(0); }

int rev = 0;
while (n > 0) { rev = rev * 10 + n % 10; n /= 10; }
```

**Program 4. Fibonacci series.**
```java
int a = 0, b = 1;
for (int i = 0; i < n; i++) {
        System.out.print(a + " ");
int next = a + b; a = b; b = next;
}

static int fib(int n) { return n <= 1 ? n : fib(n - 1) + fib(n - 2); }  // O(2^n), add a memo map to make it O(n)
```

**Program 5. Factorial.**
```java
static long fact(int n) { long r = 1; for (int i = 2; i <= n; i++) r *= i; return r; }
// overflows after 20!, use BigInteger for larger values
static long factRec(int n) { return n <= 1 ? 1 : n * factRec(n - 1); }
```

**Program 6. Prime number.**
```java
static boolean isPrime(int n) {
    if (n < 2) return false;
    for (int i = 2; (long) i * i <= n; i++) if (n % i == 0) return false;
    return true;
}
```
Checking only up to the square root makes it O(sqrt n).

**Program 7. Swap two numbers without a third variable.**
```java
a = a + b;
b = a - b;
a = a - b;
// or with XOR: a ^= b; b ^= a; a ^= b;
```
Mention that the addition version can overflow for very large values.

**Program 8. Check anagram.**
```java
static boolean isAnagram(String a, String b) {
    if (a.length() != b.length()) return false;
    int[] count = new int[26];
    for (int i = 0; i < a.length(); i++) {
        count[Character.toLowerCase(a.charAt(i)) - 'a']++;
        count[Character.toLowerCase(b.charAt(i)) - 'a']--;
    }
    for (int x : count) if (x != 0) return false;
    return true;
}
```
The simpler alternative is to sort both char arrays and compare with `Arrays.equals`, which is O(n log n).

**Program 9. Character frequency, duplicates and first non-repeating character.**
```java
Map<Character, Integer> freq = new LinkedHashMap<>();
for (char ch : s.toCharArray()) freq.merge(ch, 1, Integer::sum);

freq.forEach((k, v) -> { if (v > 1) System.out.println(k + " -> " + v); });  // duplicates

for (Map.Entry<Character, Integer> e : freq.entrySet())
    if (e.getValue() == 1) { System.out.println(e.getKey()); break; }       // first non-repeating
```
LinkedHashMap keeps insertion order, which is what makes the first-unique logic work.

**Program 10. Count vowels.**
```java
long vowels = s.toLowerCase().chars().filter(c -> "aeiou".indexOf(c) >= 0).count();
```

**Program 11. Largest and second largest in an array (single pass).**
```java
int first = Integer.MIN_VALUE, second = Integer.MIN_VALUE;
for (int x : arr) {
    if (x > first) { second = first; first = x; }
    else if (x > second && x != first) second = x;
}
```

**Program 12. Find the missing number in 1 to n.**
```java
int expected = n * (n + 1) / 2;
int actual = 0;
for (int x : arr) actual += x;
int missing = expected - actual;
```

**Program 13. Two sum (indices of two numbers adding to a target).**
```java
Map<Integer, Integer> seen = new HashMap<>();
for (int i = 0; i < nums.length; i++) {
    Integer j = seen.get(target - nums[i]);
    if (j != null) return new int[]{j, i};
    seen.put(nums[i], i);
}
return null;   // O(n) time, O(n) space
```

**Program 14. Armstrong number and sum of digits.**
```java
static boolean isArmstrong(int n) {
    int digits = String.valueOf(n).length(), sum = 0, t = n;
    while (t > 0) { sum += (int) Math.pow(t % 10, digits); t /= 10; }
    return sum == n;      // 153 = 1^3 + 5^3 + 3^3
}

int sum = 0;
while (n > 0) { sum += n % 10; n /= 10; }
```

**Program 15. GCD and LCM.**
```java
static int gcd(int a, int b) { return b == 0 ? a : gcd(b, a % b); }
static int lcm(int a, int b) { return a / gcd(a, b) * b; }
```

**Program 16. Reverse the words in a sentence.**
```java
String[] w = s.trim().split(" +");
Collections.reverse(Arrays.asList(w));
String out = String.join(" ", w);
```

**Program 17. Check if one string is a rotation of another.**
```java
boolean rotation = a.length() == b.length() && (a + a).contains(b);
```

**Program 18. Binary search and bubble sort.**
```java
static int binarySearch(int[] a, int key) {      // array must be sorted
    int lo = 0, hi = a.length - 1;
    while (lo <= hi) {
        int mid = lo + (hi - lo) / 2;             // avoids int overflow
        if (a[mid] == key) return mid;
        if (a[mid] < key) lo = mid + 1; else hi = mid - 1;
    }
    return -1;
}

for (int i = 0; i < a.length - 1; i++) {
    boolean swapped = false;
    for (int j = 0; j < a.length - 1 - i; j++)
        if (a[j] > a[j + 1]) { int t = a[j]; a[j] = a[j + 1]; a[j + 1] = t; swapped = true; }
    if (!swapped) break;
}
```
Binary search is O(log n). Bubble sort is O(n^2) in the worst case and O(n) when already sorted, thanks to the `swapped` flag.

**Program 19. Merge two sorted arrays.**
```java
static int[] merge(int[] a, int[] b) {
    int[] r = new int[a.length + b.length];
    int i = 0, j = 0, k = 0;
    while (i < a.length && j < b.length) r[k++] = a[i] <= b[j] ? a[i++] : b[j++];
    while (i < a.length) r[k++] = a[i++];
    while (j < b.length) r[k++] = b[j++];
    return r;
}
```

**Program 20. Balanced parentheses.**
```java
static boolean balanced(String s) {
    Deque<Character> st = new ArrayDeque<>();
    for (char c : s.toCharArray()) {
        if ("([{".indexOf(c) >= 0) st.push(c);
        else if (")]}".indexOf(c) >= 0) {
            if (st.isEmpty()) return false;
            char o = st.pop();
            if ((c == ')' && o != '(') || (c == ']' && o != '[') || (c == '}' && o != '{')) return false;
        }
    }
    return st.isEmpty();
}
```

**Program 21. Remove duplicates from an array and find duplicates.**
```java
int[] unique = Arrays.stream(arr).distinct().toArray();

Set<Integer> seen = new HashSet<>(), dups = new LinkedHashSet<>();
for (int x : arr) if (!seen.add(x)) dups.add(x);
```

**Program 22. Pyramid pattern.**
```java
for (int i = 1; i <= n; i++)
    System.out.println(" ".repeat(n - i) + "*".repeat(2 * i - 1));
```

---

## 2. Multithreading Programs (Very Frequently Asked)

**Program 23. Print odd and even numbers alternately using two threads (wait/notify).**

This is the single most common thread coding question. Two threads share one object; each prints only when it is its turn and otherwise waits.
```java
class OddEvenPrinter {
    private int number = 1;
    private final int max;
    OddEvenPrinter(int max) { this.max = max; }

    synchronized void print(boolean odd) {
        while (number <= max) {
            if ((number % 2 != 0) == odd) {
                System.out.println(Thread.currentThread().getName() + ": " + number++);
                notifyAll();
            } else {
                try { wait(); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
            }
        }
        notifyAll();   // wake the other thread so it can exit
    }
}

public class Main {
    public static void main(String[] args) {
        OddEvenPrinter p = new OddEvenPrinter(10);
        new Thread(() -> p.print(true),  "Odd").start();
        new Thread(() -> p.print(false), "Even").start();
    }
}
```
Points to say out loud: `wait()` must be inside `synchronized`; use a `while` loop, not `if`, because of spurious wakeups; `notifyAll()` is safer than `notify()`.

**Program 24. Same problem using Semaphore (cleaner, no wait/notify).**
```java
Semaphore oddTurn = new Semaphore(1), evenTurn = new Semaphore(0);

Thread odd = new Thread(() -> {
    for (int i = 1; i <= 10; i += 2) {
        oddTurn.acquireUninterruptibly();
        System.out.println("Odd: " + i);
        evenTurn.release();
    }
});
Thread even = new Thread(() -> {
    for (int i = 2; i <= 10; i += 2) {
        evenTurn.acquireUninterruptibly();
        System.out.println("Even: " + i);
        oddTurn.release();
    }
});
odd.start(); even.start();
```

**Program 25. Print 1 to N using N threads in rotation (T1: 1, 4, 7; T2: 2, 5, 8; T3: 3, 6, 9).**

This generalizes the odd/even problem; with 2 threads it is the same thing.
```java
class RoundRobin {
    private int current = 1;
    private final int max, threads;
    RoundRobin(int max, int threads) { this.max = max; this.threads = threads; }

    synchronized void run(int id) {                 // id from 0 to threads-1
        while (current <= max) {
            if ((current - 1) % threads == id) {
                System.out.println("T" + (id + 1) + ": " + current++);
                notifyAll();
            } else {
                try { wait(); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
            }
        }
        notifyAll();
    }
}
// start 3 threads, each calling rr.run(0), rr.run(1), rr.run(2)
```

**Program 26. Producer-consumer using BlockingQueue.**
```java
BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);

Thread producer = new Thread(() -> {
    try {
        for (int i = 1; i <= 10; i++) { queue.put(i); System.out.println("Produced " + i); }
        queue.put(-1);                              // poison pill tells consumer to stop
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
});
Thread consumer = new Thread(() -> {
    try {
        while (true) {
            int v = queue.take();
            if (v == -1) break;
            System.out.println("Consumed " + v);
        }
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
});
producer.start(); consumer.start();
```
`put()` blocks when the queue is full and `take()` blocks when it is empty, so no manual wait/notify is needed. If asked to do it the hard way, use the same `while` plus `wait()`/`notifyAll()` pattern as Program 23 with a shared list and a capacity check.

**Program 27. Create a deadlock, then fix it.**
```java
Object a = new Object(), b = new Object();

// Deadlock: opposite lock order
new Thread(() -> { synchronized (a) { pause(100); synchronized (b) { System.out.println("T1"); } } }).start();
new Thread(() -> { synchronized (b) { pause(100); synchronized (a) { System.out.println("T2"); } } }).start();

// Fix: both threads take locks in the same order (a, then b)
new Thread(() -> { synchronized (a) { synchronized (b) { System.out.println("T2"); } } }).start();
```
(`pause` is a small helper that wraps `Thread.sleep` in try/catch.) You can also detect deadlocks with `jstack` or `ThreadMXBean.findDeadlockedThreads()`.

**Program 28. Ten threads increment a counter 1000 times each (expected 10000).**
```java
AtomicInteger count = new AtomicInteger();
ExecutorService ex = Executors.newFixedThreadPool(10);
for (int i = 0; i < 10; i++)
    ex.submit(() -> { for (int j = 0; j < 1000; j++) count.incrementAndGet(); });
ex.shutdown();
ex.awaitTermination(1, TimeUnit.MINUTES);
System.out.println(count.get());   // 10000
```
With a plain `int` and `count++` the result would be less than 10000 because `++` is not atomic. `synchronized` or `ReentrantLock` also work.

### Alternative 1: Use `synchronized`

Replace `AtomicInteger` with a plain `int`, and protect every increment with the same lock. An array lets the local counter be shared by the lambda; the array itself is not what makes the increment thread-safe—the synchronized block does.

```java
int[] count = {0};
Object lock = new Object();
ExecutorService ex = Executors.newFixedThreadPool(10);

for (int i = 0; i < 10; i++) {
    ex.submit(() -> {
        for (int j = 0; j < 1000; j++) {
            synchronized (lock) {
                count[0]++;
            }
        }
    });
}

ex.shutdown();
if (ex.awaitTermination(1, TimeUnit.MINUTES)) {
    System.out.println(count[0]);  // 10000
} else {
    System.out.println("Tasks did not finish within one minute");
}
```

All threads must synchronize on the **same lock object**. If each thread used a different lock, increments could still be lost.

### Alternative 2: Use `ReentrantLock`

```java
int[] count = {0};
ReentrantLock lock = new ReentrantLock();
ExecutorService ex = Executors.newFixedThreadPool(10);

for (int i = 0; i < 10; i++) {
    ex.submit(() -> {
        for (int j = 0; j < 1000; j++) {
            lock.lock();
            try {
                count[0]++;
            } finally {
                lock.unlock();  // always release the lock
            }
        }
    });
}

ex.shutdown();
if (ex.awaitTermination(1, TimeUnit.MINUTES)) {
    System.out.println(count[0]);  // 10000
} else {
    System.out.println("Tasks did not finish within one minute");
}
```

`ReentrantLock` must be unlocked in a `finally` block so an exception does not leave the lock held.

**Comparison:**
- `AtomicInteger`: simplest for a standalone atomic counter; no explicit lock.
- `synchronized`: simplest when protecting a critical section or multiple related fields.
- `ReentrantLock`: useful when you need features such as `tryLock()`, timed acquisition, or interruptible lock acquisition.

These are alternative implementations—use one version at a time. The `awaitTermination()` check matters because a timeout means tasks may not have completed, so you cannot assume the final count is `10000`.

**Program 29. Make threads run in order (T1, then T2, then T3).**
```java
t1.start(); t1.join();
t2.start(); t2.join();
t3.start(); t3.join();
```

**Program 30. Run async tasks and combine results.**
```java
CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> 10);
CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> 20);
int total = f1.thenCombine(f2, Integer::sum).get();   // 30
```

**Program 30a. Thread-safe Singleton.**
```java
public class Singleton {
    private Singleton() {}
    private static class Holder { static final Singleton INSTANCE = new Singleton(); }
    public static Singleton getInstance() { return Holder.INSTANCE; }
}
```
An enum singleton is the most robust option against reflection and serialization attacks.

---

## 3. Java 8 Stream Coding Questions

Assume `record Employee(String name, String dept, double salary) {}` and `List<Employee> emps`.

**Program 31. Separate even and odd numbers.**
```java
Map<Boolean, List<Integer>> parts = nums.stream()
    .collect(Collectors.partitioningBy(n -> n % 2 == 0));
```

`partitioningBy()` is designed for a predicate and creates the `true` and `false` partitions. For comparison, `groupingBy()` can group by a classifier that returns any key type.

**Program 31a. Group numbers by their remainder when divided by 3 (`groupingBy`).**
```java
Map<Integer, List<Integer>> groups = nums.stream()
    .collect(Collectors.groupingBy(n -> n % 3));

// For nums = List.of(1, 2, 3, 4, 5, 6):
// 0 -> [3, 6]
// 1 -> [1, 4]
// 2 -> [2, 5]
```

Here, `n -> n % 3` returns an integer key (`0`, `1`, or `2`), so `groupingBy()` creates one group for each key. Use `partitioningBy()` for a two-way true/false split; use `groupingBy()` when there may be multiple categories.

**Program 32. Second highest salary.**
```java
Optional<Double> second = emps.stream().map(Employee::salary).distinct()
    .sorted(Comparator.reverseOrder()).skip(1).findFirst();
```

**Program 33. Count employees per department, and average salary per department.**
```java
Map<String, Long> count = emps.stream()
    .collect(Collectors.groupingBy(Employee::dept, Collectors.counting()));

Map<String, Double> avg = emps.stream()
    .collect(Collectors.groupingBy(Employee::dept, Collectors.averagingDouble(Employee::salary)));
```

**Program 34. Highest paid employee in each department.**
```java
Map<String, Optional<Employee>> top = emps.stream().collect(
    Collectors.groupingBy(Employee::dept,
        Collectors.maxBy(Comparator.comparingDouble(Employee::salary))));
```

**Program 35. Sort by salary descending, then by name.**
```java
emps.sort(Comparator.comparingDouble(Employee::salary).reversed().thenComparing(Employee::name));
```

**Program 36. Character frequency and first non-repeating character with streams.**
```java
Map<Character, Long> f = s.chars().mapToObj(c -> (char) c)
    .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));

Optional<Character> firstUnique = f.entrySet().stream()
    .filter(e -> e.getValue() == 1).map(Map.Entry::getKey).findFirst();
```

**Program 37. Find duplicate elements.**
```java
Set<Integer> dups = nums.stream()
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
    .entrySet().stream().filter(e -> e.getValue() > 1)
    .map(Map.Entry::getKey).collect(Collectors.toSet());
```

**Program 38. Join, flatten, statistics and a stream palindrome check.**
```java
String joined = names.stream().map(String::toUpperCase).collect(Collectors.joining(", "));

List<Integer> flat = listOfLists.stream().flatMap(List::stream).collect(Collectors.toList());

IntSummaryStatistics st = nums.stream().mapToInt(Integer::intValue).summaryStatistics();
// st.getMin(), st.getMax(), st.getAverage(), st.getSum()

boolean pal = IntStream.range(0, s.length() / 2)
    .allMatch(i -> s.charAt(i) == s.charAt(s.length() - 1 - i));
```

**Program 39. First 10 Fibonacci numbers with streams.**
```java
Stream.iterate(new int[]{0, 1}, f -> new int[]{f[1], f[0] + f[1]})
        .limit(10).map(f -> f[0]).forEach(System.out::println);
```

---

## 4. Data Structure Programs Often Asked in Java Interviews

**Program 40. LRU cache using LinkedHashMap.**
```java
class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;
    LRUCache(int capacity) { super(16, 0.75f, true); this.capacity = capacity; }  // true = access order
    @Override protected boolean removeEldestEntry(Map.Entry<K, V> eldest) { return size() > capacity; }
}
```

**Program 41. Reverse a linked list, detect a cycle, find the middle.**
```java
class Node { int val; Node next; Node(int v) { val = v; } }

static Node reverse(Node head) {
    Node prev = null;
    while (head != null) { Node next = head.next; head.next = prev; prev = head; head = next; }
    return prev;
}

static boolean hasCycle(Node head) {               // Floyd's slow and fast pointers
    Node slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next; fast = fast.next.next;
        if (slow == fast) return true;
    }
    return false;
}

static Node middle(Node head) {
    Node slow = head, fast = head;
    while (fast != null && fast.next != null) { slow = slow.next; fast = fast.next.next; }
    return slow;
}
```

**Program 42. Implement a stack using an array.**
```java
class ArrayStack {
    private final int[] data;
    private int top = -1;
    ArrayStack(int capacity) { data = new int[capacity]; }
    void push(int x) { if (top == data.length - 1) throw new IllegalStateException("Stack full"); data[++top] = x; }
    int pop()  { if (top == -1) throw new IllegalStateException("Stack empty"); return data[top--]; }
    int peek() { if (top == -1) throw new IllegalStateException("Stack empty"); return data[top]; }
    boolean isEmpty() { return top == -1; }
}
```

---

## 5. Time Complexity Cheat Sheet and Interview Habits

- **Complexities to remember:** array access O(1); linear search O(n); binary search O(log n); bubble sort O(n^2); merge sort and `Arrays.sort` on objects O(n log n); HashMap get/put O(1) average; TreeMap O(log n); recursive Fibonacci O(2^n); memoized Fibonacci O(n).
- **Before coding:** repeat the problem, ask about input size, nulls, empty input and negative numbers.
- **While coding:** talk through your approach, start with the simple solution, then improve it (for example brute force two sum O(n^2) to HashMap O(n)).
- **After coding:** dry run with one normal and one edge case, then state time and space complexity.
- **For thread questions:** always mention why you used `while` instead of `if` around `wait()`, which object's lock you are using, and how the program terminates.
- **If you blank out:** write the brute force solution first. A correct slow answer beats an unfinished clever one.
