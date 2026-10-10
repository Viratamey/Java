# Java HashMap Internals — Complete Revision Notes (Java 21)

> **Core mental model:** `hashCode()` helps HashMap find the bucket. The stored hash helps eliminate candidates. `==` checks whether the references point to the same object. `equals()` checks logical key equality. If an equal key already exists, `put()` replaces its value; otherwise, a new mapping is inserted.

These notes consolidate the concepts discussed, with examples, diagrams, implementation details, common traps, and interview-ready summaries.

> 💡 **Reading guide:** Everything from the original PDF is preserved. Blocks marked **"Added clarification"** or **"Added"** are new material I included to remove ambiguity or deepen understanding. You can delete them without losing any original content.

---

## Table of Contents

1. [What is HashMap?](#1-what-is-hashmap)
2. [The three concepts: `hashCode()`, `equals()`, and `==`](#2-the-three-concepts-hashcode-equals-and-)
3. [What happens when HashMap calculates a hash?](#3-what-happens-when-hashmap-calculates-a-hash)
4. [How is the bucket index calculated?](#4-how-is-the-bucket-index-calculated)
5. [Exactly how `put(key, value)` works](#5-exactly-how-putkey-value-works)
6. [Exactly how `get(key)` works](#6-exactly-how-getkey-works)
7. [The three checks in detail](#7-the-three-checks-in-detail)
8. [Hash collision: same bucket does not mean same key](#8-hash-collision-same-bucket-does-not-mean-same-key)
9. [Linked lists and red-black trees](#9-linked-lists-and-red-black-trees)
10. [Resizing, capacity, size, and load factor](#10-resizing-capacity-size-and-load-factor)
11. [Mutable keys: a dangerous mistake](#11-mutable-keys-a-dangerous-mistake)
12. [Null keys and null values](#12-null-keys-and-null-values)
13. [Thread safety and iteration](#13-thread-safety-and-iteration)
14. [Complexity](#14-complexity)
15. [Common interview questions](#15-common-interview-questions)
16. [Final mental model](#16-final-mental-model)
17. [Added: Glossary](#17-added-glossary)
18. [Added: Common traps checklist](#18-added-common-traps-checklist)

---

## 1. What is HashMap?

`HashMap<K, V>` is a hash-table-based implementation of Java's `Map` interface. It stores key-value mappings and provides average constant-time (`O(1)`) lookup, insertion, and removal when hashes are distributed well.

Conceptually, it contains:

- An array of buckets.
- A node (entry) for each key-value mapping.
- A linked list in a bucket when multiple entries map there.
- A red-black tree in a heavily populated bucket when treeification conditions are met.

Simplified node structure:

```java
static class Node<K, V> {
    final int hash;
    final K key;
    V value;
    Node<K, V> next;
}
```

This is a simplified representation of OpenJDK's ordinary linked-bin node. Tree bins use a specialized node with extra tree references.

Example conceptual bucket array:

```text
Bucket 0: null
Bucket 1: [A, 100] -> [B, 200] -> null
Bucket 2: null
Bucket 3: [C, 300] -> null
Bucket 4: [D, 400] -> [E, 500] -> [F, 600] -> null
...
```

Entries share a bucket when their hashes produce the same bucket index. They do not necessarily have the same hash or the same key.

> 💡 **Added clarification — what is a "bucket"?** A bucket is just one slot (index) in the internal array `Node<K,V>[] table`. The slot holds a reference to the *first* node of a chain (or the root of a tree), or `null` if empty. The word "bucket" is also loosely used for "the chain of nodes hanging off that slot."
>
> Also note the `hash` and `key` fields are `final`: a node's stored hash and key reference never change after creation. Only `value` (and `next`) can change. This matters in [Section 11](#11-mutable-keys-a-dangerous-mistake).

---

## 2. The three concepts: `hashCode()`, `equals()`, and `==`

These are related but do different jobs.

| Mechanism | What it checks | How HashMap uses it |
|---|---|---|
| `hashCode()` | Returns an integer hash for an object | Helps determine the bucket |
| Stored hash | The hash saved in an existing node at insertion time | Quickly rules out a node whose hash differs from the lookup hash |
| `==` for references | Whether two references point to the exact same object | Fast path: same object means the key matches |
| `equals()` | Logical equality as defined by the class | Determines whether two different objects represent equal keys |

**Example: different references, equal strings**

```java
String a = new String("Apple");
String b = new String("Apple");

System.out.println(a == b);                       // false: different objects
System.out.println(a.equals(b));                  // true: same string contents
System.out.println(a.hashCode() == b.hashCode()); // true
```

**Example: same hash, unequal strings**

```java
System.out.println("Aa".hashCode() == "BB".hashCode()); // true
System.out.println("Aa".equals("BB"));                   // false
```

This is a hash collision. A matching hash does **not** prove that two keys are equal.

### The `equals()` / `hashCode()` contract

1. If `a.equals(b)` is `true`, `a.hashCode()` and `b.hashCode()` **must** be equal.
2. If two objects have equal hash codes, they are **not necessarily** equal.
3. If two objects are unequal, their hash codes may still be equal.
4. `equals()` and `hashCode()` are not automatically derived from one another. A custom class must implement them consistently when logical equality is required.

The contract is a requirement on class implementations. Java does not automatically fix a class that overrides `equals()` but inherits an inconsistent `hashCode()`.

> 💡 **Added clarification — why `"Aa"` and `"BB"` collide.** `String.hashCode()` is `s[0]*31^(n-1) + ... + s[n-1]`. For two-character strings: `"Aa"` = `65*31 + 97 = 2112` and `"BB"` = `66*31 + 66 = 2112`. Since `'B' - 'A' = 1` and `31 * 1 = 31 = 'a' - 'B'`'s offset (97 − 66), the difference cancels out.
>
> **What goes wrong when you break the contract** (override `equals()` but not `hashCode()`):
>
> ```java
> class P {
>     int id;
>     P(int id) { this.id = id; }
>     @Override public boolean equals(Object o) {
>         return o instanceof P p && p.id == id;
>     }
>     // hashCode() NOT overridden -> uses identity-based Object.hashCode()
> }
>
> Map<P, String> m = new HashMap<>();
> m.put(new P(1), "one");
> System.out.println(m.get(new P(1))); // almost certainly null
> ```
>
> The two `P(1)` objects are `equals()`, but have different identity hash codes, so HashMap looks in the wrong bucket (or, even in the same bucket, the stored hash differs and the node is skipped) and never reaches `equals()`.

---

## 3. What happens when HashMap calculates a hash?

In OpenJDK 21, the hash-spreading method is essentially:

```java
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

For a non-null key:

1. Call the key's `hashCode()`.
2. XOR that hash with its upper 16 bits shifted right by 16.
3. Use the resulting spread hash for bucket selection.

For a `null` key, the hash is `0`.

### Why mix the upper bits?

A HashMap table has a power-of-two length, and bucket selection uses the low bits of the spread hash. Mixing upper bits into lower bits lets more of the original hash influence the bucket index.

The spread hash is not the same thing as a guaranteed unique identifier. Different keys can still have the same spread hash.

> 💡 **Added — worked example of spreading.** Suppose `key.hashCode()` returns `0x12345678`.
>
> ```text
> h          = 0x12345678   = 0001 0010 0011 0100 | 0101 0110 0111 1000
> h >>> 16   = 0x00001234   = 0000 0000 0000 0000 | 0001 0010 0011 0100
> h ^ (h>>>16) = 0x1234444C                          (low 16 bits: 0x5678 ^ 0x1234 = 0x444C)
> ```
>
> With capacity 16 (mask `0xF`):
> - Without spreading: index = `0x8` = 8 (only the lowest 4 bits `1000` of the raw hash matter).
> - With spreading: index = `0xC` = 12 (the high bits have now influenced the result).
>
> For small hashes (e.g. small `Integer` values, where the upper 16 bits are all zero), `h >>> 16` is `0`, so spreading changes nothing.
>
> **Why it matters:** hash codes that differ only in their high bits (a common pattern, e.g. with floating-point values or certain structured IDs) would otherwise all land in the same bucket in a small table.

---

## 4. How is the bucket index calculated?

OpenJDK HashMap uses:

```java
index = (capacity - 1) & hash;
```

For a table capacity of 16:

```text
capacity     = 16
capacity - 1 = 15      (binary 1111)
hash         = 21      (binary 10101)
index        = 15 & 21
             = 5       (binary 0101)
```

So the entry belongs in bucket 5.

### Why use `&` instead of `%`?

When the capacity is a power of two, `hash & (capacity - 1)` selects the same bucket as the corresponding modulo operation for a non-negative hash. The bitwise operation is efficient, and it works with negative hash values too.

> **Important:** HashMap first spreads the key's `hashCode()` and then uses the spread hash in this calculation. A simple hand calculation using the raw `hashCode()` can therefore differ from the actual bucket index.

> 💡 **Added clarification — "works with negative hash values too".** In Java, `%` can return a *negative* result for a negative left operand (e.g. `-7 % 16 == -7`), which would be an invalid array index. The mask `& (capacity - 1)` always yields a value in `[0, capacity - 1]` because it keeps only the low bits, so the sign bit is discarded. That is the sense in which it "works" for negatives: it always produces a valid index.
>
> **Why `capacity - 1` is a mask:** if capacity is `2^k`, then `capacity - 1` is `k` ones in binary (16 → `1111`). ANDing with it keeps just the lowest `k` bits of the hash.

---

## 5. Exactly how `put(key, value)` works

Consider:

```java
Map<String, Integer> map = new HashMap<>();
map.put(new String("Apple"), 100);
map.put(new String("Apple"), 500);
```

The second `put()` does not create a second mapping because the keys are equal.

The broad insertion process is:

1. Calculate the spread hash for the requested key.
2. Allocate the table if it has not yet been allocated.
3. Calculate the bucket index.
4. If the bucket is empty, insert a new node.
5. If the bucket is not empty, inspect its first node:
   - Compare the stored hash with the requested hash.
   - Check whether the stored key and requested key are the same reference (`==`).
   - If needed, call `equals()` to check logical equality.
6. If a matching key is found, replace its value (unless the `put()` variant says otherwise).
7. If no matching key is found, continue through the linked list or search the tree; insert a new node if no equal key exists.
8. Update the map size for a new mapping and check whether resizing is required.

A simplified matching condition used in ordinary linked-bin lookup/insertion paths resembles:

```java
storedHash == requestedHash
    && (storedKey == requestedKey
        || (requestedKey != null && requestedKey.equals(storedKey)))
```

This is a conceptual simplification of the implementation condition, not a replacement for the complete source code.

> 💡 **Added clarification — `&&` short-circuit is the whole story.** Read the condition left to right:
> 1. If the stored hash differs → the whole expression is `false` immediately; neither `==` nor `equals()` is evaluated.
> 2. If hashes match, `==` is tried next. If same reference → `true`, `equals()` is never called.
> 3. Only if the references differ (and the requested key isn't `null`) is `requestedKey.equals(storedKey)` called.
>
> Note the direction: it's the **requested (lookup) key's** `equals()` that is invoked, with the stored key as the argument. For well-behaved (symmetric) `equals()` this makes no difference, but it matters if you write an asymmetric `equals()`.
>
> **Details of step 8 (resize check):** the size is incremented, and if the new size is **greater than** the threshold, the table is resized. At the defaults (capacity 16, threshold 12), the **13th** distinct mapping triggers the resize to 32. Replacing the value of an existing key does *not* increase size and never triggers a resize.

### What if the key already exists?

```java
Map<String, Integer> map = new HashMap<>();
map.put("Apple", 100);
map.put("Banana", 200);
map.put("Apple", 500);

System.out.println(map);        // {Apple=500, Banana=200}
System.out.println(map.size()); // 2
```

The value for `"Apple"` changes from `100` to `500`; the size remains `2`.

For an existing key, ordinary `put()` returns the previous value. If there was no previous mapping, it returns `null`; that return value is ambiguous if the old value itself was `null`.

> 💡 **Added — the key object is NOT replaced.** When `put("Apple", 500)` hits an existing equal key, HashMap keeps the **original key object** already stored in the node and only overwrites the value. This is observable if equal keys are distinct objects:
>
> ```java
> String k1 = new String("Apple");
> String k2 = new String("Apple");
> map.put(k1, 1);
> map.put(k2, 2);   // updates value only; node still references k1
> ```
>
> **"Unless the `put()` variant says otherwise"** refers to methods such as `putIfAbsent(key, value)` (only inserts if the key is absent or currently mapped to `null`) and `computeIfAbsent`, `merge`, `compute`, etc., which have their own replacement rules.

### Same key vs same bucket

- **Equal key:** update the existing mapping.
- **Unequal keys in the same bucket:** retain both mappings.
- **Unequal keys in different buckets:** retain both mappings.

A new node is not decided by whether the hash is different. Equality is decisive once the relevant bucket/node is being examined.

---

## 6. Exactly how `get(key)` works

Example:

```java
Map<String, Integer> map = new HashMap<>();
map.put(new String("Apple"), 100);

Integer result = map.get(new String("Apple"));
System.out.println(result); // 100
```

The broad process:

1. Calculate the requested key's spread hash.
2. Calculate its bucket index.
3. Inspect the first node in that bucket.
4. Compare the node's stored hash with the requested hash.
5. If the hashes match, check reference identity (`==`).
6. If the references differ and the requested key is non-null, call `equals()` as needed.
7. If the node matches, return its value.
8. Otherwise, search the remaining linked-list nodes or follow the tree search path.
9. If no equal key is found, return `null`.

### Does `get()` call `equals()` for every entry?

No. HashMap only searches the selected bucket, not the entire map. It can skip a node if the stored hash differs. It can also succeed through reference identity without calling `equals()`.

For a tree bin, the traversal is different from a linked list, but the hash and key-equality rules still apply.

### Why can `get()` return `null`?

Both cases can return `null`:

```java
map.put("Apple", null);
map.get("Apple");   // null: key exists, value is null
map.get("Missing"); // null: key is absent
```

Use `containsKey()` if you need to distinguish these situations:

```java
map.containsKey("Apple");   // true
map.containsKey("Missing"); // false
```

> 💡 **Added — alternatives to `containsKey()` + `get()`.** Calling both does two lookups. `getOrDefault(key, fallback)` returns `fallback` only when the key is absent *or mapped to null*, so it doesn't fully distinguish the two cases either. When the distinction matters, `containsKey()` is the right tool.

---

## 7. The three checks in detail

### Check A: stored hash equals requested hash

Each node stores the spread hash that was calculated when the key was inserted.

```text
Stored node:
  hash  = 12345
  key   = original key
  value = 100
```

During lookup, HashMap calculates the requested key's spread hash and compares it with the node's stored hash.

- If hashes differ, that node cannot be the matching key under the normal `hashCode()` contract, so HashMap skips its equality comparison.
- If hashes match, the node is a candidate. The hashes alone do not prove key equality.

This is a cheap filter: comparing integers is generally cheaper than comparing complex objects.

> 💡 **Added — why store the hash at all?** Without it, HashMap would have to call `key.hashCode()` again on stored keys (potentially expensive, e.g. for long strings or composite objects) or call `equals()` on every node in the chain. Storing the 32-bit hash makes the "is this even a candidate?" test a single integer comparison. It also allows resize to redistribute nodes without recomputing hashes ([Section 10](#10-resizing-capacity-size-and-load-factor)).

### Check B: reference identity (`==`)

If the hashes match, HashMap can check whether the stored key and requested key are literally the same object.

```java
String a = new String("Apple");
map.put(a, 100);
map.get(a); // Same reference: identity check succeeds
```

When the stored and requested references are the same, HashMap already knows it has found the key and does not need `equals()` for that node.

But:

```java
String a = new String("Apple");
String b = new String("Apple");

map.put(a, 100);
map.get(b); // a == b is false
```

The references differ, so HashMap needs the logical-equality check if the hash matches.

> 💡 **Added — why have a `==` fast path?** It's cheaper than `equals()`, and it also makes lookups robust for objects whose `equals()` is unusual (e.g. a `Double.NaN` key: `Double.equals` treats NaN as equal to itself, but a primitive `NaN == NaN` is false; reference identity guarantees a self-match regardless).

### Check C: `equals()` for logical equality

```java
a.equals(b)
```

If `a` and `b` are different references but equal according to `equals()`, HashMap treats them as the same key.

For two `String` objects containing `"Apple"`:

```java
a == b       // false
a.equals(b)  // true
```

For `put()`, HashMap updates the existing value. For `get()`, it returns the existing value.

If `equals()` is false, the node is not the requested key; HashMap continues searching the bucket.

### Summary of the checks

| Stored hash matches? | Same reference? | `equals()` result (if needed) | Outcome for that node |
|---|---|---|---|
| No | Not relevant | Not called for that node | Skip node |
| Yes | Yes | Not needed | Key matches |
| Yes | No | `true` | Key matches |
| Yes | No | `false` | Continue searching |

This describes the ordinary linked-bin matching path. Tree-bin traversal has its own navigation logic, but still uses hash and equality semantics to locate keys.

---

## 8. Hash collision: same bucket does not mean same key

A collision occurs when different keys map to the same bucket index.

There are two related possibilities:

- Different keys have the **same spread hash**.
- Different spread hashes map to the **same bucket index**.

For example, with a table capacity of 16, imagine two spread hashes of 21 and 37:

```text
(16 - 1) & 21 = 5
(16 - 1) & 37 = 5
```

The hashes differ but the bucket index is the same.

A bucket may look like:

```text
Bucket 5
   |
   v
["Key A", 100] -> ["Key B", 200] -> null
```

When searching, HashMap checks the candidates in that bucket. It uses hashes and equality to distinguish the keys.

> 💡 **Added — binary view of why 21 and 37 collide at capacity 16.**
>
> ```text
> 21 = 0001 0101
> 37 = 0010 0101
> mask 15 = 0000 1111
> 21 & 15 = 0101 = 5
> 37 & 15 = 0101 = 5     <- only the low 4 bits are used
> ```
>
> The stored hashes (21 vs 37) still differ, so in this bucket HashMap can skip one node by the stored-hash check alone, with no `equals()` call. After a resize to 32 (mask `1 1111`), 21 → 21 and 37 → 5, so they separate.

### The classic String collision example

```java
System.out.println("Aa".hashCode() == "BB".hashCode()); // true
System.out.println("Aa".equals("BB"));                   // false

Map<String, Integer> map = new HashMap<>();
map.put("Aa", 100);
map.put("BB", 200);

System.out.println(map.size()); // 2
```

Both mappings remain because the keys are unequal.

If you then run:

```java
map.put("Aa", 999);
```

Only the `"Aa"` mapping is updated; `"BB"` remains unchanged.

> 💡 **Added — here the stored hashes are identical.** `"Aa"` and `"BB"` both have `hashCode() == 2112` (spread hash also 2112, since the upper 16 bits are zero), so they land in the same bucket **and** pass the stored-hash check. Only `equals()` can tell them apart. This is the case where the three checks all come into play.

---

## 9. Linked lists and red-black trees

In Java 8 and later, a heavily populated bucket can be converted from a linked list to a red-black tree.

Important OpenJDK 21 implementation constants:

| Constant | Value | Meaning |
|---|---|---|
| `TREEIFY_THRESHOLD` | `8` | Threshold used by insertion logic when considering treeification |
| `UNTREEIFY_THRESHOLD` | `6` | Threshold used when splitting a tree bin during resize to consider reverting to a list |
| `MIN_TREEIFY_CAPACITY` | `64` | Minimum table capacity for treeification |

Do not memorize these as universal rules such as "the ninth entry always becomes a tree." The exact path depends on the insertion logic and table capacity.

- If the table is smaller than 64, HashMap generally resizes rather than treeifying the bin.
- Once the relevant conditions are met, a bucket can become a red-black tree.
- During resize, a tree bin can split; a smaller partition can revert to a linked list.
- Tree-removal logic can also cause a tree bin to revert to a list.

A red-black tree is self-balancing. Searching a heavily collided tree bin can be approximately `O(log n)`, whereas searching a linked list can take `O(n)`.

> 💡 **Added clarification — how to read the thresholds.**
>
> - **Treeify (list → tree):** while inserting into a long chain, the insertion code checks whether the chain has grown to the treeify threshold. In the usual path this check fires when the chain would reach **9 nodes** (i.e. there were already 8). *At that point* it calls `treeifyBin`, which then asks: is the table length at least 64?
>   - **No (table < 64):** it **resizes** the table instead (spreading entries out), and the bin stays a list for now.
>   - **Yes:** the bin is converted into a red-black tree.
> - **Untreeify (tree → list):** the threshold is **6**, deliberately lower than 8. The gap (hysteresis) stops a bin from flipping back and forth between list and tree if its size hovers around one number.
> - **Why trees at all?** If many keys have bad (or maliciously chosen) hash codes and pile into one bucket, a chain degrades lookup to `O(n)`. Trees cap that at roughly `O(log n)`, which also blunts hash-collision denial-of-service attacks.
> - **How tree bins order keys:** primarily by the (spread) hash. When hashes tie, if the keys are the same class and implement `Comparable`, `compareTo` is used to order them. Otherwise, a tie-breaker based on class name and identity hash is used. This is why making keys `Comparable` can help performance in collision-heavy maps. Because ties on `compareTo` can still occur, lookup may need to search both subtrees in rare cases.
> - Ordinary bins are tiny in practice. With good hash distribution, treeification is rare.

---

## 10. Resizing, capacity, size, and load factor

### Capacity vs size

- **Capacity:** number of buckets in the table.
- **Size:** number of key-value mappings currently in the map.

A map can have capacity 32 and size 10.

### Defaults

For a default-created HashMap:

```java
Map<Integer, String> map = new HashMap<>();
```

- Default initial capacity: 16.
- Default load factor: 0.75.
- The internal table is normally allocated lazily on first insertion, not when the empty HashMap object is constructed.

### Threshold

```text
threshold = capacity × load factor
```

At capacity 16 and load factor 0.75:

```text
threshold = 16 × 0.75 = 12
```

When the size exceeds the threshold, HashMap normally doubles the table capacity to 32 and updates the threshold. The exact behavior also depends on the implementation state and constructor configuration.

> 💡 **Added — threshold progression at defaults.**
>
> | Capacity | Threshold (×0.75) | Resize triggered when size becomes |
> |---|---|---|
> | 16 | 12 | 13 |
> | 32 | 24 | 25 |
> | 64 | 48 | 49 |
> | 128 | 96 | 97 |
>
> **Load factor trade-off:** a lower load factor (e.g. 0.5) means fewer collisions but more memory and more frequent resizes; a higher one (e.g. 0.9) saves memory but increases collisions and lookup cost. `0.75` is a general-purpose compromise.

### What happens to entries during resize?

HashMap stores the spread hash in each node, so it can redistribute entries without calling each key's `hashCode()` again during the ordinary resize transfer.

When capacity doubles, an entry either:

- stays at its old index, or
- moves to `oldIndex + oldCapacity`.

This follows from the extra bit introduced by the doubled power-of-two capacity.

Resizing costs work because entries need to be redistributed. It contributes to the cost of some individual insertions even though `put()` is usually `O(1)` on average.

> 💡 **Added — worked example of "stay or move".** Old capacity 16 (mask `0 1111`), new capacity 32 (mask `1 1111`). The only new bit examined is bit 4 (value 16) of the hash.
>
> ```text
> hash 5  = 0 0101 -> bit4 = 0 -> stays at index 5
> hash 21 = 1 0101 -> bit4 = 1 -> moves to 5 + 16 = 21
> ```
>
> So every old bucket splits into exactly two new buckets: `j` and `j + oldCapacity`. Relative order within each half is preserved. This is why powers of two make resizing cheap and why no rehash call is needed.
>
> **Amortised cost:** a resize is `O(n)`, but because capacity doubles, resizes become exponentially rarer, so the average `put()` cost stays `O(1)` amortised.

### Choosing an initial capacity

If you expect approximately 1,000 entries and use the default load factor, you can size the constructor to reduce repeated resizing:

```java
int expectedSize = 1_000;
int capacity = (int) Math.ceil(expectedSize / 0.75);
Map<Integer, String> map = new HashMap<>(capacity);
```

HashMap rounds its internal table length to a power of two. The constructor argument is a requested capacity, not a promise that the internal array will have exactly that length.

> 💡 **Added — Java 19+ shortcut.** Java 19 introduced a factory that does this math for you, which is available in Java 21:
>
> ```java
> Map<Integer, String> map = HashMap.newHashMap(1_000);
> ```
>
> **Worked numbers:** `1000 / 0.75 = 1333.33` → `1334` requested → rounded up to the next power of two → internal table of **2048** buckets (threshold 1536), so 1,000 entries never trigger a resize.
>
> **Common mistake:** `new HashMap<>(1000)` does *not* mean "room for 1000 entries without resizing." It requests ~1024 buckets with a threshold of 768, so it would resize while filling to 1000.
>
> **Lazy allocation:** the first `put()` calls the resize routine, which allocates the initial table (16 by default, or the rounded-up requested capacity).

---

## 11. Mutable keys: a dangerous mistake

A key's fields that participate in `equals()` and `hashCode()` should not change while the object is stored in a HashMap.

```java
class Employee {
    int id;

    Employee(int id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Employee other)) return false;
        return this.id == other.id;
    }
}
```

Example:

```java
Map<Employee, String> map = new HashMap<>();

Employee e = new Employee(10);
map.put(e, "Amey");

e.id = 20;

System.out.println(map.get(e)); // Typically null
```

### Why?

1. When inserted, the node stores the hash based on ID 10 and is placed in the corresponding bucket.
2. The key is mutated to ID 20.
3. A later lookup computes a hash based on ID 20.
4. HashMap may search a different bucket, so it cannot find the old node.

HashMap does not automatically relocate a node when a key changes.

> **Best practice:** use immutable keys, or do not mutate fields involved in equality and hashing while the key is stored in the map.

> 💡 **Added — it's actually worse than "typically null".** There are *two* independent ways the lookup fails:
>
> 1. **Different bucket:** the lookup hash (for ID 20) may point to another bucket than where the node sits.
> 2. **Even if it's the same bucket:** the node's *stored* hash is still the old one (10). The final stored-hash check compares `10` with `20`, fails, and the node is skipped.
>
> And the reverse lookup also fails:
>
> ```java
> map.get(new Employee(10)); // right bucket + right stored hash,
>                            // but equals() compares 10 vs the stored key's NEW id 20 -> false -> null
> ```
>
> So the entry becomes effectively **unreachable by key** (a silent memory leak) yet still counts in `size()` and shows up when iterating. This is why `String`, `Integer`, `UUID`, records with immutable fields, etc. make good keys.
>
> **Related tip:** Java `record`s auto-generate consistent `equals()`/`hashCode()` from their components, and are good key types provided the component values themselves are immutable.

---

## 12. Null keys and null values

A standard `HashMap` allows:

- One `null` key.
- Multiple keys with `null` values.

```java
Map<String, Integer> map = new HashMap<>();
map.put(null, 10);
map.put("Apple", null);
map.put("Banana", 20);
```

The null key has a hash of zero in the implementation.

Because `get()` returns `null` for both an absent key and a key mapped to null, use `containsKey()` to distinguish them.

> 💡 **Added:** the null key therefore always goes to **bucket 0** (since `(capacity - 1) & 0 == 0`). Matching for a null key relies on the `==` check (`null == null`); `equals()` is never invoked on a null requested key (this is the `requestedKey != null &&` guard in the matching condition). Other maps differ: `Hashtable`, `ConcurrentHashMap` and `Map.of(...)` reject null keys, while `TreeMap` rejects null keys with natural ordering.

---

## 13. Thread safety and iteration

`HashMap` is not thread-safe for concurrent modification by multiple threads without synchronization.

Possible alternatives:

| Requirement | Option |
|---|---|
| Ordinary single-threaded use | `HashMap` |
| Concurrent access with concurrent updates | `ConcurrentHashMap` |
| Synchronized access through a wrapper | `Collections.synchronizedMap(...)` |

`ConcurrentHashMap` does not permit null keys or null values.

HashMap iterators are generally fail-fast when they detect structural modifications outside the iterator. This is best-effort behavior, not a synchronization mechanism or a guarantee that concurrent misuse will always be detected.

> 💡 **Added clarifications**
>
> - **What can go wrong without synchronization:** lost updates, a stale or inconsistent `size()`, entries that disappear, and corrupted internal structure during a concurrent resize. Don't rely on "it worked in testing."
> - **"Structural modification"** means adding or removing a mapping (anything that changes size). Overwriting the value of an existing key is *not* structural.
> - **How fail-fast works:** the map keeps a `modCount` counter incremented on structural changes. Iterators remember the value at creation and throw `ConcurrentModificationException` if it later differs. Safe removal during iteration: use `Iterator.remove()`, `map.entrySet().removeIf(...)`, or `map.keySet().removeIf(...)`.
>
> ```java
> for (String k : map.keySet()) {
>     map.remove(k);       // typically throws ConcurrentModificationException
> }
> ```
>
> - **Iteration order:** it follows the table (bucket index order, then chain order). It looks arbitrary, and it can **change after a resize**. Never depend on it.

---

## 14. Complexity

| Operation | Typical / expected | Collision-heavy bucket |
|---|---|---|
| `get()` | `O(1)` average | `O(n)` for a list; approximately `O(log n)` for a tree bin in suitable conditions |
| `put()` | `O(1)` average, excluding occasional resize cost | List traversal can be `O(n)`; tree bins improve search |
| `remove()` | `O(1)` average | Depends on bucket structure and entries in the bucket |

These are practical complexity summaries, not guarantees for every key type or pathological case.

> 💡 **Added:**
> - Iterating a HashMap costs `O(capacity + size)`, since empty buckets must be skipped. An oversized initial capacity makes iteration slower.
> - `containsKey()` has the same cost profile as `get()`. `containsValue()` is `O(n)`, because values are not hashed.
> - A resize is `O(n)` for that one operation, but amortised across inserts, `put()` remains `O(1)`.

---

## 15. Common interview questions

### Q1. What happens if you insert the same key twice?

The second `put()` updates the existing value. It does not add a duplicate mapping.

```java
map.put("Apple", 100);
map.put("Apple", 500);
// Apple -> 500
```

### Q2. What if two different objects have the same hash code?

They can share a bucket. HashMap uses identity and equality checks to determine whether they are the same logical key. Unequal keys can coexist in one bucket.

### Q3. Does a different object reference mean a different key?

No. Two different objects can be logically equal. For example, two different `String` objects containing `"Apple"` compare equal.

### Q4. Does the same hash code mean `equals()` is true?

No. `"Aa"` and `"BB"` have the same hash code but are unequal.

### Q5. Does HashMap always call `equals()`?

No. It may skip a node because its stored hash differs, or succeed because the references are identical. It calls `equals()` when needed.

### Q6. Does HashMap compare every key during `get()`?

No. It selects a bucket and searches only the entries in that bucket or follows the tree search path.

### Q7. Why override both `equals()` and `hashCode()`?

Because equal keys must have equal hash codes. Otherwise, HashMap may place logically equal objects in different buckets and fail to behave as expected.

### Q8. Why are capacities powers of two?

This supports efficient bucket indexing with a bit mask and efficient redistribution during doubling.

### Q9. What is the default load factor?

`0.75`.

### Q10. Does HashMap preserve insertion order?

No. Use `LinkedHashMap` if predictable insertion-order iteration is required.

### Q11. Is HashMap thread-safe?

No. Use an appropriate concurrent or synchronized map when shared mutable access requires it.

### Q12. What changed in Java 8?

HashMap introduced tree bins to improve behavior when buckets become heavily populated. Java 21 retains this general design.

> 💡 **Added — more questions worth practicing**
>
> **Q13. What is the difference between `HashMap`, `LinkedHashMap`, and `TreeMap`?**
> `HashMap`: no order guarantee, `O(1)` average. `LinkedHashMap`: a HashMap plus a doubly-linked list through entries, giving insertion order (or optionally access order, useful for LRU caches). `TreeMap`: sorted by key (natural ordering or a `Comparator`), `O(log n)` operations, and it uses `compareTo`/`compare` for key equality instead of `equals()`/`hashCode()`.
>
> **Q14. HashMap vs Hashtable?**
> `Hashtable` is legacy: synchronized on every method, no null keys or values. Prefer `HashMap` (single-threaded) or `ConcurrentHashMap` (concurrent).
>
> **Q15. What makes a good key?**
> Immutable, with a consistent `equals()`/`hashCode()` pair and a well-distributed hash.
>
> **Q16. What is the effect of a terrible `hashCode()` (e.g. always returns 1)?**
> Everything lands in one bucket. Lookups degrade to a chain scan, or tree search once treeified (about `O(log n)` if keys are `Comparable`; otherwise worse). Correctness is preserved, only performance suffers.
>
> **Q17. Can `equals()`-equal keys end up in different buckets?**
> Only if the class violates the contract (or a key was mutated after insertion). With a correct contract, equal keys always have equal hashes and therefore the same bucket.

---

## 16. Final mental model

### `put(key, value)`

```text
key
 |
 v
key.hashCode()
 |
 v
spread hash
 |
 v
bucket index
 |
 v
inspect bucket
 |
 +--> stored hash differs --> skip that node
 |
 +--> hash matches and same reference --> existing key
 |
 +--> hash matches, different reference --> call equals() if needed
          |
   +------------+------------+
   |                         |
equals true             equals false
   |                         |
update value            continue search
                             |
                     no equal key found?
                             |
                        insert node
```

### `get(key)`

```text
key
 |
 v
spread hash -> bucket index
 |
 v
inspect candidate node(s)
 |
 +--> stored hash differs --> skip node
 |
 +--> same reference --> return value
 |
 +--> different reference --> equals() if needed
          |
   +--------+--------+
   |                 |
 true              false
   |                 |
return value    continue search
                     |
              no match -> return null
```

### Five rules to remember

1. **`hashCode()` helps locate the bucket; it is not a unique ID.**
2. **The stored hash quickly filters candidate nodes.**
3. **`==` checks whether the references point to the same object.**
4. **`equals()` checks logical equality when needed.**
5. **Same logical key means update; same bucket alone does not.**

### One-line interview answer

> Java HashMap spreads a key's hash, calculates a bucket index, checks candidate nodes using the stored hash and reference identity, and uses `equals()` when needed to establish key equality. `put()` updates an existing equal key or inserts a new mapping; `get()` returns the matching value or `null`.

---

## 17. Added: Glossary

| Term | Meaning |
|---|---|
| **Bucket / bin** | One slot in the internal table array; holds the head of a chain or tree root (or `null`). |
| **Node / entry** | An object holding `hash`, `key`, `value`, `next` for one mapping. |
| **Spread hash** | `hashCode()` XOR its own upper 16 bits (shifted down); what HashMap actually stores and indexes with. |
| **Index** | `(capacity - 1) & spreadHash`; the bucket chosen for the key. |
| **Collision** | Two different keys mapping to the same bucket (they may or may not share the same hash). |
| **Capacity** | Number of buckets in the table (always a power of two). |
| **Size** | Number of key-value mappings stored. |
| **Load factor** | Fraction of capacity allowed to fill before resizing (default `0.75`). |
| **Threshold** | `capacity × loadFactor`; resize happens when size exceeds it. |
| **Treeification** | Converting a long chain into a red-black tree. |
| **Structural modification** | Adding or removing a mapping (changes size); basis of fail-fast iteration. |
| **Fail-fast** | Iterator behavior of throwing `ConcurrentModificationException` upon detecting outside structural changes (best-effort). |

---

## 18. Added: Common traps checklist

- ❌ Overriding `equals()` without `hashCode()` (or vice versa).
- ❌ Assuming "same hash ⇒ equal" or "same bucket ⇒ same key."
- ❌ Assuming "different reference ⇒ different key" (`==` is only a fast path).
- ❌ Mutating a key's equality/hash fields after insertion.
- ❌ Treating `get() == null` as "key absent" (value may be `null`).
- ❌ Relying on iteration order of a `HashMap`.
- ❌ Sharing a plain `HashMap` between threads for writes.
- ❌ Modifying the map directly while iterating over it.
- ❌ Assuming `new HashMap<>(n)` has room for exactly `n` entries without resizing.
- ❌ Memorizing treeify/untreeify numbers as absolute rules (depends on table capacity ≥ 64 and insertion path).
- ❌ Hand-computing bucket indexes from raw `hashCode()` and forgetting the spread step.
