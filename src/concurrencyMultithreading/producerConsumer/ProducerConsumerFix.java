package concurrencyMultithreading.producerConsumer;

public class ProducerConsumerFix {

    Object key = new Object();
    private static int buffer[];
    private static final int BUFFER_SIZE = 10;
    private static int count;


    public void produce() throws InterruptedException {
        synchronized(key) {
            while(isFull(buffer)) {
                key.wait();
            }
            buffer[count++] = 1;
            key.notify();
        }
    }

    public void consume() throws InterruptedException {
        synchronized(key) {
            while(isEmpty(buffer)) {
                key.wait();
            }
            buffer[--count] = 0;
            key.notify();
        }
    }

    private boolean isEmpty(int[] buffer) {
        return count == 0;
    }

    private boolean isFull(int[] buffer) {
        return buffer.length == count;
    }

    public static void main(String[] args) throws InterruptedException {
        ProducerConsumerFix producerConsumer = new ProducerConsumerFix();
        buffer = new int[BUFFER_SIZE];
        count = 0;
        Runnable r1 = () -> {
            for(int i=0; i<50; ++i) {
                try {
                    producerConsumer.produce();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("done producing");

        };

        Runnable r2 = () -> {
            for(int i=0; i<44; ++i) {
                try {
                    producerConsumer.consume();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("done consuming");
        };
        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r2);
        t2.start();
        t1.start();
        t1.join();
        t2.join();
        System.out.println(count);
    }
}
