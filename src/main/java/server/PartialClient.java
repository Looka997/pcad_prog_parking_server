package server;

import common.Targa;

import java.util.concurrent.Callable;

public abstract class PartialClient extends LocalClient implements Callable<Boolean> {

    public PartialClient(Targa targa, Parking parking, String marca) {
        super(targa, parking, marca);
    }

}
