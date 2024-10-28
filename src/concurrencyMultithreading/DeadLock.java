package concurrencyMultithreading;

import concurrencyMultithreading.thread.Task;

public class DeadLock {
    public static void main(String[] args) throws InterruptedException {
        Task task = new Task();
        Runnable r1 = () -> task.a();
        Runnable r2 = () -> task.b();
        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r2);

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}
