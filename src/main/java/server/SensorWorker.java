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
    public static void stopExiting(){ exiting.shutdownNow(); }
    @Override
    public void run() {
        try {
            InputStream input = clientSocket.getInputStream();
            boolean success;
            InputStreamReader inputReader= new InputStreamReader(input);
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(),true);
            BufferedReader reader = new BufferedReader(inputReader);
            ContentMessage cm = ContentMessage.fromString(reader.readLine());
            if (cm.getTipoRichiesta().compareTo(TipoRichiesta.ENTRATA) == 0){
                success = entering.submit(new PartialClient(cm.getTarga(), parking, cm.getMarca()) {
                    @Override
                    public Boolean call() throws OperationsException {
                        return park();
                    }
                }).get();
            }
            else{
                success = exiting.submit(new PartialClient(cm.getTarga(), parking, cm.getMarca()) {
                    @Override
                    public Boolean call() throws OperationsException {
                        return unpark();
                    }
                }).get();
            }
            if (success)
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
