Java does not have a concept of static classes directly, but you can have **static nested classes**. A static nested class is a class that is defined within another class and marked as `static`. It can be accessed without creating an instance of the outer class.

Example:

```
public class OuterClass {

    static class StaticNestedClass {

        void printMessage() {

            System.out.println("Inside static nested class");
    
        }

    }

}
```

You can use the static nested class like this: `OuterClass.StaticNestedClass obj = new OuterClass.StaticNestedClass();`.

### Abstract Class Vs Interface Summary of Differences:

| Feature | Abstract Class | Interface |
| --- | --- | --- |
| **Multiple Inheritance** | No (single inheritance) | Yes (multiple inheritance) |
| **Methods** | Can have both abstract and concrete methods | Can have abstract, default, and static methods (Java 8+) |
| **Constructor** | Can have constructors | Cannot have constructors |
| **Fields** | Can have instance variables | Can only have `public static final` constants |
| **Access Modifiers** | Can have private, protected, public methods | All methods are implicitly `public` |
| **Use Case** | Use when you want shared implementation | Use when you want to define a contract for unrelated classes |

### When to Use Each:

-   **Use an abstract class** when:

    -   You want to share code among several related classes.
    -   You want to allow for default behavior that can be overridden by subclasses.
    -   You need to maintain a common base class with some shared state or functionality.
-   **Use an interface** when:

    -   You want to define a contract that can be implemented by any class.
    -   You want to enable multiple inheritance of functionality across different class hierarchies.
    -   You are defining behavior that is common across many classes with no need for shared state or implementation.

### **Primitive Data Types**

| **Data Type** | **Size** | **Description** |
| --- | --- | --- |
| `byte` | 1 byte (8 bits) | Stores values from -128 to 127. Often used for saving memory in large arrays. |
| `short` | 2 bytes (16 bits) | Stores values from -32,768 to 32,767. |
| `int` | 4 bytes (32 bits) | Stores values from -2^31 to 2^31-1 (i.e., -2,147,483,648 to 2,147,483,647). |
| `long` | 8 bytes (64 bits) | Stores values from -2^63 to 2^63-1. Used for large integers. |
| `float` | 4 bytes (32 bits) | Stores floating-point numbers with 6-7 digits of precision. |
| `double` | 8 bytes (64 bits) | Stores floating-point numbers with 15-16 digits of precision. |
| `char` | 2 bytes (16 bits) | Represents a single Unicode character (i.e., a character from the UTF-16 encoding). |
| `boolean` | 1 byte (but JVM implementation may vary) | Can store only `true` or `false`. Typically, it takes 1 byte in memory, though the JVM might optimize it for storage in arrays. |


-   **Unicode**:

    -   Unicode is a character encoding standard that allows the representation of characters from almost every writing system in the world.
    -   In order to support a wide range of characters from various languages (including special symbols, emojis, etc.), Unicode requires more than just 1 byte per character. UTF-16 is one of the encoding schemes used to represent Unicode characters.-   
    

- **UTF-16 Encoding**:

  -   In UTF-16, most commonly used characters are represented by a single 16-bit unit (i.e., 2 bytes). This is why `char` is 2 bytes.
  -   UTF-16 allows for representing over 65,000 characters using one 16-bit unit, which is more than enough for most characters.

**Unicode is a character set. It translates characters to numbers.**

**UTF-16 is an encoding standard. It translates numbers into binary.**