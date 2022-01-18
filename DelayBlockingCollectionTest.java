package main;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DelayBlockingCollectionTest {
    private static final int SOME_DELAY_TIME = 1000;
    private static final int DEFAULT_TIMEOUT = 100000;
    DelayBlockingCollection<Object> collection;

    @BeforeEach
    void init() {
        collection = new DelayBlockingCollection<>(DEFAULT_TIMEOUT);
    }

    @Test
    void shouldAddElement() throws InterruptedException {
        Object itemExpected = new Object();
        collection.add(itemExpected);

        assertSame(itemExpected, collection.get());
    }

    @Test
    void shouldAddElements() throws InterruptedException {
        List<Object> itemsExpected = Arrays.asList(new Object(), new Object(), new Object());

        collection.addAll(itemsExpected);

        List<Object> itemsActual = Arrays.asList(collection.get(SOME_DELAY_TIME), collection.get(SOME_DELAY_TIME), collection.get(SOME_DELAY_TIME));

        assertTrue(itemsExpected.size() == itemsActual.size() && itemsExpected.containsAll(itemsActual) && itemsActual.containsAll(itemsExpected));
    }

    @Test
    void shouldAddElementsInConstructor() throws InterruptedException {
        List<Object> itemsExpected = Arrays.asList(new Object(), new Object(), new Object());

        collection = new DelayBlockingCollection<>(itemsExpected, DEFAULT_TIMEOUT);

        List<Object> itemsActual = Arrays.asList(collection.get(SOME_DELAY_TIME), collection.get(SOME_DELAY_TIME), collection.get(SOME_DELAY_TIME));

        assertTrue(itemsExpected.size() == itemsActual.size() && itemsExpected.containsAll(itemsActual) && itemsActual.containsAll(itemsExpected));
    }

    @Test
    void shouldReturnElementsInProperOrder() throws InterruptedException {
        Object item1 = new Object();
        Object item2 = new Object();
        Object item3 = new Object();

        collection.add(item1, 100);
        collection.add(item2, 110);
        collection.add(item3, 120);

        assertSame(item1, collection.get(2000));
        assertSame(item2, collection.get(2100));
        assertSame(item3, collection.get(2200));

        assertSame(item1, collection.get(2200));
        assertSame(item2, collection.get(2000));
        assertSame(item3, collection.get(2300));

        assertSame(item2, collection.get(1000));
        assertSame(item1, collection.get(1000));
        assertSame(item3, collection.get(1000));
    }

    @Test
    void shouldReturnElementsInProperOrderWhenItemsExpired() throws InterruptedException {
        Object item1 = new Object();
        Object item2 = new Object();
        Object item3 = new Object();

        collection.add(item1, 100);
        collection.add(item2, 110);
        collection.add(item3, 120);

        assertSame(item1, collection.get(1000));
        assertSame(item2, collection.get(500));
        assertSame(item3, collection.get(700));

        synchronized (this) {
            Thread.sleep(2000);
        }

        assertSame(item2, collection.get(1000));
        assertSame(item3, collection.get(1000));
        assertSame(item1, collection.get(1000));
    }

    @Test
    void shouldWaitForElementToExpire() throws InterruptedException {
        Object item = new Object();

        collection.add(item, 1000);

        long start = System.currentTimeMillis();
        collection.get(1000);
        long end = System.currentTimeMillis();

        assertTrue(end - start >= 999 && end - start < 1050);
    }

    @Test
    void shouldRemoveElement() throws InterruptedException {
        Object item1 = new Object();
        Object item2 = new Object();
        Object item3 = new Object();
        List<Object> items = Arrays.asList(item1, item2, item3);

        collection.addAll(items);
        boolean isOperationSuccessful = collection.remove(item2);

        List<Object> itemsExpected = Arrays.asList(item1, item3);
        List<Object> itemsActual = Arrays.asList(collection.get(SOME_DELAY_TIME), collection.get(SOME_DELAY_TIME));

        assertEquals(itemsExpected, itemsActual);
        assertTrue(isOperationSuccessful);
    }

    @Test
    void shouldNotRemoveAnythingIfElementAbsent() throws InterruptedException {
        Object item = new Object();
        Object absentItem = new Object();

        collection.add(item);
        boolean isRemoveSuccessful = collection.remove(absentItem);

        assertEquals(item, collection.get(SOME_DELAY_TIME));
        assertFalse(isRemoveSuccessful);
    }

    @Test
    void shouldReturnFirstExpiredElement() throws InterruptedException {
        Object item1 = new Object();
        Object item2 = new Object();

        for (int i = 0; i < 10; i++) {
            collection = new DelayBlockingCollection<>(DEFAULT_TIMEOUT);

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
            collection = new DelayBlockingCollection<>(DEFAULT_TIMEOUT);

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
            collection = new DelayBlockingCollection<>(DEFAULT_TIMEOUT);

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

    /*
    TODO fix it will wait here forever because take() method is called on empty DelayQueue sometimes, should change get method to
    TODO get timeout parameter and use pool() instead of take
    */
    @Test
    void shouldNotBlock() throws InterruptedException {

        fail("it will wait here forever because take() method is called on empty DelayQueue sometimes, see TODO comment above this test method");

        Object item1 = new Object();

        for (int i = 0; i < 10; i++) {
            collection = new DelayBlockingCollection<>(DEFAULT_TIMEOUT);

            Runnable r1 = () -> collection.add(item1, 1000);
            Runnable r2 = () -> {
                try {
                    assertSame(item1, collection.get(1000));
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