package server;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UniqueBlockingQueue<K> {
    private final Set set;
    final Lock lock = new ReentrantLock();
    final Condition notFull = lock.newCondition();
    final Condition notEmpty = lock.newCondition();
    int count;
    final int capacity;

    public UniqueBlockingQueue(){
        this(16);
    }

    public UniqueBlockingQueue(int capacity){
        this(capacity, 0);
    }
    public UniqueBlockingQueue(int capacity, int count){
        set = Collections.synchronizedSet(new LinkedHashSet<K>());
        this.capacity = capacity;
        this.count = count;
    }

    public boolean put(K key) throws InterruptedException {
        lock.lock();
        try{
            if (set.contains(key))
                return false;
            while (count >= capacity)
                notFull.await();
            set.add(key);
            count++;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
        return true;
    }

    public K take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0)
                notEmpty.await();
            Iterator<K> i = set.iterator();
            K next = i.next();
            i.remove();
            --count;
            notFull.signal();
            return next;
        } finally {
            lock.unlock();
        }
    }

    public int countDown(){
        lock.lock();
        try{
            --count;
            notFull.signal();
            return count;
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(K key){
        lock.lock();
        try {
            boolean removed = set.remove(key);
            if (removed){
                --count;
                notFull.signal();
            }
            return removed;
        } finally {
            lock.unlock();
        }
    }

    public int size(){
        lock.lock();
        try{
            return count;
        }finally {
            lock.unlock();
        }

    }

}
