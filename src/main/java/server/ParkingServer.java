package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ParkingServer implements Runnable{
    private Parking parking;
    private final int port;
    private ServerSocket serverSocket;
    volatile boolean isStopped = false;


    public ParkingServer(int capacity) throws IOException {
        this(8080, capacity);
    }

    public ParkingServer(int port, int capacity) throws IOException {
        parking = new Parking(capacity);
        this.port = port;
        this.serverSocket = new ServerSocket(port);
    }

    public void stop(){
        parking.close(true);
        SensorWorker.stopEntering();
    }

    private void stop(boolean print){
        parking.close(print);
    }

    public void stopNow() throws IOException {
        stop(false);
        isStopped = true;
        serverSocket.close();
        SensorWorker.stopExiting();
    }

    public void save(String filename){
        parking.save(filename);
    }

    @Override
    public void run() {
        while(!isStopped){
            Socket clientSocket;
            try {
                clientSocket = this.serverSocket.accept();
            }catch (IOException e) {
                if(isStopped){
                    System.out.println("Server Stopped.");
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
             new Thread(new SensorWorker(clientSocket, parking)).start();
        }
    }

    public int getNEntered() { return parking.getNEntered(); }

    public int getNLeft() { return parking.getNLeft(); }

    public int getRejected() {
        return parking.getRejected();
    }

    public int getPort() {
        return port;
    }
}
