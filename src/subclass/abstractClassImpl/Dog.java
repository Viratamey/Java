package subclass.abstractClassImpl;

public class Dog extends Animal {

    static String className;
    String name= "dog";

    @Override
    void walk() {
        System.out.println("Dog walking");
    }

    @Override
    void Run() {
        System.out.println("Dog Running");
    }

    static void staticMethod() {
        System.out.println("Dog Static method");
    }

    void method() {
        System.out.println("dog method");
    }
}
