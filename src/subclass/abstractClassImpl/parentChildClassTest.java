package subclass.abstractClassImpl;

public class parentChildClassTest {
    public static void main(String[] args) {
        Animal animal = new Dog();
        System.out.println(animal.name);
        System.out.println(animal.className);
        System.out.println(animal.parentname);
        animal.staticMethod();
        animal.method();
        System.out.println();

        Dog dog = new Dog();
        System.out.println(dog.name);
        System.out.println(dog.className);
        System.out.println(dog.parentname);
        dog.staticMethod();
        dog.method();
    }
}
