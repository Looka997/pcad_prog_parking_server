package server;


import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintStream;

public class ServerGUI extends JFrame {
    private final ParkingServer parkingServer;
    private Thread parkingThread = null;

    public ServerGUI(ParkingServer parkingServer) {
        this.parkingServer = parkingServer;
    }

    public void addComponentsToPane(Container pane) {
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

        openButton.addActionListener(e -> new SwingWorker() {
            @Override
            protected Void doInBackground() {
                if (parkingServer.isStopped){
                    parkingServer.restart();
                    return null;
                }
                parkingThread = new Thread(parkingServer);
                parkingThread.start();
                return null;
            }

            @Override
            protected void done() {
                openButton.setEnabled(false);
                closeButton.setEnabled(true);
            }
        }.execute());

        closeButton.addActionListener(e -> {
            parkingServer.stop();
            closeButton.setEnabled(false);
            openButton.setEnabled(true);
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
        int capacity, port = 8080;
        ParkingServer parkingServer;
        if (args.length > 0 && args.length < 3){
            capacity = Integer.parseInt(args[0]);
            if (args.length > 1)
                port = Integer.parseInt(args[1]);
            parkingServer= new ParkingServer(port, capacity);
            Runnable init = new ServerGUI(parkingServer)::createAndShowGUI;
            SwingUtilities.invokeLater(init);
            System.out.println("capacity: " + capacity + "\nUsing port " + port);
        } else{
            System.out.println("Usage: capacity [port]\nExiting.");
        }
    }
}
