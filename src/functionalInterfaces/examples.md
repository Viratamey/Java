# Java Functional Interfaces - Complete Guide with Examples

## 1. Runnable

**SAM:** `void run()`

### Lambda

``` java
Runnable r = () -> System.out.println("Running...");
r.run();
```

### Anonymous Class

``` java
Runnable r = new Runnable() {
    @Override
    public void run() {
        System.out.println("Running...");
    }
};
```

### Method Reference

``` java
Runnable r = MyClass::printMessage;
```

------------------------------------------------------------------------

## 2. Comparator`<T>`{=html}

**SAM:** `int compare(T o1, T o2)`

### Lambda

``` java
Comparator<String> cmp = (a, b) -> a.length() - b.length();
```

### Anonymous Class

``` java
Comparator<String> cmp = new Comparator<>() {
    @Override
    public int compare(String a, String b) {
        return a.length() - b.length();
    }
};
```

### Method Reference

``` java
Comparator<String> cmp = String::compareToIgnoreCase;
```

### Sorting

``` java
list.sort((a,b) -> a.compareTo(b));
list.sort(Comparator.naturalOrder());
list.sort(Comparator.reverseOrder());
list.sort(Comparator.comparing(String::length));
```

------------------------------------------------------------------------

## 3. Predicate`<T>`{=html}

**SAM:** `boolean test(T t)`

``` java
Predicate<Integer> even = n -> n % 2 == 0;

System.out.println(even.test(10));
```

### Composition

``` java
Predicate<Integer> positive = n -> n > 0;

Predicate<Integer> positiveEven = even.and(positive);
Predicate<Integer> odd = even.negate();
Predicate<Integer> evenOrPositive = even.or(positive);
```

------------------------------------------------------------------------

## 4. Function\<T,R\>

**SAM:** `R apply(T t)`

``` java
Function<String,Integer> length = String::length;
System.out.println(length.apply("Java"));
```

### Composition

``` java
Function<Integer,Integer> square = x -> x*x;
Function<Integer,Integer> addOne = x -> x+1;

Function<Integer,Integer> result = square.andThen(addOne);
Function<Integer,Integer> result2 = square.compose(addOne);
```

------------------------------------------------------------------------

## 5. Consumer`<T>`{=html}

**SAM:** `void accept(T t)`

``` java
Consumer<String> print = System.out::println;
print.accept("Hello");
```

### Chaining

``` java
Consumer<String> upper = s -> System.out.println(s.toUpperCase());

print.andThen(upper).accept("java");
```

------------------------------------------------------------------------

## 6. Supplier`<T>`{=html}

**SAM:** `T get()`

``` java
Supplier<Double> random = Math::random;

System.out.println(random.get());
```

------------------------------------------------------------------------

## 7. Callable`<T>`{=html}

**SAM:** `T call()`

``` java
Callable<Integer> task = () -> 100;

System.out.println(task.call());
```

### ExecutorService

``` java
ExecutorService executor = Executors.newSingleThreadExecutor();

Future<Integer> future = executor.submit(() -> 50);

System.out.println(future.get());

executor.shutdown();
```

------------------------------------------------------------------------

# Stream API Examples

### Predicate

``` java
list.stream()
    .filter(x -> x > 10)
    .toList();
```

### Function

``` java
list.stream()
    .map(String::length)
    .toList();
```

### Consumer

``` java
list.forEach(System.out::println);
```

### Comparator

``` java
list.stream()
    .sorted(Comparator.comparing(String::length))
    .toList();
```

------------------------------------------------------------------------

# Functional Interface Cheat Sheet

  --------------------------------------------------------------------------------
Interface                SAM         Returns         Common Methods
  ------------------------ ----------- --------------- ---------------------------
Runnable                 run()       void            run()

Comparator`<T>`{=html}   compare()   int             comparing(), reversed(),
thenComparing()

Predicate`<T>`{=html}    test()      boolean         and(), or(), negate()

Function\<T,R\>          apply()     R               compose(), andThen(),
identity()

Consumer`<T>`{=html}     accept()    void            andThen()

Supplier`<T>`{=html}     get()       T               get()

Callable`<T>`{=html}     call()      T               submit() via
ExecutorService
  --------------------------------------------------------------------------------

# When to Use

-   **Runnable** → execute a task.
-   **Callable** → execute a task that returns a value.
-   **Predicate** → filtering and condition checking.
-   **Function** → data transformation.
-   **Consumer** → perform an action (printing, saving, logging).
-   **Supplier** → lazy value generation.
-   **Comparator** → sorting and comparing objects.
