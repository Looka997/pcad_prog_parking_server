package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.ContentMessage;
import common.StatusResponse;
import server.model.MovimentiDao;
import spark.Spark;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static spark.Spark.*;

public class ParkingServer implements Runnable{
    private final Parking parking;
    private final ServerSocket serverSocket;
    volatile boolean isStopped = false;
    private final MovimentiDao movimentiDao = new MovimentiDao();


    public ParkingServer(int capacity) throws IOException {
        this(8080, capacity);
    }

    public ParkingServer(int port, int capacity) throws IOException {
        parking = new Parking(capacity);
        this.serverSocket = new ServerSocket(port);
    }

    public void stop(){
        parking.close(true);
        SensorWorker.stopEntering();
        isStopped = true;
    }

    public void restart(){
        parking.start(true);
        SensorWorker.restart();
        isStopped = false;
    }

    private void stop(boolean print){
        parking.close(print);
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
            success = SensorWorker.submit(cm, parking, movimentiDao);
            if (success){
                return new Gson().toJson(StatusResponse.SUCCESS);
            }
            return new Gson().toJson(StatusResponse.ERROR);
        });
        get("/users/:plate", (request, response) -> {
            response.type("application/json");
            return "{\"state\":\"" +
                movimentiDao.checkLastMovement(request.params(":plate")) +
                "\"}";
        });
        String usage =
                "<html>" +
                    "<body>" +
                        "Usage: " +
                        "<p>POST 'Content-Type: application/json' " +
                        "'{\"request\" : \"ENTRATA\" OR \"USCITA\", " +
                        "\"plate\": YOUR PLATE HERE, " +
                        "\"brand\" : VALID BRAND HERE}'</p>" +
                        "<p>GET /users/:plate </p>" +
                    "</body>" +
                "</html>";
        internalServerError(usage);
        notFound(usage);
    }

    @Override
    public void run() {
        setRoutes();
        movimentiDao.load();
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
             new Thread(new SensorWorker(clientSocket, parking, movimentiDao)).start();
        }
        Spark.stop();
    }

    public int getNEntered() { return parking.getNEntered(); }

    public int getNLeft() { return parking.getParked(); }

    public int getRejected() {
        return parking.getRejected();
    }
}
