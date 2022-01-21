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
        clientDisconnected = false;

        //host properties
        makeNewClients = true;

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
        //edit the number of rounds
        rounds += (rounds < 8) ? 1 : 0;
        //send changes to all clients
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (PlayerSocket player : playerSocketArrayList) {
                        DataOutputStream dataOut = new DataOutputStream(player.client.getOutputStream());
                        dataOut.writeByte(3);
                        dataOut.writeInt(rounds);
                        dataOut.flush();
                    }
                } catch (Exception e) {
                    //do nothing
                }
            }
        });
        thread.start();
        try { thread.join(); } catch (Exception e) {}
    }
    public void removeRound() {
        rounds -= (rounds > 1) ? 1 : 0;
        //send changes to all clients
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (PlayerSocket player : playerSocketArrayList) {
                        DataOutputStream dataOut = new DataOutputStream(player.client.getOutputStream());
                        dataOut.writeByte(3);
                        dataOut.writeInt(rounds);
                        dataOut.flush();
                    }
                } catch (Exception e) {
                    //do nothing
                }
            }
        });
        thread.start();
        try { thread.join(); } catch (Exception e) {}
    }
    public void addTime() {
        time += (time < 360) ? 30 : 0;
        //send changes to all clients
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (PlayerSocket player : playerSocketArrayList) {
                        DataOutputStream dataOut = new DataOutputStream(player.client.getOutputStream());
                        dataOut.writeByte(4);
                        dataOut.writeInt(time);
                        dataOut.flush();
                    }
                } catch (Exception e) {
                    //do nothing
                }
            }
        });
        thread.start();
        try { thread.join(); } catch (Exception e) {}
    }
    public void removeTime() {
        time -= (time > 60) ? 30 : 0;
        //send changes to all clients
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (PlayerSocket player : playerSocketArrayList) {
                        DataOutputStream dataOut = new DataOutputStream(player.client.getOutputStream());
                        dataOut.writeByte(4);
                        dataOut.writeInt(time);
                        dataOut.flush();
                    }
                } catch (Exception e) {
                    //do nothing
                }
            }
        });
        thread.start();
        try { thread.join(); } catch (Exception e) {}
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
        if (this.isHost) {
            //kick all players from game
            makeNewClients = false;
            for (PlayerSocket player : playerSocketArrayList) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DataOutputStream dataOut = new DataOutputStream(player.client.getOutputStream());
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
            }
            //close the server socket
            try { socket.close(); } catch (Exception e) {}
            //go to the main screen
            activity.mainScreen();
        } else {
            //disconnect from server and then leave
            clientDisconnected = true;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DataOutputStream dataOut = new DataOutputStream(server.getOutputStream());
                        dataOut.writeByte(0);
                        dataOut.writeUTF("disconnected");
                        dataOut.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            try {
                thread.join();
            } catch (Exception e) {
                //do nothing
            }
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

        //remove the player from the playerSocketArrayList
        playerSocketArrayList.remove(playerToKick);
        playerNames.remove(playerNumber + 1);
        //a new playerSocket will be started up automatically

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
                            String message = dataIn.readUTF();
                            serverRunning = message.equals("Chief'd Up!"); //if the sent String is from a chief'd up client then set up the server, else disconnect

                            if (serverRunning) {
                                dataOut.writeUTF("Chief'd Up!");
                                dataOut.flush();
                                connectedPlayers++;
                            } else {
                                dataOut.writeByte(0);
                                dataOut.flush();
                                connectedPlayers--;
                                //TODO update the disconnected player on all other devices
                                if (message.equals("disconnected")) {
                                    int id = playerSocketArrayList.indexOf(this);
                                    //remove the player from the playerSocketArrayList
                                    playerSocketArrayList.remove(id);
                                    playerNames.remove(id + 1);

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

                                    //update the names for the host
                                    uiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            //reset the game start screen and then fill in all remaining player names
                                            activity.gameStartScreen();
                                            for (int i = 1; i < playerNames.size(); i++) {
                                                setPlayerNameDisplay(i, playerNames.get(i), true, activity.getResources().getColor(R.color.teal_200));
                                            }
                                        }
                                    });
                                }
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

                            //update each client's name list, round number, and time data
                            for (PlayerSocket player : playerSocketArrayList) {
                                //get the player's DataOutputStream
                                DataOutputStream playerDataOut = new DataOutputStream(player.client.getOutputStream());

                                //write the name list
                                playerDataOut.writeByte(2);
                                playerDataOut.writeUTF(playerNamesToSend);
                                playerDataOut.flush();

                                //write the round number
                                playerDataOut.writeByte(3);
                                playerDataOut.writeInt(getRounds());
                                playerDataOut.flush();

                                //write the time data
                                playerDataOut.writeByte(4);
                                playerDataOut.writeInt(time);
                                playerDataOut.flush();
                            }
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

            //After the player socket is "closed" reset the socket and wait for a new client to join
            if (makeNewClients) {
                try {
                    client = null;
                    client = socket.accept();
                    playerSocketArrayList.add(this);
                    run();  //recurse on function to loop. This probably isn't the best way to do this but it's easy
                } catch (Exception e) {
                    System.err.println("PlayerSocket could not reopen connection");
                    e.printStackTrace();
                }
            }
        }
    }

    //an extremely unorganized mess of networking variables needed on a global level
    ArrayList<PlayerSocket> playerSocketArrayList;
    private int connectedPlayers;
    private boolean clientRunning;
    ArrayList<String> playerNames;
    private ServerSocket socket;
    private boolean makeNewClients;
    private String[] clientPlayerNames;
    private Socket server;
    private boolean clientDisconnected;

    @Override
    public void run() {
        this.setUncaughtExceptionHandler(activity.uncaughtExceptionHandler);
        if (this.isHost) {
            connectedPlayers = 0;
            playerNames = new ArrayList<>();
            playerNames.add(this.name());

            //define the server socket
            socket = null;
            try {
                socket = new ServerSocket(pin);
            } catch (Exception e) {
                //do nothing
            }

            //start the server and create all 7 player sockets
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
                    //do nothing, if something goes wrong it is because the host has stopped the game
                }
            }

        } else {
            class ClientSocket implements Runnable {
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
                                            if (!clientDisconnected) {
                                                Button joinGameButton = activity.findViewById(R.id.joinButton);
                                                AlertDialog.Builder builder = new AlertDialog.Builder(joinGameButton.getContext());
                                                builder.setMessage("You have been kicked from the game").setPositiveButton("Ok", null).show();
                                            }
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
                                    uiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            activity.gameStartScreen();
                                        }
                                    });
                                    clientPlayerNames = dataIn.readUTF().split("\n");
                                    for (int i = 0; i < clientPlayerNames.length; i++) {
                                        int index = i;
                                        uiHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                setPlayerNameDisplay(index, clientPlayerNames[index], false, activity.getResources().getColor(R.color.teal_200));
                                            }
                                        });
                                    }
                                    break;
                                case 3:
                                    //recieving the rounds number
                                    rounds = dataIn.readInt();
                                    //refresh game_start_screen to display new information
                                    uiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            activity.gameStartScreen();
                                        }
                                    });
                                    //set the player names
                                    for (int i = 0; i < clientPlayerNames.length; i++) {
                                        int index = i;
                                        uiHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                setPlayerNameDisplay(index, clientPlayerNames[index], false, activity.getResources().getColor(R.color.teal_200));
                                            }
                                        });
                                    }
                                    break;
                                case 4:
                                    //recieving the time data
                                    time = dataIn.readInt();
                                    //refresh game_start_screen to display new information
                                    uiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            activity.gameStartScreen();
                                        }
                                    });
                                    //set the player names
                                    for (int i = 0; i < clientPlayerNames.length; i++) {
                                        int index = i;
                                        uiHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                setPlayerNameDisplay(index, clientPlayerNames[index], false, activity.getResources().getColor(R.color.teal_200));
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

    private void setPlayerNameDisplay(int playerNumber, String playerName, boolean isHost, @ColorInt int color) {
        switch (playerNumber) {
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
