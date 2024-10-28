package collections.queue;


import java.util.*;
import java.util.stream.Collectors;

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
        if(this.getName().length() != p2.getName().length()) {
            return this.getName().length() - p2.getName().length();
        } else {
            return this.getName().compareTo(p2.getName());
        }
    }

}


public class QueueImpl {

    public static void main(String[] args) {

        Person p1 = new Person(1,"one");
        Person p2 = new Person(2,"two");
        Person p3 = new Person(3,"three");
        Person p4 = new Person(4,"four");
        Person p5 = new Person(5,"five");
        Person p6 = new Person(6,"six");
        Person p7 = new Person(7,"seven");
        Person p8 = new Person(8,"eight");

        //  LinkedList as List Queue

        Queue<Person> personsQ = new LinkedList<Person>();
        System.out.println(personsQ.addAll(Arrays.asList(p1, p2, p3)));
        System.out.println(personsQ.add(p4));
        System.out.println(personsQ.offer(p6));
        System.out.println(personsQ.peek());
        System.out.println(personsQ.element());
        System.out.println(personsQ.remove());
        System.out.println(personsQ.poll());

        System.out.println(personsQ);
        System.out.println();


        //  PriorityQueue as Queue

        Queue<Person> personsPq = new PriorityQueue<>(Arrays.asList(p2, p3, p1));
        Queue<Person> personsPq1 = new PriorityQueue(Arrays.asList(p2, p3, p1));
        System.out.println(personsPq);
        personsPq.add(p4);
        personsPq.addAll(Arrays.asList(p5,p6));
        personsPq.addAll(Arrays.asList(p8,p7));
        System.out.println(personsPq);
        System.out.println();
        while(!personsPq.isEmpty()) {
            System.out.println(personsPq.poll());
        }
        System.out.println(personsPq);
        personsPq.removeIf(p -> p.getName().length() > 4);
        System.out.println(personsPq);

        Comparator<Person> cmp = (person1, person2) -> person1.getAge()-person2.getAge();
        Comparator<Person> cmp2 = Comparator.comparingInt(Person::getAge);
        Comparator<Person> cmp3 = Comparator.comparing(Person::getAge);
        Comparator<Person> cmp4 = Comparator.comparing(Person::getName);

        Queue<Person> pq = new PriorityQueue<>(cmp);
        pq.addAll(Arrays.asList(p1,p2,p3,p7,p8,p6,p5,p4));
        Queue<Person> pq2 = new PriorityQueue<>(cmp2);
        pq2.addAll(pq);
        Queue<Person> pq3 = new PriorityQueue<>(cmp3);
        pq3.addAll(pq);
        Queue<Person> pq4 = new PriorityQueue<>(cmp3);
        pq4.addAll(pq);
        Iterator<Person> it = pq.iterator();
        it.forEachRemaining(i -> System.out.println(i));
        System.out.println();
        while(!pq.isEmpty()) {
            System.out.println(pq.poll());
        }

        System.out.println();
        while(!pq2.isEmpty()) {
            System.out.println(pq2.poll());
        }

        System.out.println();
        while(!pq3.isEmpty()) {
            System.out.println(pq3.poll());
        }

        System.out.println();
        List<Person> newList = pq4.stream().collect(Collectors.toList());
        System.out.println(newList);
        Collections.sort(newList, Comparator.comparing(Person::getName));
        System.out.println(newList);


    }




}
