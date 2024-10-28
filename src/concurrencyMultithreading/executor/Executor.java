package concurrencyMultithreading.executor;

import java.util.concurrent.*;

public class Executor {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        Runnable task = () -> System.out.println(Thread.currentThread().getName());
        for(int i=0; i<10; ++i) {
            new Thread(task).start();
        }

        // Runnable interface                  vs       Callable interface
        //It cannot return the result of computation	It can return the result of the parallel processing of a task.
        //It cannot throw a checked Exception	        It can throw a checked Exception.
        //one needs to override the run() method 	    In order to use Callable, you need to override the call()

        Callable task1 = () ->  {System.out.println(Thread.currentThread().getName()); return null; };
        ExecutorService executor = Executors.newSingleThreadExecutor();
        for(int i=0; i<10; ++i) {
            executor.submit(task1);
        }
        executor.shutdown();

        Callable task2 = () ->  {System.out.println(Thread.currentThread().getName()); return null; };
        executor = Executors.newFixedThreadPool(4);
        for(int i=0; i<10; ++i) {
            executor.submit(task2);
        }
        executor.shutdown();

        Callable<String> task3 = () ->  {return Thread.currentThread().getName();};
        executor = Executors.newFixedThreadPool(4);
        for(int i=0; i<10; ++i) {
            Future<String> future = executor.submit(task3);
            System.out.println(future.get());
        }
        executor.shutdown();

        Callable<String> task4 = () ->  {throw new Exception();};
        executor = Executors.newFixedThreadPool(4);
        try {
            for(int i=0; i<10; ++i) {
                Future<String> future = executor.submit(task4);
                System.out.println(future.get());
            }
        }
        catch (Exception ex) {
            System.out.println("Executor caught exception");
            executor.shutdown();
        }
        finally {
            System.out.println("Executor caught exception");
            executor.shutdown();
        }

        Callable<String> task5 = () ->  {return Thread.currentThread().getName();};
        ExecutorService executor1 = Executors.newFixedThreadPool(4);
        try {
            for(int i=0; i<10; ++i) {
                Future<String> future = executor1.submit(task5);
                System.out.println(future.get(4, TimeUnit.MICROSECONDS));
            }
        }
        finally {
            executor1.shutdown();
        }

        Callable<String> task6 = () ->  {return Thread.currentThread().getName();};
        ExecutorService executor2 = Executors.newFixedThreadPool(4);
        try {
            for(int i=0; i<10; ++i) {
                Future<String> future = executor2.submit(task5);
                System.out.println(future.get(1, TimeUnit.MICROSECONDS));
            }
        }
        finally {
            executor2.shutdown();
        }

        ExecutorService executor3 = Executors.newCachedThreadPool();
        try {
            for(int i=0; i<10; ++i) {
                Callable<String> callable = () -> {return Thread.currentThread().getName();};
                System.out.println(executor3.submit(callable).get());
            }
        }
        finally {
            executor3.shutdown();
        }
    }
}
