package common;

import javax.management.OperationsException;

import static java.util.Objects.requireNonNull;

public abstract class Client {
    private String targa;
    private String marca;

    public Client(String targa, String marca){
        requireNonNull(targa);
        this.targa = targa;
        this.marca = marca;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Client))
            return false;
        Client client = (Client) obj;
        return client.getTarga().equals(targa) && client.getMarca().equals(marca);
    }

    public String getTarga() {
        return targa;
    }
    public String getMarca(){ return marca;}

    @Override
    public String toString() {
        return "(" + targa + " " + marca + ")";
    }

    abstract public boolean park() throws OperationsException;
    abstract public boolean unpark() throws OperationsException;

    public void printParkMessage(){
        System.out.println("client " + toString() + " has parked");
    }

    public void printUnparkMessage(){
        System.out.println("client " + getTarga() + " has left the parking");
    }


}
