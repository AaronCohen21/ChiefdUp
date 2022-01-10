package com.aaroncohen.chiefdup;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

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
                        ignored.close();
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

        //net code things
        uiHandler = new Handler();
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
        //get the playerSocket of the player to be kicked
        PlayerSocket playerToKick = playerSocketArrayList.get(playerNumber);
        //post new commands to kick the player
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DataOutputStream dataOut = new DataOutputStream(playerToKick.client.getOutputStream());
                    dataOut.writeByte(0);
                    dataOut.flush();
                } catch (IOException e) {
                    throw new RuntimeException();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            //do nothing
        }

        //remove the player from the playerSocketArrayList and start up a new player in its place
        playerSocketArrayList.remove(playerToKick);
        playerNames.remove(playerNumber);
        //TODO figure out how to start up the new player

        //update player names on all the other clients
        String newNames = "";
        for (String name : playerNames) {
            newNames += name + "\n";
        }

        for (PlayerSocket player : playerSocketArrayList) {
            String finalNewNames = newNames;
            Thread thread2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DataOutputStream dataOut = new DataOutputStream(player.client.getOutputStream());
                        dataOut.writeByte(2);
                        dataOut.writeUTF(finalNewNames);
                        dataOut.flush();
                    } catch (IOException e) {
                        //do nothing
                    }
                }
            });
            thread2.start();
            try {
                thread2.join();
            } catch (InterruptedException e) {
                //do nothing
            }
        }

        //reset the game start screen and then fill in all remaining player names
        activity.gameStartScreen();
        for (int i = 1; i < playerNames.size(); i++) {
            setPlayerNameDisplay(i, playerNames.get(i), true, activity.getResources().getColor(R.color.teal_200));
        }
    }

    /*
    ========
    NET CODE
    ========
     */

    Handler uiHandler;

    class PlayerSocket implements Runnable {

        public PlayerSocket(ServerSocket socket, Socket client) {
            this.socket = socket;
            this.client = client;
        }

        public ServerSocket socket;
        public Socket client;

        public boolean serverRunning;

        DataInputStream dataIn;
        DataOutputStream dataOut;

        @Override
        public void run() {
            try {
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
                                dataOut.writeUTF("Chief'd Up!");
                                dataOut.flush();
                                connectedPlayers++;
                            } else {
                                dataOut.writeByte(0);
                                dataOut.flush();
                                connectedPlayers--;
                            }

                            break;
                        case 1:
                            //the client's name should now be sent, add to the name list
                            String playerName = dataIn.readUTF();
                            playerNames.add(playerName);
                            //display the new playername in the host's game_start_screen
                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    setPlayerNameDisplay(connectedPlayers, playerName, true, activity.getResources().getColor(R.color.teal_200));
                                }
                            });
                            //send the client the list of player names to update their own screen
                            String playerNamesToSend = "";
                            for (String name : playerNames) {
                                //each player name is separated by a new line
                                playerNamesToSend += name + "\n";
                            }

                            //move client to game_start_screen
                            dataOut.writeByte(1);
                            dataOut.flush();

                            //update the client's name list
                            dataOut.writeByte(2);
                            dataOut.writeUTF(playerNamesToSend);
                            dataOut.flush();
                            break;
                    }
                }

                //close the server thread
                dataIn.close();
                dataOut.close();
                client.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    ArrayList<PlayerSocket> playerSocketArrayList;
    private int connectedPlayers;
    private boolean clientRunning;
    ArrayList<String> playerNames;

    @Override
    public void run() {
        this.setUncaughtExceptionHandler(activity.uncaughtExceptionHandler);
        if (this.isHost) {
            connectedPlayers = 0;
            playerNames = new ArrayList<>();
            playerNames.add(this.name());

            //define the server socket
            ServerSocket socket = null;
            try {
                socket = new ServerSocket(pin);
            } catch (Exception e) {
                //do nothing
            }

            //start the server and create all 7 player sockets
            //TODO create a way to close the ServerSocket
            playerSocketArrayList = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                try {
                    //create the socket
                    Socket client = socket.accept();
                    //create the player socket and add it to the list
                    PlayerSocket playerSocket = new PlayerSocket(socket, client);
                    playerSocketArrayList.add(playerSocket);
                    Thread playerThread = new Thread(playerSocket);
                    //start the thread
                    playerThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException();
                }
            }

        } else {
            class ClientSocket implements Runnable {

                Socket server;

                @Override
                public void run() {
                    server = null;
                    //connect to the server as the client
                    try {
                        //connect to the server
                        try {
                            server = new Socket("localhost", pin);  //this will always work if the server exists
                        } catch (Exception e) {
                            server = new Socket("10.0.2.2", pin);   //this will only work if an emulator is running
                        }

                        DataInputStream dataIn = new DataInputStream(server.getInputStream());
                        DataOutputStream dataOut = new DataOutputStream(server.getOutputStream());

                        clientRunning = true;

                        //establish a connection with the server
                        dataOut.writeByte(0);
                        dataOut.writeUTF("Chief'd Up!");
                        dataOut.flush();

                        //make sure the server is a chief'd up server
                        clientRunning = dataIn.readUTF().equals("Chief'd Up!");

                        //now the client and server have been fully established, send the server the client's name
                        dataOut.writeByte(1);
                        dataOut.writeUTF(name());
                        dataOut.flush();

                        while (clientRunning) {
                            byte protocol = dataIn.readByte();

                            switch (protocol) {
                                case 0:
                                    //kick the client
                                    clientRunning = false;

                                    //disconnect from the server
                                    dataOut.writeByte(0);
                                    dataOut.writeUTF("");
                                    dataOut.flush();

                                    //go to the main screen and display that the client was kicked
                                    uiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            activity.mainScreen();
                                            Button joinGameButton = activity.findViewById(R.id.joinButton);
                                            AlertDialog.Builder builder = new AlertDialog.Builder(joinGameButton.getContext());
                                            builder.setMessage("You have been kicked from the game").setPositiveButton("Ok", null).show();
                                        }
                                    });
                                    break;
                                case 1:
                                    //move to game_start_screen
                                    uiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            activity.gameStartScreen();
                                        }
                                    });
                                    break;
                                case 2:
                                    //receiving the player names and updating the game_start_screen name list
                                    String[] playerNames = dataIn.readUTF().split("\n");
                                    for (int i = 0; i < playerNames.length; i++) {
                                        int index = i;
                                        uiHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                setPlayerNameDisplay(index, playerNames[index], false, activity.getResources().getColor(R.color.teal_200));
                                            }
                                        });
                                    }
                                    break;
                            }
                        }

                        //close the client
                        dataIn.close();
                        dataOut.close();
                        server.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException();
                    }
                }
            }

            //start the client thread
            ClientSocket clientSocket = new ClientSocket();
            Thread thread = new Thread(clientSocket);
            thread.setUncaughtExceptionHandler(activity.uncaughtExceptionHandler);
            thread.start();
        }
    }

    private void setPlayerNameDisplay(int connectedPlayers, String playerName, boolean isHost, @ColorInt int color) {
        switch (connectedPlayers) {
            case 0:
                TextView playerOneName = activity.findViewById(R.id.playerOneName);
                playerOneName.setText(playerName);
                playerOneName.setTextColor(color);
                break;
            case 1:
                TextView playerTwoName = activity.findViewById(R.id.playerTwoName);
                playerTwoName.setText(playerName);
                playerTwoName.setTextColor(color);
                if (isHost) activity.kick2Group.addView(activity.kickPlayer2Button);
                break;
            case 2:
                TextView playerThreeName = activity.findViewById(R.id.playerThreeName);
                playerThreeName.setText(playerName);
                playerThreeName.setTextColor(color);
                if (isHost) activity.kick3Group.addView(activity.kickPlayer3Button);
                break;
            case 3:
                TextView playerFourName = activity.findViewById(R.id.playerFourName);
                playerFourName.setText(playerName);
                playerFourName.setTextColor(color);
                if (isHost) activity.kick4Group.addView(activity.kickPlayer4Button);
                break;
            case 4:
                TextView playerFiveName = activity.findViewById(R.id.playerFiveName);
                playerFiveName.setText(playerName);
                playerFiveName.setTextColor(color);
                if (isHost) activity.kick5Group.addView(activity.kickPlayer5Button);
                break;
            case 5:
                TextView playerSixName = activity.findViewById(R.id.playerSixName);
                playerSixName.setText(playerName);
                playerSixName.setTextColor(color);
                if (isHost) activity.kick6Group.addView(activity.kickPlayer6Button);
                break;
            case 6:
                TextView playerSevenName = activity.findViewById(R.id.playerSevenName);
                playerSevenName.setText(playerName);
                playerSevenName.setTextColor(color);
                if (isHost) activity.kick7Group.addView(activity.kickPlayer7Button);
                break;
            case 7:
                TextView playerEightName = activity.findViewById(R.id.playerEightName);
                playerEightName.setText(playerName);
                playerEightName.setTextColor(color);
                if (isHost) activity.kick8Group.addView(activity.kickPlayer8Button);
                break;
        }
    }
}
