package com.aaroncohen.chiefdup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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

        Button startButton = findViewById(R.id.startGameButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainScreen();
            }
        });

        if (!client.isHost()) {
            //remove host elements from ViewGroup

        }
    }
}