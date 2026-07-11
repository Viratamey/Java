# Java OOP Summary

## 1. Abstract Class

An **abstract class** is a class that **cannot be instantiated**. It serves as a blueprint for subclasses.

```java
abstract class Animal {

    abstract void walk();

    void eat() {
        System.out.println("Animal eating");
    }
}
```

### Characteristics

- Cannot create an object directly.
- Can have abstract methods.
- Can have concrete methods.
- Can have constructors.
- Can have instance variables.
- Can have static variables and methods.

```java
Animal animal = new Animal();   // ❌ Compile-time error
Animal animal = new Dog();      // ✅ Valid
```

---

# 2. Abstract Method

An abstract method has **only a declaration**, no implementation.

```java
abstract void walk();
```

The subclass **must** implement it.

```java
class Dog extends Animal {

    @Override
    void walk() {
        System.out.println("Dog walking");
    }
}
```

---

# 3. Inheritance

```java
class Dog extends Animal
```

Dog inherits:

- Fields
- Methods
- Constructors (via `super()`)

---

# 4. Method Overriding

```java
class Animal {
    void method() {
        System.out.println("Animal");
    }
}

class Dog extends Animal {

    @Override
    void method() {
        System.out.println("Dog");
    }
}
```

```java
Animal animal = new Dog();
animal.method();
```

Output

```
Dog
```

This is **Runtime Polymorphism**.

---

# 5. Runtime Polymorphism

Runtime polymorphism applies **only to instance methods**.

```java
Animal animal = new Dog();
animal.method();
```

### Compile Time

Compiler checks

```
Does Animal have method() ?
```

If yes → compilation succeeds.

### Runtime

JVM checks

```
Actual Object = Dog
```

Calls

```
Dog.method()
```

---

# 6. Why Runtime Polymorphism Doesn't Apply to Fields

```java
class Animal {
    String name = "Animal";
}

class Dog extends Animal {
    String name = "Dog";
}

Animal animal = new Dog();

System.out.println(animal.name);
```

Output

```
Animal
```

Reason:

Fields are **hidden**, not overridden.

Field resolution always uses the **reference type**.

---

# 7. Static Variables

```java
class Animal {
    static String className = "Animal";
}

class Dog extends Animal {
    static String className = "Dog";
}
```

```java
Animal animal = new Dog();

System.out.println(animal.className);
```

Output

```
Animal
```

Static variables belong to the class.

---

# 8. Static Methods

```java
class Animal {

    static void display() {
        System.out.println("Animal");
    }
}

class Dog extends Animal {

    static void display() {
        System.out.println("Dog");
    }
}
```

```java
Animal animal = new Dog();

animal.display();
```

Output

```
Animal
```

Static methods are **hidden**, not overridden.

---

# 9. Resolution Rules

| Member | Runtime Polymorphism |
|----------|----------------------|
| Instance Method | ✅ Yes |
| Instance Variable | ❌ No |
| Static Method | ❌ No |
| Static Variable | ❌ No |

---

# 10. Why Only Methods Are Polymorphic

Methods represent **behavior**.

Fields represent **state**.

Behavior depends on the actual object.

State belongs to the class that declared it.

Example

```java
Animal animal = new Dog();
```

Compiler knows

```
animal is Animal
```

Runtime knows

```
Object is Dog
```

Therefore

```java
animal.walk();
```

can use runtime dispatch.

But

```java
animal.name;
```

already refers to `Animal.name`, so no runtime lookup occurs.

---

# 11. Interface

```java
public interface Animal {

    void walk();

    default void name() {
        System.out.println("Animal name");
    }
}
```

An interface defines a **contract**.

---

# 12. Implementing an Interface

```java
public class Dog implements Animal {

    @Override
    public void walk() {
        System.out.println("Dog walking");
    }

    public void dogSpecial() {
        System.out.println("Dog special");
    }
}
```

---

# 13. Upcasting

```java
Animal animal = new Dog();
```

Reference Type

```
Animal
```

Actual Object

```
Dog
```

Allowed methods

```java
animal.walk();
animal.name();
```

Not allowed

```java
animal.dogSpecial();   // Compile-time error
```

Reason:

Compiler only knows methods declared in `Animal`.

---

# 14. Why `dogSpecial()` Doesn't Work

```java
Animal animal = new Dog();

animal.dogSpecial();
```

Compiler asks

```
Does Animal have dogSpecial() ?
```

Answer

```
No
```

Compilation fails before runtime.

**Important Rule**

Runtime polymorphism can happen **only after** compilation succeeds.

---

# 15. Downcasting

```java
Animal animal = new Dog();

Dog dog = (Dog) animal;

dog.dogSpecial();
```

Now the compiler knows the reference is `Dog`.

Output

```
Dog special
```

---

# 16. ClassCastException

```java
Animal animal = new Cat();

Dog dog = (Dog) animal;
```

Compiles.

Fails at runtime.

```
ClassCastException
```

Safe version

```java
if (animal instanceof Dog) {
    Dog dog = (Dog) animal;
}
```

---

# 17. Compile Time vs Runtime

## Compile Time

Compiler validates using the **reference type**.

Example

```java
Animal animal = new Dog();

animal.walk();
```

Compiler checks

```
Does Animal contain walk() ?
```

---

## Runtime

JVM checks the actual object.

```
Object = Dog
```

Calls

```
Dog.walk()
```

---

# 18. Memory Representation

```java
Animal animal = new Dog();
```

```
Reference (Animal)
        |
        V
+-------------------------+
| Dog Object              |
|-------------------------|
| Animal.name             |
| Dog.name                |
| Animal.method()         |
| Dog.method()            |
+-------------------------+
```

Field access

```java
animal.name;
```

Compiler selects

```
Animal.name
```

Method call

```java
animal.method();
```

Runtime selects

```
Dog.method()
```

---

# 19. Resolution Summary

| Feature | Uses Reference Type | Uses Actual Object |
|----------|---------------------|--------------------|
| Instance Variable | ✅ | ❌ |
| Static Variable | ✅ | ❌ |
| Static Method | ✅ | ❌ |
| Instance Method | ❌ | ✅ |

---

# 20. Key Rules to Remember

### Rule 1

Fields are **hidden**, not overridden.

---

### Rule 2

Static methods are **hidden**, not overridden.

---

### Rule 3

Only **instance methods** support runtime polymorphism.

---

### Rule 4

Compiler decides **whether a method can be called**.

JVM decides **which implementation to execute**.

---

# Interview One-Liner

> **The compiler checks the reference type to determine whether a method exists. If it exists, the JVM performs runtime polymorphism by invoking the overridden method of the actual object. Fields and static members are resolved using the reference type and do not participate in runtime polymorphism.**
