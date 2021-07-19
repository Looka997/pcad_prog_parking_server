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

    public UniqueBlockingQueue(){
        this(16);
    }

    public UniqueBlockingQueue(int capacity){
        set = Collections.synchronizedSet(new LinkedHashSet<K>(capacity));
    }

    public boolean put(K key) throws InterruptedException {
        lock.lock();
        try{
            if (set.contains(key))
                return false;
            while (count == set.size())
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

}
