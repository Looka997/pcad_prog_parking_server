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

    public ServerGUI(ParkingServer parkingServer){
        this.parkingServer = parkingServer;
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
            parkingThread = new Thread(parkingServer);
            parkingThread.start();
            openButton.setEnabled(false);
            closeButton.setEnabled(true);
        });

        closeButton.addActionListener(e -> {
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
}
