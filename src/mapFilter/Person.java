package mapFilter;

public class Person {
    int age;
    String name;

    Person(int age) {
        this.age = age;
    }

    Person(int age, String name) {
        this.age = age;
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String toString() {
        return name;
    }

}
