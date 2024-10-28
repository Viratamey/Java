package concurrencyMultithreading.thread;

public class ThreadTest {
    public static void main(String[] args) throws InterruptedException {
        IntWrapper intWrapper = new IntWrapper(0);
        Runnable runnable = () -> increment(intWrapper);
        Thread t1 = new Thread(runnable);
        Runnable runnable2 = () -> increment(intWrapper);
        Thread t2 = new Thread(runnable2);

        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(intWrapper.getIntWrapper());

        IntWrapper intWrapperSync = new IntWrapper(0);
        Runnable runnable3 = () -> incrementSync(intWrapperSync);
        Thread t3 = new Thread(runnable3);
        Runnable runnable4 = () -> incrementSync(intWrapperSync);
        Thread t4= new Thread(runnable4);

        t3.start();
        t4.start();
        t3.join();
        t4.join();
        System.out.println(intWrapperSync.getIntWrapper());

    }

    private static void increment(IntWrapper i)
    {
        for(int j=0; j<1000; ++j) {
            i.setIntWrapper(i.getIntWrapper() + 1);
        }
    }

    synchronized private static void incrementSync(IntWrapper i)
    {
        for(int j=0; j<1000; ++j) {
            i.setIntWrapper(i.getIntWrapper() + 1);
        }
    }
}
