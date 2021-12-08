package com.aaroncohen.chiefdup;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.*;

public class Client {

    private String name;
    private int pin;
    private boolean isHost;

    public Client(String name, int pin, boolean isHost) {
        this.name = name;
        this.isHost = isHost;

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



    public boolean isHost() {
        return isHost;
    }

    public String name() {
        return name;
    }

    public int pin() {
        return pin;
    }

}
