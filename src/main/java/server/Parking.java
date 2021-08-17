package server;
import common.Client;
import javax.management.OperationsException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class Parking {
    private final int capacity;
    private final UniqueBlockingQueue<Client> parked;
    private Boolean closed = false;
    private int rejected = 0;
    private volatile int nleft = 0;
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

    public boolean exit(Client client) throws OperationsException {
        if (parked.remove(client)) {
            nleft++;
            return true;
        }
        throw new OperationsException("tried to remove a client that wasn't in the Q");
    }

    public synchronized int free(){
        return capacity - parked.size();
    }

    public void close(boolean print){
        if (print)
            System.out.println("Parking has closed!");
        closed = true;
    }

    int getNLeft(){
        return nleft;
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

    public void save(String filename){
        CompletableFuture.runAsync(() -> {
            try {
                FileWriter fw = new FileWriter(filename);
                fw.write(parked.toString());
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).thenRun(() -> System.out.println("Log saved"));
    }
}
