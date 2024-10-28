package subclass.interfaceImpl.mainPackage;

public interface Animal {
    void walk();

    default void name() {
        System.out.println("Animal class name");
    }
}
