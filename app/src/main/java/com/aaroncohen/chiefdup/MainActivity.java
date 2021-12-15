package com.aaroncohen.chiefdup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private Client client;

    public Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

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

        MainActivity activity = this;

        Button createButton = findViewById(R.id.createButton2);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!nameText.getText().toString().equals("")) {
                    //create a new host client
                    client = new Client(activity, nameText.getText().toString(), 0, true);

                    //change screen to game start screen and start the server
                    client.start();
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

        MainActivity activity = this;

        Button joinButton = findViewById(R.id.createButton3);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!nameText.getText().toString().equals("")) {
                    try {
                        //try to create a new client with the provided game pin
                        int pin;
                        pin = Integer.parseInt(gamePin.getText().toString());
                        client = new Client(activity, nameText.getText().toString(), pin, false);
                        //this is where the client should try to connect to the server, if the client can't connect, throw an exception
                        uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
                            @Override
                            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                                gamePin.setText("");
                                gamePin.setHintTextColor(getResources().getColor(R.color.light_red));
                                gamePin.setHint("Invalid Pin");
                            }
                        };
                        Thread thread = new Thread(client);
                        thread.start();
                        //the client will then change the screen to game_start_screen
                    } catch (Exception e) {
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

        EditText ceoName = findViewById(R.id.ceoName);
        DrawCanvas drawCanvas = findViewById(R.id.drawCanvas);
        drawCanvas.setCEOName(ceoName);
        drawCanvas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ceoName.clearFocus();
            }
        });
        PaintPreview paintPreview = findViewById(R.id.paintPreview);

        ImageView rivalPreview = findViewById(R.id.rivalPreview);

        SeekBar sizeBar = findViewById(R.id.sizeBar);
        sizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawCanvas.setDrawStroke((float) progress);
                paintPreview.setWidth((progress <= 5) ? 5f : ((float) progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ceoName.clearFocus();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //color buttons
        ArrayList<FloatingActionButton> colorButtons = new ArrayList<>();

        //get color buttons and add buttons to ArrayList
        FloatingActionButton redButton = findViewById(R.id.redButton);
        colorButtons.add(redButton);

        FloatingActionButton orangeButton = findViewById(R.id.orangeButon);
        colorButtons.add(orangeButton);

        FloatingActionButton yellowButton = findViewById(R.id.yellowButton);
        colorButtons.add(yellowButton);

        FloatingActionButton greenButton = findViewById(R.id.greenButton);
        colorButtons.add(greenButton);

        FloatingActionButton blueButton = findViewById(R.id.blueButton);
        colorButtons.add(blueButton);

        FloatingActionButton purpleButton = findViewById(R.id.purpleButton);
        colorButtons.add(purpleButton);

        FloatingActionButton brownButton = findViewById(R.id.brownButton);
        colorButtons.add(brownButton);

        FloatingActionButton blackButton = findViewById(R.id.blackButton);
        colorButtons.add(blackButton);

        FloatingActionButton eraserButton = findViewById(R.id.eraserButton);
        colorButtons.add(eraserButton);

        //add functionality to all buttons
        for (FloatingActionButton button : colorButtons) {
            button.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    ceoName.clearFocus();

                    //remove outline from all buttons
                    for (FloatingActionButton button : colorButtons) {
                        button.setForeground(null);
                    }

                    //test to see if the eraser was clicked
                    if (!button.equals(eraserButton)) { //check to make sure the eraser is not chosen
                        drawCanvas.setDrawColor(button.getBackgroundTintList().getDefaultColor());
                        paintPreview.setColor(button.getBackgroundTintList().getDefaultColor());
                        //put outline on the clicked button
                        button.setForeground(getResources().getDrawable(R.drawable.ic_circle_border_foreground));

                    } else {    //if the eraser is chosen set draw color to erase
                        drawCanvas.setDrawColor(getResources().getColor(R.color.white));
                        paintPreview.setEraserColor();

                        //put outline on the clicked button
                        button.setForeground(getResources().getDrawable(R.drawable.ic_circle_border_teal_foreground));
                    }
                }
            });
        }

        FloatingActionButton undoButton = findViewById(R.id.undoButton);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ceoName.clearFocus();
                drawCanvas.undo();
            }
        });

        //set up FlingAnimation for rivalPreview
        ViewCompat.setTranslationZ(rivalPreview, 90f);  //bring view to front

        final boolean[] flingLeft = {false};    //this boolean switches each fling to determine the fling direction
        final boolean[] canFling = {true};  //this boolean is only true when the view is not being animated

        //this is a calculated velocity that remains constant across all devices
        float pixelPerSecond = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 945,
                getResources().getDisplayMetrics());

        //set up the fling with correct velocity
        FlingAnimation fling = new FlingAnimation(rivalPreview, DynamicAnimation.X).setStartVelocity(pixelPerSecond);

        //tell the fling to set the canFling boolean back to true when the animation is finished
        fling.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                canFling[0] = true;
            }
        });

        //when the view is clicked, start the animation if the animation can be started
        rivalPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ceoName.clearFocus();

                if (canFling[0]) {
                    canFling[0] = false;

                    //set up fling direction and velocity for next fling
                    int flingVelocity = (int) (flingLeft[0] ? pixelPerSecond : -pixelPerSecond);
                    flingLeft[0] = !flingLeft[0];

                    //start the fling and apply the direction and velocity for the next fling
                    fling.start();
                    fling.setStartVelocity(flingVelocity);
                }
            }
        });

        Button doneButton = findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ceoName.clearFocus();

                //if there is no name for the ceo
                if (ceoName.getText().toString().equals("")) ceoName.setText("...");

                //lock the drawing elements
                drawCanvas.locked = true;
                ceoName.setFocusable(false);
                ceoName.setEnabled(false);

                //remove functionality from the undo button
                undoButton.setOnClickListener(null);

                //get a bitmap of the drawCanvas
                Bitmap drawScreenBitmap = drawCanvas.getBitmap();
                rivalPreview.setImageBitmap(drawScreenBitmap);
            }
        });

        FloatingActionButton homeButton = findViewById(R.id.drawScreenHomeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ceoName.clearFocus();

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
    }

}