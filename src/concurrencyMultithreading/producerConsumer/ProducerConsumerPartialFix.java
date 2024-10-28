package concurrencyMultithreading.producerConsumer;

public class ProducerConsumerPartialFix {

    Object key = new Object();
    private static int buffer[];
    private static final int BUFFER_SIZE = 10;
    private static int count;


    public void produce() {
        synchronized(key) {
            while(isFull(buffer)) {

            }
            buffer[count++] = 1;
        }
    }

    public void consume() {
        synchronized(key) {
            while(isEmpty(buffer)) {

            }
            buffer[--count] = 0;
        }
    }

    private boolean isEmpty(int[] buffer) {
        return count == 0;
    }

    private boolean isFull(int[] buffer) {
        return buffer.length == count;
    }

    public static void main(String[] args) throws InterruptedException {
        ProducerConsumerPartialFix producerConsumer = new ProducerConsumerPartialFix();
        buffer = new int[BUFFER_SIZE];
        count = 0;
        Runnable r1 = () -> {
            for(int i=0; i<50; ++i) {
                producerConsumer.produce();
            }
            System.out.println("done producing");

        };

        Runnable r2 = () -> {
            for(int i=0; i<50; ++i) {
                producerConsumer.consume();
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
