package com.aaroncohen.chiefdup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //this hides the action bar, but we can probably use this to display a title if neccesarry
        getSupportActionBar().hide();

        mainScreen();
    }

    /*
    Screens: each function on called inflates the specific layout
    for each screen, and then gives all views functionality
     */

    public void mainScreen() {
        setContentView(R.layout.activity_main);

        //set up main screen
        Button createGame = findViewById(R.id.createButton);
        createGame.setOnClickListener(new View.OnClickListener() {      //create game button
            @Override
            public void onClick(View v) {
                createGameScreen();
            }
        });

        Button joinGame = findViewById(R.id.joinButton);
        joinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinGameScreen();
            }
        });
    }

    public void createGameScreen() {
        setContentView(R.layout.create_game);

        FloatingActionButton exit = findViewById(R.id.backToMainScreen);
        exit.setOnClickListener(new View.OnClickListener() {            //back button
            @Override
            public void onClick(View v) {
                mainScreen();
            }
        });

        EditText nameText = findViewById(R.id.editText);

        Button createButton = findViewById(R.id.createButton2);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create a new host client
                client = new Client(nameText.getText().toString(), 0, true);

                //change screen to game start screen
                gameStartScreen();
            }
        });
    }

    public void joinGameScreen() {
        setContentView(R.layout.join_game);

        FloatingActionButton exit = findViewById(R.id.backToMainScreen2);
        exit.setOnClickListener(new View.OnClickListener() {            //back button
            @Override
            public void onClick(View v) {
                mainScreen();
            }
        });

        EditText nameText = findViewById(R.id.nameField);
        EditText gamePin = findViewById(R.id.gamePin);

        Button joinButton = findViewById(R.id.createButton3);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //try to create a new client with the provided game pin
                    int pin;
                    pin = Integer.parseInt(gamePin.getText().toString());
                    client = new Client(nameText.getText().toString(), pin, false);
                    //this is where the client should try to connect to the server, if the client can't connect, throw an exception




                    //change screen to game start screen
                    gameStartScreen();

                } catch (NumberFormatException e){
                    //if the provided pin isn't a number, make the editText field blank
                    gamePin.setText("");
                    gamePin.setHintTextColor(getResources().getColor(R.color.light_red));
                    gamePin.setHint("Invalid Pin");
                }
            }
        });
    }

    public void gameStartScreen() {
        setContentView(R.layout.game_start_screen);

        TextView title = findViewById(R.id.gameTitle);
        title.setText("Game: " + client.pin());

        //set up player names
        if (client.isHost()) {
            TextView playerOneName = findViewById(R.id.playerOneName);
            playerOneName.setText(client.name());
            playerOneName.setTextColor(getResources().getColor(R.color.teal_200));
        }

        //game property text labels
        TextView roundsText = findViewById(R.id.roundsNumberText);
        TextView timeText = findViewById(R.id.drawingTimeText);

        //game property buttons
        FloatingActionButton addRoundButton = findViewById(R.id.addRoundButton);
        addRoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.addRound();
                roundsText.setText("Rounds: " + client.getRounds());
            }
        });

        FloatingActionButton removeRoundButton = findViewById(R.id.removeRoundButton);
        removeRoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.removeRound();
                roundsText.setText("Rounds: " + client.getRounds());
            }
        });

        FloatingActionButton addTimeButton = findViewById(R.id.addTimeButton);
        addTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.addTime();
                timeText.setText("Drawing Time: " + client.getTime());
            }
        });

        FloatingActionButton removeTimeButton = findViewById(R.id.removeTimeButton);
        removeTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.removeTime();
                timeText.setText("Drawing Time: " + client.getTime());
            }
        });


        Button startButton = findViewById(R.id.startGameButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //code to start the game


            }
        });

        FloatingActionButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //code to make the prompt to take the user to the homescreen
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked

                                //disconnect the client


                                //go to main menu
                                mainScreen();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked, do nothing
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Exit To Main Menu?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
            }
        });

        if (!client.isHost()) {
            //remove host elements from ViewGroup

        }
    }

}