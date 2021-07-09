package server;

import common.ContentMessage;
import common.TipoRichiesta;

import javax.management.OperationsException;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

public class SensorWorker implements Runnable {
    private static Parking parking;
    private Socket clientSocket;
    private static ExecutorService entering = Executors.newCachedThreadPool();
    private static ExecutorService exiting = Executors.newCachedThreadPool();
    public SensorWorker(Socket clientSocket, Parking parking) {
        this.parking = parking;
        this.clientSocket = clientSocket;
    }

    public static void stopEntering(){
        entering.shutdownNow();
    }

    public static boolean submit(TipoRichiesta request, String targa, Parking parking, String brand) throws ExecutionException, InterruptedException {
        if (request.compareTo(TipoRichiesta.ENTRATA) == 0){
            return entering.submit(new PartialClient(targa, parking, brand) {
                @Override
                public Boolean call() throws OperationsException {
                    return park();
                }
            }).get();
        };
        return exiting.submit(new PartialClient(targa, parking, brand){
                @Override
                public Boolean call() throws OperationsException {
                    return unpark();
                }
            }).get();

    }

    public static void stopExiting(){ exiting.shutdownNow(); }
    @Override
    public void run() {
        try {
            InputStream input = clientSocket.getInputStream();
            InputStreamReader inputReader= new InputStreamReader(input);
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(),true);
            BufferedReader reader = new BufferedReader(inputReader);
            ContentMessage cm = ContentMessage.fromString(reader.readLine());
            if (submit(cm.getTipoRichiesta(),cm.getPlate(), parking, cm.getBrand()))
                writer.println("OK\n");
            else
                writer.println("NOT_OK\n");
            clientSocket.close();
        } catch (RejectedExecutionException e){
            System.out.println("server.Parking was shutdown");
        } catch(ExecutionException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
