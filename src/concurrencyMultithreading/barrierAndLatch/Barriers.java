package concurrencyMultithreading.barrierAndLatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

class Person implements Callable {

    @Override
    public String call() throws Exception {
        System.out.println("getParties: " + Barriers.barrier.getParties());
        System.out.println("getNumberWaiting: " + Barriers.barrier.getNumberWaiting());
        System.out.println("isBroken: " + Barriers.barrier.isBroken());
        Barriers.barrier.await();
        return "I am ready for movie";
    }
}

public class Barriers {
    public static CyclicBarrier barrier = new CyclicBarrier(5, () -> System.out.println("barrier started"));
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<String>> futures = new ArrayList<>();
        // if any future remain not concelled then it wont closed the jvm and terminal for 6 it will be open
        // for less than or equal to 5 it will be closed as we are cancelling 5 futures
        for(int i=0; i<5; ++i) {
            Person person = new Person();
            Thread.sleep(200);
            Future<String> future = executor.submit(person);
            futures.add(future);
        }
        try {
            for(int i=0; i<5; ++i) {
                Future<String> future = futures.get(i);
                System.out.println(future.get());
                future.cancel(true);
            }
        }
        catch (ExecutionException ex) {
            System.out.println("ExecutionException occured");
            executor.shutdown();
        }
        catch (Exception ex) {
            System.out.println("Exception occured " + ex.getMessage());
            executor.shutdown();
        }
        executor.shutdown();
        System.out.println("task completed");
        // Barriers can be reset again for further operations
        Barriers.barrier.reset();
    }
}
