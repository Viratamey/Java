package concurrencyMultithreading.thread;

import concurrencyMultithreading.thread.IntWrapperKey;

public class ThreadTestWithKey {
    public static void main(String[] args) throws InterruptedException {
        IntWrapperKey intWrapperKey = new IntWrapperKey(0);
        Runnable runnable = () -> increment(intWrapperKey);
        Thread t1 = new Thread(runnable);
        Runnable runnable2 = () -> increment(intWrapperKey);
        Thread t2 = new Thread(runnable2);

        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(intWrapperKey.getIntWrapper());
    }

    private static void increment(IntWrapperKey i)
    {
        for(int j=0; j<1000; ++j) {
            i.setIntWrapper(i.getIntWrapper() + 1);
        }
    }
}
