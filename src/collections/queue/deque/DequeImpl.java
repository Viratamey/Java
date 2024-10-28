package collections.queue.deque;

import java.util.*;
import java.util.stream.Collectors;

public class DequeImpl {

    static class Person implements Comparable<Person> {

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

    public static void main(String[] args) {
        
        ////  LinkedList as List Deque
        
        Person p1 = new Person(1,"one");
        Person p2 = new Person(2,"two");
        Person p3 = new Person(3,"three");
        Person p4 = new Person(4,"four");
        Person p5 = new Person(5,"five");
        Person p6= new Person(6,"six");
        Person p7 = new Person(7,"seven");
        Person p8= new Person(8,"eight");

        Deque<Person> personsDq = new LinkedList<Person>();

        System.out.println(personsDq.addAll(Arrays.asList(p1, p2, p3)));
        System.out.println(personsDq.add(p4));
        personsDq.addLast(p6);
        personsDq.addFirst(p7);
        System.out.println(personsDq.offer(p8));
        System.out.println(personsDq.offerFirst(p1));
        System.out.println(personsDq.offerLast(p2));

        System.out.println();
        System.out.println(personsDq);

        System.out.println(personsDq.remove(p4));
        System.out.println(personsDq.removeLast());
        System.out.println(personsDq.removeFirst());
        System.out.println(personsDq.poll());
        System.out.println(personsDq.pollFirst());
        System.out.println(personsDq.pollLast());

        System.out.println(personsDq.peek());
        System.out.println(personsDq.peekFirst());
        System.out.println(personsDq.peekLast());
        System.out.println(personsDq.element());
        System.out.println(personsDq.getFirst());
        System.out.println(personsDq.getLast());

        Iterator<Person> desc = personsDq.descendingIterator();
        Iterator<Person> asc = personsDq.iterator();
        asc.forEachRemaining(p -> System.out.print(p));
        System.out.println();
        while(desc.hasNext()) {
            System.out.print(desc.next());
        }

        //  ArrayDequeue as DeQueue

        Deque<Person> adq = new ArrayDeque<>();
        adq.addFirst(p1);
        System.out.println(adq.addAll(Arrays.asList(p1, p2, p3)));
        System.out.println(adq.add(p4));
        adq.addLast(p6);
        adq.addFirst(p7);
        System.out.println(adq.offer(p8));
        System.out.println(adq.offerFirst(p1));
        System.out.println(adq.offerLast(p2));

        System.out.println();
        System.out.println(adq);

        System.out.println(adq.remove(p4));
        System.out.println(adq.removeLast());
        System.out.println(adq.removeFirst());
        System.out.println(adq.poll());
        System.out.println(adq.pollFirst());
        System.out.println(adq.pollLast());

        System.out.println(adq.peek());
        System.out.println(adq.peekFirst());
        System.out.println(adq.peekLast());
        System.out.println(adq.element());
        System.out.println(adq.getFirst());
        System.out.println(adq.getLast());

        Iterator<Person> desc1 = adq.descendingIterator();
        Iterator<Person> asc1 = adq.iterator();
        asc.forEachRemaining(p -> System.out.print(p));
        System.out.println();
        while(desc1.hasNext()) {
            System.out.print(desc1.next());
        }
    }

    


}
