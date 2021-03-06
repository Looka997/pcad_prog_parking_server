package server;

import common.ContentMessage;
import common.TipoRichiesta;

import javax.management.OperationsException;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

public class SensorWorker implements Runnable {
    private static Parking parking;
    private final Socket clientSocket;
    private static ExecutorService entering = Executors.newCachedThreadPool();
    private static final ExecutorService exiting = Executors.newCachedThreadPool();
    public SensorWorker(Socket clientSocket, Parking parking) {
        SensorWorker.parking = parking;
        this.clientSocket = clientSocket;
    }

    public static void stopEntering(){
        entering.shutdownNow();
    }

    public static boolean submit(ContentMessage cm, Parking parking) throws ExecutionException, InterruptedException {
        TipoRichiesta request = cm.getTipoRichiesta();
        String brand = cm.getBrand();
        String plate = cm.getPlate();
        if (request == TipoRichiesta.ENTRATA){
            return (entering.submit(new PartialClient(plate, parking, brand) {
                @Override
                public Boolean call() throws OperationsException {
                    return park();
                }
            }).get());
        }
        return exiting.submit(new PartialClient(plate, parking, brand){
            @Override
            public Boolean call() throws OperationsException {
                return unpark();
            }
        }).get();
    }

    public static void restart(){
        synchronized (entering){
            if (entering.isTerminated()) entering = Executors.newCachedThreadPool();
        }
    }
    @Override
    public void run() {
        try {
            InputStream input = clientSocket.getInputStream();
            InputStreamReader inputReader= new InputStreamReader(input);
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(),true);
            BufferedReader reader = new BufferedReader(inputReader);
            ContentMessage cm = ContentMessage.fromString(reader.readLine());
            if (submit(cm, parking))
                writer.println("OK\n");
            else
                writer.println("NOT_OK\n");
            clientSocket.close();
        } catch (RejectedExecutionException e){
            System.out.println("server.Parking was shutdown");
        } catch(ExecutionException | InterruptedException | IOException e){
            e.printStackTrace();
        }
    }
}
