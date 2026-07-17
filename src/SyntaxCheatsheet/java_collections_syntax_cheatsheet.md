# Java Collections & Data Structures — Syntax Cheat Sheet

Goal: never freeze on syntax again. Organized so you can scan top-to-bottom right before an interview. Every entry is copy-paste-correct, compilable syntax.

---

## 1. The Collection Hierarchy (mental map first)

```
Collection
├── List        → ArrayList, LinkedList
├── Set         → HashSet, LinkedHashSet, TreeSet
└── Queue       → LinkedList, PriorityQueue, ArrayDeque
     └── Deque  → ArrayDeque, LinkedList

Map (separate hierarchy, not a Collection)
└── HashMap, LinkedHashMap, TreeMap
```

**Rule of thumb for interviews:**
- Need order of insertion preserved + fast lookup? → `LinkedHashMap` / `LinkedHashSet`
- Need sorted order? → `TreeMap` / `TreeSet`
- Need O(1) average lookup, don't care about order? → `HashMap` / `HashSet`
- Need FIFO? → `Queue` (`LinkedList` or `ArrayDeque`)
- Need LIFO? → `Deque` (`ArrayDeque`, NOT the legacy `Stack` class)
- Need min/max on the fly? → `PriorityQueue`

---

## 2. ArrayList

```java
List<Integer> list = new ArrayList<>();

list.add(10);                  // append
list.add(0, 5);                // insert at index
list.get(0);                   // read
list.set(0, 99);               // update
list.remove(0);                // remove by INDEX (int arg)
list.remove(Integer.valueOf(99)); // remove by VALUE (careful: autoboxing trap!)
list.size();
list.isEmpty();
list.contains(99);
list.indexOf(99);
list.clear();

// Iteration
for (int x : list) { }
for (int i = 0; i < list.size(); i++) { }
list.forEach(x -> System.out.println(x));

// Sorting
Collections.sort(list);                          // natural order
Collections.sort(list, Collections.reverseOrder()); // descending
list.sort((a, b) -> a - b);                       // custom comparator (ascending)
list.sort(Comparator.reverseOrder());

// Convert
Integer[] arr = list.toArray(new Integer[0]);
List<Integer> fromArray = new ArrayList<>(Arrays.asList(1, 2, 3));
List<Integer> immutableList = List.of(1, 2, 3);   // fixed-size, immutable

// Sublist (VIEW, not a copy — mutating it mutates original)
List<Integer> sub = list.subList(1, 3);           // [1,3) inclusive-exclusive
```

**Gotcha:** `list.remove(1)` on `List<Integer>` removes by **index**, not value 1. Use `list.remove(Integer.valueOf(1))` to remove the value.

---

## 3. LinkedList (as List AND as Deque)

```java
LinkedList<Integer> ll = new LinkedList<>();
ll.addFirst(1);
ll.addLast(2);
ll.removeFirst();
ll.removeLast();
ll.peekFirst();
ll.peekLast();
ll.getFirst();   // throws if empty
ll.getLast();    // throws if empty
```

Use `LinkedList` when you need frequent insert/delete at both ends. For pure stack/queue use, prefer `ArrayDeque` (faster, no legacy overhead).

---

## 4. HashSet / LinkedHashSet / TreeSet

```java
Set<Integer> hs = new HashSet<>();          // no order guarantee
Set<Integer> lhs = new LinkedHashSet<>();   // insertion order preserved
Set<Integer> ts = new TreeSet<>();          // sorted order

hs.add(5);
hs.remove(5);
hs.contains(5);
hs.size();

// TreeSet-specific navigation methods (VERY commonly asked)
TreeSet<Integer> tset = new TreeSet<>(List.of(10, 20, 30, 40));
tset.first();          // 10 (smallest)
tset.last();           // 40 (largest)
tset.higher(20);       // 30  (strictly greater than 20)
tset.lower(20);        // 10  (strictly less than 20)
tset.ceiling(25);      // 30  (>= 25)
tset.floor(25);        // 20  (<= 25)
tset.pollFirst();      // removes & returns smallest
tset.pollLast();       // removes & returns largest

// TreeSet with custom comparator
TreeSet<String> byLength = new TreeSet<>((a, b) -> a.length() - b.length());
```

