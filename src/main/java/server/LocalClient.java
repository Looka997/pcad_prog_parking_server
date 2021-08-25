package server;

import common.Client;

import javax.management.OperationsException;

public class LocalClient extends Client {
    private final Parking parking;
    private boolean parked;
    public LocalClient(String targa, Parking parking, String marca) {
        super(targa, marca);
        this.parking = parking;
        this.parked = false;
    }

    @Override
    public boolean park() throws OperationsException {
        if (parked)
            throw new OperationsException("this client is already parked");
        if (parking.enter(this)){
            printParkMessage();
            parked = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean unpark() throws OperationsException {
        if (parking == null)
            throw new OperationsException("this client is not parked");
        parking.exit(this);
        return true;
    }
}
