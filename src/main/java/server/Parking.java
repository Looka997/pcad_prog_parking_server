package server;
import common.Client;
import common.ContentMessage;
import common.TipoRichiesta;
import server.model.MovimentiDao;

import java.sql.SQLException;

public class Parking {
    private final UniqueBlockingQueue<Client> parked;
    private Boolean closed = false;
    private int rejected = 0;
    private volatile int nentered = 0;
    private final MovimentiDao dao = new MovimentiDao();

    public Parking(int capacity){
        if (capacity < 0)
            throw new IllegalArgumentException("capacity should not be negative");
        int entered = 0, exit = 0;
        try {
            entered = dao.getCount(true);
            exit = dao.getCount(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        parked = new UniqueBlockingQueue<>(capacity, entered-exit);
    }
    public boolean enter(Client client) {
        ContentMessage cm = new ContentMessage(TipoRichiesta.ENTRATA, client.getPlate(), client.getBrand());
        synchronized (closed){
            if (closed) {
                rejected++;
            return false;
            }
        }
        try {
            if (parked.put(client)) {
                if(dao.insert(cm,true)){
                    nentered++;
                    return true;
                }
            }
        } catch (InterruptedException e) {
            synchronized (closed){
                if (closed) {
                    rejected++;
                    return false;
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public boolean exit(Client client) {
        boolean removed = parked.remove(client);
        ContentMessage cm = new ContentMessage(TipoRichiesta.USCITA, client.getPlate(), client.getBrand());
        try {
            if (dao.insert(cm,true)){
                if (!removed) parked.countDown();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
