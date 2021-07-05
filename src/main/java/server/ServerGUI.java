package server;


import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

public class ServerGUI extends JFrame {
    final static boolean RIGHT_TO_LEFT = false;
    private ParkingServer parkingServer;
    private Thread parkingThread = null;
    private Timer timer = new Timer();
    private final String filename = "log";
    private final int logs_delay;
    private final int timeOut;
    private final TimerTask printLogs = new TimerTask() {
        int i=1;
        @Override
        public void run() {
            parkingServer.save(filename + i + ".txt");
            i++;
        }
    };

    public ServerGUI(ParkingServer parkingServer){
        this(parkingServer, 1, 5);
    }

    public ServerGUI(ParkingServer parkingServer, int logs_delay, int timeOut) {
        this.parkingServer = parkingServer;
        this.logs_delay = logs_delay;
        this.timeOut = timeOut;
    }

    public void addComponentsToPane(Container pane) {
        if (RIGHT_TO_LEFT) {
            pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        JButton openButton, closeButton;
        pane.setPreferredSize(new Dimension(510, 450));
        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        JTextArea textArea = new JTextArea(10, 40);
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(24, 30, 24, 30);
        textArea.setEditable(false);
        pane.add(textArea, c);

        openButton = new JButton("Open");
        c.gridy = 1;
        c.gridx = 0;
        c.ipady = 10;
        c.ipadx = 7;
        c.insets = new Insets(8, 24, 8, 24);
        pane.add(openButton, c);


        closeButton = new JButton("Close");
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(8, 24, 8, 24);
        pane.add(closeButton, c);
        closeButton.setEnabled(false);
        pane.repaint();
        pane.validate();

        PrintStream printStream = new PrintStream(new JTextAreaOutputStream(textArea));
        System.setOut(printStream);

        openButton.addActionListener(e -> {
            new SwingWorker() {
                @Override
                protected Void doInBackground() throws Exception {
                    timer.schedule(printLogs, 0, logs_delay * 1000);
                    parkingThread = new Thread(parkingServer);
                    parkingThread.start();
                    return null;
                }

                @Override
                protected void done() {
                    openButton.setEnabled(false);
                    closeButton.setEnabled(true);
                }
            }.execute();
        });

        closeButton.addActionListener(e -> {
            new SwingWorker() {
                @Override
                protected Void doInBackground() throws Exception {
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
                    return null;
                }
            }.execute();
            parkingServer.stop();
            closeButton.setEnabled(false);
        });
    }

    private void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Desktop server GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Set up the content pane.
        addComponentsToPane(frame.getContentPane());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
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
            parkingServer= new ParkingServer(port, capacity);
            Runnable init = new ServerGUI(parkingServer, logs_delay, timeOut)::createAndShowGUI;
            SwingUtilities.invokeLater(init);
            System.out.println("capacity: " + capacity + "\nUsing port " + port);
            System.out.println("Using timeout= " + timeOut + "s after accepting entering clients stops");
            System.out.println("Saving logs every " + logs_delay + "s");
            System.out.println("Press \"s\" to stop server.");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));
            while(!reader.readLine().toLowerCase().equals("s"));
            System.out.println("Exiting.");
            parkingServer.stopNow();

        } else{
            System.out.println("Usage: capacity [port] [timeout] [logs-delay]\nExiting.");
        }
    }
}
