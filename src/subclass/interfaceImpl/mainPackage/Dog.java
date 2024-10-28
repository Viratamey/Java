package subclass.interfaceImpl.mainPackage;

public class Dog implements Animal {
    @Override
    public void walk() {
        System.out.println("dog walking");
    }

    public void dogSpecial() {
        System.out.println("dog special activities");
    }

}
