package server;
import common.Client;
import common.ContentMessage;
import common.TipoRichiesta;
import server.model.MovimentiDao;

import java.sql.SQLException;

public class Parking {
    private final int capacity;
    private final UniqueBlockingQueue<Client> parked;
    private Boolean closed = false;
    private int rejected = 0;
    private volatile int nentered = 0;
    private final MovimentiDao dao = new MovimentiDao();

    public Parking(int capacity){
        if (capacity < 0)
            throw new IllegalArgumentException("capacity should not be negative");
        this.capacity = capacity;
        parked = new UniqueBlockingQueue<>(capacity);

    }

    private boolean checkValidMove(ContentMessage cm){
        String plate = cm.getPlate();
        try {
            TipoRichiesta lastMov = dao.checkLastMovement(plate);
            if (lastMov.equals(cm.getTipoRichiesta())){
                System.out.println("client " + plate + " " +
                        (lastMov == TipoRichiesta.ENTRATA? "has already " : "is not ") + "parked");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }
    public boolean enter(Client client) {
        ContentMessage cm = new ContentMessage(TipoRichiesta.ENTRATA, client.getPlate(), client.getBrand());
        if (!checkValidMove(cm))
            return false;
        synchronized (closed){
            if (closed) {
                rejected++;
            return false;
            }
        }
        try {
            if (parked.put(client)) {
                if(dao.insert(cm)){
                    nentered++;
                    return true;
                }
                else{
                    parked.remove(client);
                    return false;
                }
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
        parked.remove(client);
        ContentMessage cm = new ContentMessage(TipoRichiesta.USCITA, client.getPlate(), client.getBrand());
        if (!checkValidMove(cm))
            return false;
        return dao.insert(cm);
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
