package concurrencyMultithreading.producerConsumer;

public class ProducerConsumer {
    private static int buffer[];
    private static final int BUFFER_SIZE = 10;
    private static int count;


    public void produce() {
        while(isFull(buffer)) {

        }
        buffer[count++] = 1;
    }

    public void consume() {
        while(isEmpty(buffer)) {

        }
        buffer[--count] = 0;
    }

    private boolean isEmpty(int[] buffer) {
        return count == 0;
    }

    private boolean isFull(int[] buffer) {
        return buffer.length == count;
    }

    public static void main(String[] args) throws InterruptedException {
        ProducerConsumer producerConsumer = new ProducerConsumer();
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