**Gotcha:** `TreeSet`/`TreeMap` use `compareTo`/`Comparator` for equality, NOT `.equals()`. Two elements that compare as 0 are treated as duplicates even if `.equals()` says they differ.

---

## 5. HashMap / LinkedHashMap / TreeMap

```java
Map<String, Integer> map = new HashMap<>();

map.put("a", 1);
map.get("a");                       // 1, or null if absent
map.getOrDefault("z", 0);           // 0 if "z" absent — avoids null checks
map.containsKey("a");
map.containsValue(1);
map.remove("a");
map.size();
map.isEmpty();

// Very common interview patterns:
map.put("a", map.getOrDefault("a", 0) + 1);      // frequency counting
map.merge("a", 1, Integer::sum);                  // same thing, cleaner
map.computeIfAbsent("a", k -> new ArrayList<>()).add(1); // group-by pattern
map.putIfAbsent("a", 1);                          // insert only if absent

// Iteration — THE THREE WAYS
for (String key : map.keySet()) { }
for (Integer value : map.values()) { }
for (Map.Entry<String, Integer> entry : map.entrySet()) {
    entry.getKey();
    entry.getValue();
}
map.forEach((k, v) -> System.out.println(k + "=" + v));

// LinkedHashMap — preserves insertion order
Map<String, Integer> lhm = new LinkedHashMap<>();

// LinkedHashMap as an LRU cache (real trick used in interviews)
Map<Integer, Integer> lru = new LinkedHashMap<>(16, 0.75f, true) { // true = access order
    protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
        return size() > 3; // capacity = 3
    }
};

// TreeMap — sorted by key
TreeMap<Integer, String> tm = new TreeMap<>();
tm.firstKey();
tm.lastKey();
tm.higherKey(5);
tm.lowerKey(5);
tm.ceilingKey(5);
tm.floorKey(5);
tm.firstEntry();
tm.pollFirstEntry();
```

---

## 6. Stack (use ArrayDeque, not `java.util.Stack`)

```java
// Preferred modern way:
Deque<Integer> stack = new ArrayDeque<>();
stack.push(1);       // add to top
stack.pop();         // remove & return top
stack.peek();        // look at top without removing
stack.isEmpty();

// Legacy way (still fine to use if faster to recall under pressure):
Stack<Integer> legacyStack = new Stack<>();
legacyStack.push(1);
legacyStack.pop();
legacyStack.peek();
```

---

## 7. Queue / Deque (ArrayDeque)

```java
// As a FIFO Queue
Queue<Integer> queue = new ArrayDeque<>();
queue.offer(1);      // add to tail (preferred over add() — doesn't throw)
queue.poll();        // remove & return head (returns null if empty)
queue.peek();        // look at head (returns null if empty)

// As a Deque (both ends)
Deque<Integer> deque = new ArrayDeque<>();
deque.offerFirst(1);
deque.offerLast(2);
deque.pollFirst();
deque.pollLast();
deque.peekFirst();
deque.peekLast();
```

**Gotcha:** `add()`/`remove()`/`element()` throw exceptions on failure; `offer()`/`poll()`/`peek()` return `false`/`null` instead. **Prefer the offer/poll/peek family** in interviews — safer under pressure.

---

## 8. PriorityQueue (min-heap by default)

