package collections.queue.blockingQueue;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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


public class BlockingQueueImpl {

//    LinkedBlockingQueue as BlockingQueue

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Person> blockq = new LinkedBlockingQueue<>(4);
        BlockingQueue<Person> blockq1 = new ArrayBlockingQueue<>(6);
        Person p1 = new Person(1,"one");
        Person p2 = new Person(2,"two");
        Person p3 = new Person(3,"three");
        Person p4 = new Person(4,"four");
        Person p5 = new Person(5,"five");
        Person p6= new Person(6,"six");
        Person p7 = new Person(7,"seven");
        Person p8= new Person(8,"eight");

        blockq.add(p1);
        blockq.addAll(Arrays.asList(p3,p4));

        System.out.println(blockq.offer(p2));
        System.out.println(blockq.offer(p5, 1, TimeUnit.MILLISECONDS));

        // put will wait till blockq is full at capacity
        blockq.put(p6);

        System.out.println(blockq.remainingCapacity());
        System.out.println(blockq);

        Collection<Person> persons = new ArrayList<>();
        blockq.drainTo(persons);
        System.out.println(blockq);
        System.out.println(blockq.size());
        blockq.addAll(Arrays.asList(p1,p2,p3,p4));
        blockq.drainTo(persons, 3);

        blockq.poll();
        blockq.poll(200, TimeUnit.MICROSECONDS);

        Person p = blockq.take();
        System.out.println(p);
        System.out.println(blockq.size());

        // take will wait till blockq is empty same as put
        p = blockq.take();

        System.out.println(blockq.size());
        System.out.println(p);
        blockq.contains(p1);

        BlockingQueue<Person> blockingQueue = new LinkedBlockingQueue<>();
        Queue<Person> queue = new LinkedList<>();

        int threads = 10;

        // for Blocking queue

        ArrayList<Thread> producersBq = new ArrayList<>(threads);
        System.out.println(producersBq);

        for(int i=0; i<threads; ++i) {
            Runnable r = () -> {
                for(int j=0; j<1000; ++j) {
                    blockingQueue.add(p1);
                }
            };
            producersBq.add(i, new Thread(r));
        }

        for(int i=0; i<threads; ++i) {
            producersBq.get(i).start();
        }

        for(int i=0; i<threads; ++i) {
            producersBq.get(i).join();
        }

        // collections.queue.blockingQueue size will be 5000 as it wont it is thread safe so simultaneous addition wont take place
        System.out.println("collections.queue.blockingQueue.size() : " + blockingQueue.size());


        // for queue

        ArrayList<Thread> producersQ = new ArrayList<>(threads);

        for(int i=0; i<threads; ++i) {
            Runnable r = () -> {
                for(int j=0; j<1000; ++j) {
                    queue.add(p1);
                }
            };
            producersQ.add(i, new Thread(r));
        }

        System.out.println(queue.size());

        for(int i=0; i<threads; ++i) {
            producersQ.get(i).start();
        }

        for(int i=0; i<threads; ++i) {
            producersQ.get(i).join();
        }

        // queue size will not be 5000 as it wont it is not thread safe so simultaneous addition takes place
        System.out.println("queue.size() : " + queue.size());


        /// when queue is empty and if we remove element then it will throw exception
        // for above threads were getting exception and they got terminated
        // for below the main thread will get exception and then main program will be terminated
        while(blockq.size()!=0){
            blockq.remove();
        }
        blockq.remove();
        blockq.add(p1);



    }
}
