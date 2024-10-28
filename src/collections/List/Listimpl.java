package collections.List;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

class Person implements Comparable<Person> {

    Person(int age, String name) {
        this.age = age;
        this.name = name;
    }
    private int age;
    private String name;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Person getPerson() {
        return this;
    }

    @Override
    public String toString() {
        return "Person{" +
                "age=" + age +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int compareTo(Person p2) {
        if(this.getAge() != p2.getAge()) {
            return this.getName().length() - p2.getName().length();
        } else {
            return this.getName().compareTo(p2.getName());
        }
    }
}

public class Listimpl {

    public static void main(String[] args) {

        ////  ArrayList as List


        List<Person> persons = new ArrayList<Person>();
        // ensureCapacity is specific to ArrayList
//        persons.ensureCapacity(100);

        Person p1 = new Person(11,"rohit");
        Person p2 = new Person(34,"yuvi");
        Person p3 = new Person(14,"pant");
        Person p4 = new Person(44,"virat");
        persons.addAll(Arrays.asList(p1, p2, p3));
        persons.add(p4);
        Person p5 = new Person(27,"sreesanth");
        Person p6= new Person(11,"hardik");
        Person p7 = new Person(27,"rishabh");
        Person p8= new Person(11,"dhoni");
        ArrayList<Person> anotherPersons = new ArrayList<Person>(Arrays.asList(p5, p6, p7, p8)); // initialise
        persons.addAll(2, anotherPersons); //add list at specific index
        System.out.println(persons);
        System.out.println("length of list: " + persons.size());
        System.out.println("is list empty: " + persons.isEmpty());
        System.out.println("is p1 present: " + persons.contains(p1));
        System.out.println("is Persons present: " + persons.contains(new Person(11,"p1")));
        System.out.println("get second person in the list: " + persons.get(1));
        System.out.println("get index of p4 : " + persons.indexOf(p4));
        System.out.println("get last index of p1 : " + persons.lastIndexOf(p1));

        Comparator<Person> cmp = (person1, person2) -> { return person1.getAge()-person2.getAge();};
        persons.sort(cmp);
        System.out.println(persons);
        persons.sort(Comparator.comparing(Person::getName));
        System.out.println(persons);
        // Faster than comparing as it does boxing and unboxing to Integer from primitive int
        persons.sort(Comparator.comparingInt(Person::getAge));
        System.out.println(persons);
        persons.sort(Comparator.comparing(Person::getAge));

        persons.addAll(persons);
        Comparator<Person> strcmp = Comparator.comparing(Person::getName);
        Comparator<Person> ageNamecmp = cmp.thenComparing(strcmp);
        persons.sort(ageNamecmp);
        System.out.println(persons);

        Function<Person, Integer> fn = (person1) ->  {return person1.getName().length();} ;
        BiFunction<Person, Person, Integer> fn1 = (person1, person2) ->  {return person1.getName().length()-person2.getName().length();} ;
        Comparator<Person> cmp2 = Comparator.comparing(fn);
//        Comparator<Person> cmp3 = Comparator.comparing(fn1);  // comparing doesnt take bifunction only function
        Function<Person, Person> fn2 = (person1) -> person1;
        // wont work if Person is not comparable let make it comparable
//        Comparator<Person> cmp3 = Comparator.comparing(fn2);
        Comparator<Person> cmp3 = Comparator.comparing(fn2);
        persons.sort(cmp3);

        persons.remove(1);
        persons.remove(p1);
        System.out.println(persons);
        System.out.println(persons.subList(2,4));

        Stream<Person> streams = persons.stream();
        System.out.println(streams.count());
//        streams.count();  // will fail as stream is treminated by count();

        streams = persons.stream();
        System.out.println(streams.filter(person -> person.getAge() > 30).count());

        streams = persons.stream();
        System.out.println(streams.reduce((person1,person2) -> new Person(person1.getAge() + person2.getAge() , "result"))) ;

        Iterator<Person> it = persons.iterator();
//        it.remove(); will throw exception;
        it.next();
        it.remove();
        it.forEachRemaining(p -> System.out.println(p.getName()));



        ////  LinkedList as List

        List<Person> personList = new LinkedList<Person>();
        personList.addAll(Arrays.asList(p1, p2, p3));
        personList.add(p4);
        List<Person> anotherPersonsList = new LinkedList<Person>(Arrays.asList(p5, p6, p7, p8)); // initialise
        personList.addAll(2, anotherPersonsList); //add list at specific index
        System.out.println(personList);
        System.out.println("length of list: " + personList.size());
        System.out.println("is list empty: " + personList.isEmpty());
        System.out.println("is p1 present: " + personList.contains(p1));
        System.out.println("is Persons present: " + personList.contains(new Person(11,"p1")));
        System.out.println("get second person in the list: " + personList.get(1));
        System.out.println("get index of p4 : " + personList.indexOf(p4));
        System.out.println("get last index of p1 : " + personList.lastIndexOf(p1));
        System.out.println("remove element at index 2: " + personList.remove(2));
        System.out.println("remove element p1 : " + personList.remove(p1));
        System.out.println("get last index of p1 : " + personList.remove(p1));

        personList.sort(cmp3);
        System.out.println(personList);
        personList.sort(ageNamecmp);
        System.out.println(personList);
    }

}
