package main;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DelayBlockingCollectionMultiThreadedTest {
    private static final int SOME_DELAY_TIME = 1000;
    DelayBlockingCollection<Object> collection;

    @Test
    void shouldReturnFirstExpiredElement() throws InterruptedException {
        Object item1 = new Object();
        Object item2 = new Object();

        for (int i = 0; i < 10; i++) {
            collection = new DelayBlockingCollection<>();

            Runnable r1 = () -> collection.add(item1, 10000);
            Runnable r2 = () -> {
                try {
                    assertSame(item2, collection.get(100000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };
            Runnable r3 = () -> collection.add(item2, 100);

            Thread t1 = new Thread(r1);
            Thread t2 = new Thread(r2);
            Thread t3 = new Thread(r3);


            t1.start();
            t1.join();
            t3.start();
            t3.join();

            t2.start();
            t2.join();
        }
    }

    @Test
    void shouldReturnFirstExpiredElementWhenNonEmptyAtGetCall() throws InterruptedException {

        Object item1 = new Object();
        Object item2 = new Object();

        for (int i = 0; i < 10; i++) {
            collection = new DelayBlockingCollection<>();

            Runnable r1 = () -> collection.add(item1, 10000);
            Runnable r2 = () -> {
                try {
                    assertSame(item2, collection.get(100000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };
            Runnable r3 = () -> collection.add(item2, 100);

            Thread t1 = new Thread(r1);
            Thread t2 = new Thread(r2);
            Thread t3 = new Thread(r3);


            t1.start();
            t1.join();

            t2.start();
            t3.start();

            t2.join();
            t3.join();
        }
    }

    @Test
    void shouldReturnFirstExpiredElementWhenCanBeEmptyAtGetCall() throws InterruptedException {

        Object item1 = new Object();
        Object item2 = new Object();

        for (int i = 0; i < 10; i++) {
            collection = new DelayBlockingCollection<>();

            Runnable r1 = () -> collection.add(item1, 10000);
            Runnable r2 = () -> {
                try {
                    assertSame(item2, collection.get(100000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };
            Runnable r3 = () -> collection.add(item2, 100);

            Thread t1 = new Thread(r1);
            Thread t2 = new Thread(r2);
            Thread t3 = new Thread(r3);

            t1.start();
            t2.start();
            t3.start();

            t1.join();
            t2.join();
            t3.join();
        }
    }

    @Test
    void shouldRespectTimeout() throws InterruptedException {

        Object item1 = new Object();

        for (int i = 0; i < 10; i++) {
            collection = new DelayBlockingCollection<>();

            Runnable r1 = () -> collection.add(item1, 100);
            Runnable r2 = () -> {
                try {
                    Object returnedItem = collection.get(100, 300);//You cannot say if collection will be empty or not there
                    assertTrue(item1 == returnedItem || returnedItem == null);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };
            Runnable r3 = () -> collection.remove(item1);

            Thread t1 = new Thread(r1);
            Thread t2 = new Thread(r2);
            Thread t3 = new Thread(r3);

            t1.start();
            t2.start();
            t3.start();

            t1.join();
            t2.join();
            t3.join();
        }
    }
}