```java
// Min-heap (default) — smallest element at head
PriorityQueue<Integer> minHeap = new PriorityQueue<>();
minHeap.offer(5);
minHeap.offer(1);
minHeap.offer(3);
minHeap.poll();          // returns 1 (smallest)

// Max-heap — THE SYNTAX YOU ALWAYS FORGET:
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
// OR
PriorityQueue<Integer> maxHeap2 = new PriorityQueue<>((a, b) -> b - a);

// PriorityQueue of custom objects — MOST COMMONLY ASKED PATTERN
class Task {
    int priority;
    String name;
    Task(int priority, String name) { this.priority = priority; this.name = name; }
}
PriorityQueue<Task> taskQueue = new PriorityQueue<>(
    (a, b) -> a.priority - b.priority   // min-heap by priority
);
// or with Comparator.comparing:
PriorityQueue<Task> taskQueue2 = new PriorityQueue<>(
    Comparator.comparingInt(t -> t.priority)
);

// PriorityQueue with initial capacity + comparator (constructor overload trap)
PriorityQueue<Task> pq = new PriorityQueue<>(10, Comparator.comparingInt(t -> t.priority));
```

---

## 9. Comparator & Comparable — the syntax you keep blanking on

### Comparable (natural ordering — defined INSIDE the class)
```java
class Employee implements Comparable<Employee> {
    int salary;
    String name;
    Employee(String name, int salary) { this.name = name; this.salary = salary; }

    @Override
    public int compareTo(Employee other) {
        return this.salary - other.salary;   // ascending by salary
    }
}
List<Employee> employees = new ArrayList<>();
Collections.sort(employees);   // uses compareTo automatically
```

### Comparator (external, flexible, MULTIPLE sort orders for same class)
```java
// Old-school anonymous class (still valid, sometimes clearer under pressure)
Comparator<Employee> bySalary = new Comparator<Employee>() {
    @Override
    public int compare(Employee a, Employee b) {
        return a.salary - b.salary;
    }
};

// Lambda form (preferred — faster to type)
Comparator<Employee> bySalaryLambda = (a, b) -> a.salary - b.salary;

// Comparator.comparing — THE MOST USEFUL, MEMORIZE THIS
Comparator<Employee> byName = Comparator.comparing(e -> e.name);
Comparator<Employee> bySalary2 = Comparator.comparingInt(e -> e.salary); // avoids autoboxing

// Descending
Comparator<Employee> bySalaryDesc = Comparator.comparingInt((Employee e) -> e.salary).reversed();

// Multi-level sort — THE #1 THING PEOPLE FORGET SYNTAX FOR
Comparator<Employee> byDeptThenSalary = Comparator
        .comparing((Employee e) -> e.name)
        .thenComparing(e -> e.salary);

Comparator<Employee> byDeptThenSalaryDesc = Comparator
        .comparing((Employee e) -> e.name)
        .thenComparing(Comparator.comparingInt((Employee e) -> e.salary).reversed());

// Using it to sort
employees.sort(bySalary);
Collections.sort(employees, bySalary);
employees.sort(Comparator.comparingInt(e -> e.salary));

// Sorting a List<int[]> (extremely common in interviews — intervals, pairs, etc.)
List<int[]> intervals = new ArrayList<>();
intervals.sort((a, b) -> a[0] - b[0]);                     // sort by start
intervals.sort(Comparator.comparingInt(a -> a[0]));        // same thing, cleaner
intervals.sort(Comparator.comparingInt((int[] a) -> a[0]).thenComparingInt(a -> a[1]));
```

**Memorize this skeleton** — it covers 90% of Comparator needs in interviews:
```java
Comparator.comparing(x -> x.field)             // ascending by field
          .reversed()                          // flip to descending
          .thenComparing(x -> x.field2)        // tie-breaker
```

---

## 10. Iterator (for safe removal while iterating)

```java
Iterator<Integer> it = list.iterator();
while (it.hasNext()) {
    int val = it.next();
    if (val == 5) {
        it.remove();   // SAFE way to remove during iteration
    }
}
```

**Gotcha:** Never call `list.remove()` directly inside a for-each loop — throws `ConcurrentModificationException`. Use `Iterator.remove()` or `removeIf()`:
```java
list.removeIf(x -> x == 5);   // cleanest modern way
```

---

## 11. Arrays — utility methods

