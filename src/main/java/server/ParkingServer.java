package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import common.ContentMessage;
import common.StatusResponse;
import spark.Spark;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static spark.Spark.*;

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

    private void setRoutes(){
        post("/users", (request, response) -> {
            boolean success;
            response.type("application/json");
            String body = request.body();
            Gson gson = new GsonBuilder()
                    .serializeNulls()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
                    .create();
            MessageInstanceCreator mic = gson.fromJson(body, MessageInstanceCreator.class);
            ContentMessage cm = mic.createInstance(ContentMessage.class);
            success = SensorWorker.submit(cm.getTipoRichiesta(), cm.getPlate(), parking, cm.getBrand());
            if (success)
                return new Gson().toJson(StatusResponse.SUCCESS);
            return new Gson().toJson(StatusResponse.ERROR);
        });
        String usage =
                "<html>" +
                    "<body>" +
                        "Usage: " +
                        "POST 'Content-Type: application/json' " +
                        "'{\"request\" : \"ENTRATA\" OR \"USCITA\", " +
                        "\"plate\": YOUR PLATE HERE, " +
                        "\"brand\" : VALID BRAND HERE}'" +
                    "</body>" +
                "</html>";
        internalServerError(usage);
        notFound(usage);
    }

    @Override
    public void run() {
        setRoutes();
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
        Spark.stop();
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
