# Functional Interfaces in Java -- Summary

## What is a Functional Interface?

A **functional interface** is an interface that contains **exactly one
abstract method (SAM - Single Abstract Method)**.

It can have: - One abstract method - Any number of `default` methods -
Any number of `static` methods - Methods inherited from `Object`
(`toString()`, `equals()`, `hashCode()`)

## Why are Functional Interfaces Used?

They allow you to use **lambda expressions** and **method references**,
making code shorter and more readable.

Example:

``` java
Runnable r = () -> System.out.println("Hello");
```

## Single Abstract Method (SAM)

The only abstract method in a functional interface is called the
**Single Abstract Method (SAM)**.

``` java
@FunctionalInterface
interface MyInterface {
    void display();
}
```

## `@FunctionalInterface` Annotation

``` java
@FunctionalInterface
interface MyInterface {
    void display();
}
```

The annotation ensures the interface contains only one abstract method.

## Rules

A functional interface: - Must have exactly one abstract method. - Can
have multiple default methods. - Can have multiple static methods. - Can
extend another functional interface if only one abstract method remains.

## Common Functional Interfaces

Interface         SAM                     Purpose
  ----------------- ----------------------- ----------------------------
`Runnable`        `run()`                 Execute a task
`Comparator<T>`   `compare(T o1, T o2)`   Compare objects
`Callable<T>`     `call()`                Return a value from a task
`Predicate<T>`    `test(T t)`             Check a condition
`Function<T,R>`   `apply(T t)`            Transform data
`Consumer<T>`     `accept(T t)`           Consume data
`Supplier<T>`     `get()`                 Supply data

## Example: Custom Functional Interface

``` java
@FunctionalInterface
interface Greeting {
    void sayHello();
}

public class Main {
    public static void main(String[] args) {
        Greeting g = () -> System.out.println("Hello!");
        g.sayHello();
    }
}
```

## Example: Comparator

``` java
Comparator<String> byLength =
    (s1, s2) -> s1.length() - s2.length();

System.out.println(byLength.compare("cat", "elephant"));
```

`Comparator` is a functional interface because its SAM is:

``` java
int compare(T o1, T o2);
```

## Key Points

-   Functional Interface = **Exactly one abstract method (SAM)**.
-   Used with **lambda expressions** and **method references**.
-   `@FunctionalInterface` helps enforce the rule.
-   Default and static methods are allowed.
-   Common examples: `Predicate`, `Function`, `Consumer`, `Supplier`,
    `Runnable`, `Callable`, and `Comparator`.

> **Mnemonic:** **SAM = Single Abstract Method → Lambda implements the
> SAM.**