```java
int[] arr = {5, 3, 1, 4, 2};

Arrays.sort(arr);                          // ascending, in-place
Arrays.sort(arr, 1, 4);                    // sort subrange [1,4)

Integer[] boxedArr = {5, 3, 1};
Arrays.sort(boxedArr, Collections.reverseOrder()); // descending — ONLY works on boxed types!

Arrays.toString(arr);                      // "[1, 2, 3, 4, 5]" for printing
Arrays.equals(arr, arr2);
Arrays.fill(arr, 0);                       // fill all with 0
Arrays.copyOf(arr, 10);                    // new array, size 10, extra = 0
Arrays.copyOfRange(arr, 1, 4);             // [1,4)

List<Integer> list = Arrays.asList(1, 2, 3);   // FIXED-SIZE list backed by array — no add/remove!
List<Integer> mutable = new ArrayList<>(Arrays.asList(1, 2, 3)); // mutable copy

// 2D arrays
int[][] grid = new int[3][4];
int[][] grid2 = {{1,2},{3,4}};
for (int[] row : grid2) Arrays.fill(row, 0);
```

**Gotcha:** `Arrays.sort()` on a **primitive** array (`int[]`) only sorts ascending — no comparator overload exists for primitives. To sort descending, either sort ascending then reverse manually, or use `Integer[]` (boxed) with `Collections.reverseOrder()`.

```java
// Reverse a primitive int[] after ascending sort:
Arrays.sort(arr);
for (int i = 0; i < arr.length / 2; i++) {
    int temp = arr[i];
    arr[i] = arr[arr.length - 1 - i];
    arr[arr.length - 1 - i] = temp;
}
```

---

## 12. Collections utility class

```java
Collections.sort(list);
Collections.reverse(list);
Collections.max(list);
Collections.min(list);
Collections.max(list, comparator);
Collections.frequency(list, 5);        // count occurrences of 5
Collections.emptyList();
Collections.singletonList(5);
Collections.unmodifiableList(list);    // read-only view
Collections.swap(list, 0, 1);
Collections.shuffle(list);
Collections.fill(list, 0);
Collections.nCopies(3, "x");           // ["x","x","x"]
```

---

## 13. String ↔ Collection conversions (constantly needed)

```java
// String to char array
char[] chars = "hello".toCharArray();

// String to List<Character>
List<Character> charList = "hello".chars()
        .mapToObj(c -> (char) c)
        .collect(Collectors.toList());

// String split to List
List<String> words = Arrays.asList("a,b,c".split(","));

// List<String> to single String
String joined = String.join(",", List.of("a", "b", "c"));  // "a,b,c"
String joined2 = list.stream().map(String::valueOf).collect(Collectors.joining(","));

// StringBuilder — for building strings in loops (never use String += in a loop)
StringBuilder sb = new StringBuilder();
sb.append("hello");
sb.append(123);
sb.reverse();
sb.deleteCharAt(0);
sb.insert(0, "X");
sb.toString();
sb.setLength(0);   // clear/reset
```

---

## 14. Streams — quick reference (used to simplify code fast in interviews)

```java
List<Integer> nums = List.of(5, 3, 8, 1, 9);

nums.stream().filter(n -> n > 3).collect(Collectors.toList());
nums.stream().map(n -> n * 2).collect(Collectors.toList());
nums.stream().sorted().collect(Collectors.toList());
nums.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
nums.stream().max(Integer::compareTo).get();
nums.stream().min(Integer::compareTo).get();
nums.stream().mapToInt(Integer::intValue).sum();
nums.stream().anyMatch(n -> n > 8);
nums.stream().allMatch(n -> n > 0);
nums.stream().count();
nums.stream().distinct().collect(Collectors.toList());

// Group-by — very commonly needed
Map<Boolean, List<Integer>> partitioned = nums.stream()
        .collect(Collectors.partitioningBy(n -> n % 2 == 0));

Map<Integer, List<String>> groupedByLength = List.of("a", "bb", "cc", "ddd").stream()
        .collect(Collectors.groupingBy(String::length));

// Frequency map via streams
Map<Character, Long> freq = "aabbbcc".chars()
        .mapToObj(c -> (char) c)
        .collect(Collectors.groupingBy(c -> c, Collectors.counting()));
```

