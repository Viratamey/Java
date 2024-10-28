package subclass.interfaceImpl.mainPackage;

public class parentChildTest {
    public static void main(String[] args) {
        Animal animal = new Dog();
        animal.walk();
        // dog special wont work as reference object is not of dog class
//        animal.dogSpecial();
        Dog dog = (Dog) animal;
        dog.dogSpecial();
        Dog dog1 = new Dog();
        dog1.dogSpecial();

    }
}
