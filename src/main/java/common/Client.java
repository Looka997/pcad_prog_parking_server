package common;

import javax.management.OperationsException;

import static java.util.Objects.requireNonNull;

public abstract class Client {
    private final String plate;
    private final String brand;

    public Client(String plate, String brand){
        requireNonNull(plate);
        this.plate = plate;
        this.brand = brand;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Client))
            return false;
        Client client = (Client) obj;
        return client.getPlate().equals(plate) && client.getBrand().equals(brand);
    }

    public String getPlate() {
        return plate;
    }
    public String getBrand(){ return brand;}

    @Override
    public String toString() {
        return "(" + plate + " " + brand + ")";
    }

    abstract public boolean park() throws OperationsException;
    abstract public boolean unpark() throws OperationsException;

    public void printParkMessage(){
        System.out.println("client " + this + " has parked");
    }

    public void printUnparkMessage(){
        System.out.println("client " + getPlate() + " has left the parking");
    }


}
