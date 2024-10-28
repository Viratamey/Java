package concurrencyMultithreading.barrierAndLatch;

import java.util.concurrent.CountDownLatch;

public class Latches {
    static int num;

    static CountDownLatch latch;
    public static void main(String[] args) throws InterruptedException {
        // only after 6 latch.countDown() calls the code after latch.await() will run
        latch = new CountDownLatch(6);
        num = 5;
        latch.countDown();
        latch.countDown();
        latch.countDown();
        Thread t1= new Thread(() -> task1());
        Thread t2= new Thread(() -> task2());
        Thread t3= new Thread(() -> task3());
        
        t1.start();
        t1.join();
        t2.start();
        t2.join();
        t3.start();
        t3.join();

        latch.await();

        System.out.println("num: " + num);
    }

    private static void task3() {
        System.out.println("in task3");
        latch.countDown();
        num = num+1;
    }

    private static void task2() {
        System.out.println("in task2");
        latch.countDown();
        num = num*2;
    }

    private static void task1() {
        System.out.println("in task1");
        latch.countDown();
        num = num + 10;
    }
}
