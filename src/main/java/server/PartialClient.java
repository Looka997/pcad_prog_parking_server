package server;

import java.util.concurrent.Callable;

public abstract class PartialClient extends LocalClient implements Callable<Boolean> {

    public PartialClient(String targa, Parking parking, String marca) {
        super(targa, parking, marca);
    }

}
