package concurrencyMultithreading.thread;

public class Task {

    private Object key1 = new Object();
    private Object key2 = new Object();

    public void a() {
        synchronized(key1) {
            System.out.println("Current thread running in a: " + Thread.currentThread().getName());
            b();
        }
    }

    public void b() {
        synchronized(key2) {
            System.out.println("Current thread running in b: " + Thread.currentThread().getName());
            c();
        }
    }

    public void c() {
        synchronized(key1) {
            System.out.println("Current thread running in c: " + Thread.currentThread().getName());
        }
    }
}
