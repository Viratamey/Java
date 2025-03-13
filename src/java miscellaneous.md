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