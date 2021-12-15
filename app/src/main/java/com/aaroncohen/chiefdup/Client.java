package com.aaroncohen.chiefdup;

import android.os.AsyncTask;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class Client extends Thread {

    private String name;
    private int pin, rounds, time;
    private boolean isHost;

    MainActivity activity;

    public Client(MainActivity mainActivity, String name, int pin, boolean isHost) {
        //client properties
        this.name = name;
        this.isHost = isHost;
        activity = mainActivity;

        //game properties
        rounds = 3;
        time = 180;     //time is measured in seconds, 180 seconds is 3 minutes

        if (isHost) {
            //create a random port number and refresh value until port is available
            class PortMaker implements Runnable {
                private int pin;

                public PortMaker() {
                    super();
                    this.pin = 1;
                }

                private boolean portAvailable(int port) {                    //tests if the supplied port is available to create a new game
                    try (Socket ignored = new Socket("localhost", port)) {
                        return false;
                    } catch (IOException ignored) {
                        return true;
                    }
                }

                @Override
                public void run() {
                    int randomPin = (int) (Math.random() * 55535) + 10000;
                    while (!portAvailable(randomPin)) {
                        randomPin = (int) (Math.random() * 55535) + 10000;
                    }
                    pin = randomPin;
                }

                public int getPin() {
                    return pin;
                }
            }

            PortMaker portMaker = new PortMaker();
            Thread thread = new Thread(portMaker);
            thread.start();
            try {
                thread.join();
            } catch (Exception e) {
                //do nothing
            }
            this.pin = portMaker.getPin();
        } else {
            this.pin = pin;
        }
    }

    /*
    ===================
    GETTERS AND SETTERS
    ===================
     */

    //game property getters
    public int getRounds() {
        return rounds;
    }
    public String getTime() {
        //returns round time formatted in a string
        int mins = 0;
        int copyOfTime = time;
        //calculate minutes
        while (copyOfTime - 60 >= 0) {
            mins++;
            copyOfTime -= 60;
        }
        //copyOfTime should now be equal to seconds
        return mins + ":" + String.format("%02d", copyOfTime);
    }

    //game property setters
    public void addRound() {
        rounds += (rounds < 8) ? 1 : 0;
    }
    public void removeRound() {
        rounds -= (rounds > 1) ? 1 : 0;
    }
    public void addTime() {
        time += (time < 360) ? 30 : 0;
    }
    public void removeTime() {
        time -= (time > 60) ? 30 : 0;
    }

    //client property getters
    public boolean isHost() {
        return isHost;
    }
    public String name() {
        return name;
    }
    public int pin() {
        return pin;
    }

    /*
    ==============
    CLIENT METHODS
    ==============
     */

    public void disconnect() {  //this is the code that runs when the client disconnects from the game

        //leaving the game is handled by MainActivity in the popup

        if (this.isHost) {
            //kick all players from game and then leave

        } else {
            //disconnect from server and then leave

        }
    }

    /*
    ============
    HOST METHODS
    ============
     */

    public void kickPlayer(int playerNumber) {

    }

    /*
    ========
    NET CODE
    ========
     */

    class PlayerSocket implements Runnable {

        public ServerSocket socket;
        public Socket client;

        public boolean serverRunning;

        DataInputStream dataIn;
        DataOutputStream dataOut;

        @Override
        public void run() {
            try {
                //create the socket
                socket = new ServerSocket(pin);
                client = socket.accept();

                DataInputStream dataIn = new DataInputStream(client.getInputStream());
                DataOutputStream dataOut = new DataOutputStream(client.getOutputStream());

                serverRunning = true;

                while (serverRunning) {
                    byte protocol = dataIn.readByte();
                    switch (protocol) {
                        case 0:
                            //establish or disconnect the client
                            serverRunning = dataIn.readUTF().equals("Chief'd Up!"); //if the sent String is from a chief'd up client then set up the server, else disconnect

                            if (serverRunning) {
                                dataOut.writeChars("Chief'd Up!");
                                dataOut.flush();
                            }

                            break;
                        case 1:
                            //send the client name array

                    }
                }

                //close the server thread
                dataIn.close();
                socket.close();
                client.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    ArrayList<PlayerSocket> playerSocketArrayList;
    private int connectedPlayers;
    private boolean clientRunning;

    @Override
    public void run() {
        this.setUncaughtExceptionHandler(activity.uncaughtExceptionHandler);
        if (this.isHost) {
            connectedPlayers = 0;

            //start the server and create all 7 player sockets
            playerSocketArrayList = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                PlayerSocket playerSocket = new PlayerSocket();
                playerSocketArrayList.add(playerSocket);
                Thread playerThread = new Thread(playerSocket);
                //start the thread armada
                playerThread.start();
            }

        } else {
            class ClientSocket implements Runnable {
                @Override
                public void run() {

                    //connect to the server as the client
                    Socket server = null;
                    try {
                        server = new Socket("localhost", pin);

                        DataInputStream dataIn = new DataInputStream(server.getInputStream());
                        DataOutputStream dataOut = new DataOutputStream(server.getOutputStream());

                        String in = "", out = "";

                        clientRunning = true;

                        //establish a connection with the server
                        dataOut.writeByte(0);
                        dataOut.writeChars("Chief'd Up!");
                        dataOut.flush();

                        //make sure the server is a chief'd up server
                        clientRunning = dataIn.readUTF().

                                equals("Chief'd Up!");

                        while (clientRunning) {
                            byte protocol = dataIn.readByte();

                            switch (protocol) {
                                case 0:
                                    //kick the client
                                    clientRunning = false;
                                    break;
                                case 1:
                                    //move to game_start_screen with array of playerNames
                                    activity.gameStartScreen();
                                    break;
                            }
                        }

                        //close the client
                        dataOut.close();
                        server.close();
                    } catch (Exception e) {
                        throw new RuntimeException();
                    }
                }
            }
            ClientSocket clientSocket = new ClientSocket();
            Thread thread = new Thread(clientSocket);
            thread.setUncaughtExceptionHandler(activity.uncaughtExceptionHandler);
            thread.start();
        }
    }
}
