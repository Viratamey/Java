package subclass.abstractClassImpl;

public abstract class Animal {

    static String className = "Animal";
    String name = "name";
    String parentname = "parent";

    abstract void walk();

    abstract void Run();

    static void staticMethod() {
        System.out.println("Animal Static method");
    }

    void method() {
        System.out.println("Animal method");
    }

    void talk() {
        System.out.println("Animal talk");
    }
}
