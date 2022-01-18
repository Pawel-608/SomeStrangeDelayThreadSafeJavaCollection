package main;

import java.util.*;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Collection that based on {@linkplain DelayQueue} and allows to specify
 * minimum delay between getting same object two times
 *
 * @param <E> - type of elements held in this collection
 */
public class DelayBlockingCollection<E> {
    private final DelayQueue<Wrapper<E>> itemsWrapped = new DelayQueue<>();
    private final long defaultTimeout;

    /**
     * @param c - collection with elements that you want to add
     */
    public DelayBlockingCollection(Collection<? extends E> c, long defaultTimeoutInMs) {
        this(defaultTimeoutInMs);
        this.addAll(c);
    }

    public DelayBlockingCollection(long defaultTimeoutInMs) {
        this.defaultTimeout = defaultTimeoutInMs;
    }

    /**
     * Equivalent to get(0)
     *
     * @return element that expired the longest time ago or element that will expire first
     */
    public synchronized E get() throws InterruptedException {
        return get(0);
    }

    /**
     * Works like peek - returns element from the head of queue - but also moves this element backward, by changing
     * its expiration date
     *
     * @param delayInMs minimum time to return same element next time
     * @return element that expired the longest time ago or element that will expire first
     */
    public synchronized E get(long delayInMs) throws InterruptedException {
        Wrapper<E> wrappedItem = itemsWrapped.take();
        wrappedItem.setDelay(delayInMs);
        itemsWrapped.add(wrappedItem);

        return wrappedItem.getItem();
    }

    /**
     * Equivalent to add(e, 0)
     *
     * @param e element to add
     */
    public void add(E e) {
        add(e, 0);
    }

    /**
     * Adding element
     *
     * @param e         element to add
     * @param delayInMs time to element become available
     */
    public void add(E e, int delayInMs) {
        itemsWrapped.add(new Wrapper<>(e, delayInMs));
    }

    /**
     * Adds all elements (equivalent to call add(e) for each element)
     *
     * @param c - collection with elements that you want to add
     */
    public void addAll(Collection<? extends E> c) {
        c.forEach(this::add);
    }

    /**
     * Removes element
     *
     * @param e element to remove
     * @return {@code true} if this collection changed as a result of the call
     */
    public synchronized boolean remove(E e) {
        Wrapper<E> clientToRemove = itemsWrapped.stream()
                .filter(wrapper -> wrapper.item == e)
                .findFirst()
                .orElse(null);

        return itemsWrapped.remove(clientToRemove);
    }


    static private class Wrapper<E> implements Delayed {
        private final E item;
        private long delayTime;

        Wrapper(E e, long delayTimeInMs) {
            this.item = e;

            this.delayTime = System.currentTimeMillis() + delayTimeInMs;
        }

        public E getItem() {
            return item;
        }

        public void setDelay(long delayInMs) {
            this.delayTime = System.currentTimeMillis() + delayInMs;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long diff = this.delayTime - System.currentTimeMillis();
            return unit.convert(diff, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {//TODO make sure this implementation is correct!
            return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
        }
    }
}
