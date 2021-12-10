package com.aaroncohen.chiefdup;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.*;

public class Client {

    private String name;
    private int pin, rounds, time;
    private boolean isHost;

    public Client(String name, int pin, boolean isHost) {
        //client properties
        this.name = name;
        this.isHost = isHost;

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
        return mins + ":" + ((copyOfTime == 0) ? "00" : copyOfTime);
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

}