---

## 15. Generics quick reminders

```java
class Box<T> {
    private T value;
    public void set(T value) { this.value = value; }
    public T get() { return value; }
}

// Bounded type (must be Comparable)
class Sorter<T extends Comparable<T>> {
    T max(T a, T b) { return a.compareTo(b) > 0 ? a : b; }
}

// Wildcard — accepts any List of Number or its subtypes
void printList(List<? extends Number> list) { }
```

---

## 16. Common Interview Data Structure Recipes (syntax you'll actually type)

**Frequency map:**
```java
Map<Character, Integer> freq = new HashMap<>();
for (char c : s.toCharArray()) freq.merge(c, 1, Integer::sum);
```

**Two-pointer with a Set (e.g., two-sum style):**
```java
Set<Integer> seen = new HashSet<>();
for (int num : nums) {
    if (seen.contains(target - num)) { /* found pair */ }
    seen.add(num);
}
```

**Graph adjacency list:**
```java
Map<Integer, List<Integer>> graph = new HashMap<>();
graph.computeIfAbsent(u, k -> new ArrayList<>()).add(v);
graph.computeIfAbsent(v, k -> new ArrayList<>()).add(u); // if undirected
```

**BFS skeleton:**
```java
Queue<Integer> queue = new ArrayDeque<>();
Set<Integer> visited = new HashSet<>();
queue.offer(start);
visited.add(start);
while (!queue.isEmpty()) {
    int node = queue.poll();
    for (int neighbor : graph.getOrDefault(node, List.of())) {
        if (!visited.contains(neighbor)) {
            visited.add(neighbor);
            queue.offer(neighbor);
        }
    }
}
```

**DFS skeleton (iterative, using Deque as stack):**
```java
Deque<Integer> stack = new ArrayDeque<>();
Set<Integer> visited = new HashSet<>();
stack.push(start);
while (!stack.isEmpty()) {
    int node = stack.pop();
    if (visited.contains(node)) continue;
    visited.add(node);
    for (int neighbor : graph.getOrDefault(node, List.of())) {
        if (!visited.contains(neighbor)) stack.push(neighbor);
    }
}
```

**Top-K elements (PriorityQueue trick):**
```java
PriorityQueue<Integer> minHeap = new PriorityQueue<>();
for (int num : nums) {
    minHeap.offer(num);
    if (minHeap.size() > k) minHeap.poll();
}
// minHeap now holds the k largest elements, smallest of them at head
```

---

## 17. Quick Method-Name Decision Table (when your mind goes blank)

| I want to... | List | Set | Map | Deque/Queue |
|---|---|---|---|---|
| Add | `add(x)` | `add(x)` | `put(k,v)` | `offer(x)` / `offerFirst/Last` |
| Remove | `remove(idx)` | `remove(x)` | `remove(k)` | `poll()` / `pollFirst/Last` |
| Check existence | `contains(x)` | `contains(x)` | `containsKey(k)` | — |
| Peek without removing | `get(idx)` | — | `get(k)` | `peek()` |
| Size | `size()` | `size()` | `size()` | `size()` |
| Is empty | `isEmpty()` | `isEmpty()` | `isEmpty()` | `isEmpty()` |

---

## 18. Things to say out loud if you blank on syntax mid-interview

If you genuinely forget exact method names, say what you want and let the interviewer nudge you — this is normal and better than silently freezing:
- "I want a min-heap here — give me a second, the constructor takes a Comparator..."
- "I need the sorted-order navigation methods on TreeMap — `ceilingKey`/`floorKey`, let me confirm the exact names..."

Talking through *intent* even when syntax is fuzzy still signals strong understanding — interviewers care more about whether you know **which structure and why** than whether you recall a method name instantly.
