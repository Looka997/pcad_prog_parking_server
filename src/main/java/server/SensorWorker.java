package server;

import common.ContentMessage;
import common.TipoRichiesta;
import server.model.MovimentiDao;

import javax.management.OperationsException;
import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.*;

public class SensorWorker implements Runnable {
    private static Parking parking;
    private final Socket clientSocket;
    private static ExecutorService entering = Executors.newCachedThreadPool();
    private static final ExecutorService exiting = Executors.newCachedThreadPool();
    private final MovimentiDao dao;
    public SensorWorker(Socket clientSocket, Parking parking, MovimentiDao dao ) {
        SensorWorker.parking = parking;
        this.clientSocket = clientSocket;
        this.dao = dao;
    }

    public static void stopEntering(){
        entering.shutdownNow();
    }

    public static boolean submit(ContentMessage cm, Parking parking, MovimentiDao dao) throws ExecutionException, InterruptedException {
        TipoRichiesta request = cm.getTipoRichiesta();
        String brand = cm.getBrand();
        String plate = cm.getPlate();
        try {
            TipoRichiesta lastMov = dao.checkLastMovement(plate);
            if (lastMov.equals(cm.getTipoRichiesta())){
                System.out.println("client " + plate + " " +
                        (lastMov == TipoRichiesta.ENTRATA? "has already " : "is not ") + "parked");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (request == TipoRichiesta.ENTRATA){
            return (entering.submit(new PartialClient(plate, parking, brand) {
                @Override
                public Boolean call() throws OperationsException {
                    return park();
                }
            }).get() && dao.insert(cm));
        }
        exiting.submit(new PartialClient(plate, parking, brand){
            @Override
            public Boolean call() throws OperationsException {
                return unpark();
            }
        }).get();
        return dao.insert(cm);
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
            if (submit(cm, parking, dao))
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
