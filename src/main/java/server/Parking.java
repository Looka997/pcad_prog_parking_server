package server;
import common.Client;

public class Parking {
    private final int capacity;
    private final UniqueBlockingQueue<Client> parked;
    private Boolean closed = false;
    private int rejected = 0;
    private volatile int nentered = 0;

    public Parking(int capacity){
        if (capacity < 0)
            throw new IllegalArgumentException("capacity should not be negative");
        this.capacity = capacity;
        parked = new UniqueBlockingQueue<>(capacity);

    }

    public boolean enter(Client client) {
        synchronized (closed){
            if (closed) {
                rejected++;
            return false;
            }
        }
        try {
            if (parked.put(client)) {
                nentered++;
                return true;
            }
        } catch (InterruptedException e) {
            synchronized (closed){
                if (closed) {
                    rejected++;
                    return false;
                }
            }
        }
        return false;
    }

    public boolean exit(Client client) {
        return parked.remove(client);
    }

    public synchronized int free(){
        return capacity - parked.size();
    }

    public void close(boolean print){
        if (print)
            System.out.println("Parking has closed!");
        closed = true;
    }

    public void start(boolean print){
        if (print)
            System.out.println("Parking has started");
        closed = false;
    }

    int getParked(){
        return parked.count;
    }
    int getNEntered(){
        return nentered;
    }
    int getRejected(){
        return rejected;
    }

    synchronized boolean isClosed(){
        return closed;
    }
}
