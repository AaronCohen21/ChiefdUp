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
import android.view.ViewGroup;
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

        //this hides the action bar, but we can probably use this to display a title if necessary
        getSupportActionBar().hide();

        mainScreen();
    }

    /*
    =======
    SCREENS
    =======

    each function on called inflates the specific layout
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
                if (!nameText.getText().toString().equals("")) {
                    //create a new host client
                    client = new Client(nameText.getText().toString(), 0, true);

                    //change screen to game start screen
                    gameStartScreen();
                } else {
                    nameText.setHintTextColor(getResources().getColor(R.color.light_red));
                }
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
                if (!nameText.getText().toString().equals("")) {
                    try {
                        //try to create a new client with the provided game pin
                        int pin;
                        pin = Integer.parseInt(gamePin.getText().toString());
                        client = new Client(nameText.getText().toString(), pin, false);
                        //this is where the client should try to connect to the server, if the client can't connect, throw an exception


                        //change screen to game start screen
                        gameStartScreen();

                    } catch (NumberFormatException e) {
                        //if the provided pin isn't a number, make the editText field blank
                        gamePin.setText("");
                        gamePin.setHintTextColor(getResources().getColor(R.color.light_red));
                        gamePin.setHint("Invalid Pin");
                    }
                } else {
                    nameText.setHintTextColor(getResources().getColor(R.color.light_red));
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

        /*
        =========================
        game property text labels
        =========================
         */

        TextView roundsText = findViewById(R.id.roundsNumberText);
        TextView timeText = findViewById(R.id.drawingTimeText);

        /*
        =====================
        game property buttons
        =====================
         */

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

        /*
        ==================================
        set up and hide kickPlayer buttons
        ==================================
         */

        FloatingActionButton kickPlayer2Button = findViewById(R.id.kickPlayer2Button);
        kickPlayer2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.kickPlayer(1);
            }
        });
        ((ViewGroup) kickPlayer2Button.getParent()).removeView(kickPlayer2Button);

        FloatingActionButton kickPlayer3Button = findViewById(R.id.kickPlayer3Button);
        kickPlayer3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.kickPlayer(2);
            }
        });
        ((ViewGroup) kickPlayer3Button.getParent()).removeView(kickPlayer3Button);

        FloatingActionButton kickPlayer4Button = findViewById(R.id.kickPlayer4Button);
        kickPlayer4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.kickPlayer(3);
            }
        });
        ((ViewGroup) kickPlayer4Button.getParent()).removeView(kickPlayer4Button);

        FloatingActionButton kickPlayer5Button = findViewById(R.id.kickPlayer5Button);
        kickPlayer5Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.kickPlayer(4);
            }
        });
        ((ViewGroup) kickPlayer5Button.getParent()).removeView(kickPlayer5Button);

        FloatingActionButton kickPlayer6Button = findViewById(R.id.kickPlayer6Button);
        kickPlayer6Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.kickPlayer(5);
            }
        });
        ((ViewGroup) kickPlayer6Button.getParent()).removeView(kickPlayer6Button);

        FloatingActionButton kickPlayer7Button = findViewById(R.id.kickPlayer7Button);
        kickPlayer7Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.kickPlayer(6);
            }
        });
        ((ViewGroup) kickPlayer7Button.getParent()).removeView(kickPlayer7Button);

        FloatingActionButton kickPlayer8Button = findViewById(R.id.kickPlayer8Button);
        kickPlayer8Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.kickPlayer(7);
            }
        });
        ((ViewGroup) kickPlayer8Button.getParent()).removeView(kickPlayer8Button);

        /*
        ============================
        other buttons and misc. code
        ============================
         */

        Button startButton = findViewById(R.id.startGameButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //code to start the game
                drawScreen();

            }
        });

        FloatingActionButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //code to make the prompt to take the user to the home screen
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked

                                //disconnect the client
                                client.disconnect();

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
                builder.setMessage("Exit to main menu?" + (client.isHost() ? "\nThis will end the game for everyone else" : "")).setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
            }
        });

        if (!client.isHost()) {
            //remove host elements from ViewGroup

            //config elements
            ((ViewGroup) addRoundButton.getParent()).removeView(addRoundButton);
            ((ViewGroup) removeRoundButton.getParent()).removeView(removeRoundButton);
            ((ViewGroup) addTimeButton.getParent()).removeView(addTimeButton);
            ((ViewGroup) removeTimeButton.getParent()).removeView(removeTimeButton);

            //start game section
            ((ViewGroup) startButton.getParent()).removeView(startButton);
            ((ViewGroup) findViewById(R.id.gamePinDivider2).getParent()).removeView(findViewById(R.id.gamePinDivider2));
        }
    }

    public void drawScreen() {
        setContentView(R.layout.draw_screen);

        DrawCanvas drawCanvas = findViewById(R.id.drawCanvas);

        Button doneButton = findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

}