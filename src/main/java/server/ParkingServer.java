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

    public static void main(String[] args) throws IOException {
        int capacity, port = 8080, timeOut = 5, logs_delay=1;
        ParkingServer parkingServer;
        if (args.length > 0 && args.length < 5){
            capacity = Integer.valueOf(args[0]);
            switch (args.length){
                case 4: logs_delay = Integer.valueOf(args[3]);
                case 3: timeOut = Integer.valueOf(args[2]);
                case 2: port = Integer.valueOf(args[1]);
            }
            System.out.println("capacity: " + capacity + "\nUsing port " + port);
            System.out.println("Using timeout= " + timeOut + "s after accepting entering clients stops");
            System.out.println("Saving logs every " + logs_delay + "s");
            parkingServer= new ParkingServer(port, capacity);
            Timer timer = new Timer();
            Thread parkingThread = new Thread(parkingServer);
            parkingThread.start();
            String filename = "log";
            TimerTask printLogs = new TimerTask() {
                int i=1;
                @Override
                public void run() {
                    parkingServer.save(filename + i + ".txt");
                    i++;
                }
            };
            timer.schedule(printLogs, 0, logs_delay * 1000);
            System.out.println("Press \"s\" to stop server.");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));
            while(!reader.readLine().toLowerCase().equals("s"));
            System.out.println("Exiting.");
            parkingServer.stop();
            printLogs.cancel();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    timer.cancel();
                    try {
                        parkingServer.stopNow();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("entered:" + parkingServer.getNEntered() + " left:" + parkingServer.getNLeft());
                    if (parkingServer.getRejected() > 0)
                        System.out.println(parkingServer.getRejected() + " were rejected because the parking closed before they could enter or server was closed too soon");
                }
            },timeOut * 1000);

        } else{
            System.out.println("Usage: capacity [port]\nExiting.");
        }
        return;

    }

    public int getPort() {
        return port;
    }
}
