package edu.hitsz;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import edu.hitsz.application.AudioManager;
import edu.hitsz.application.Game;

public class MainActivity extends AppCompatActivity implements Game.GameStateListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AudioManager.init(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        showStartMenu();
    }

    private void showStartMenu() {
        setContentView(R.layout.activity_main);
        MaterialButton simpleGameButton = findViewById(R.id.simpleGameButton);
        MaterialButton normalGameButton = findViewById(R.id.normalGameButton);
        MaterialButton hardGameButton = findViewById(R.id.hardGameButton);

        simpleGameButton.setOnClickListener(view -> startGame(Game.GameMode.SIMPLE));
        normalGameButton.setOnClickListener(view -> startGame(Game.GameMode.NORMAL));
        hardGameButton.setOnClickListener(view -> startGame(Game.GameMode.HARD));
    }

    private void startGame(Game.GameMode mode) {
        Game.gameMode = mode;
        setContentView(new Game(this, this));
    }

    @Override
    public void onGameOver() {
        runOnUiThread(this::showStartMenu);
    }

    @Override
    protected void onDestroy() {
        AudioManager.release();
        super.onDestroy();
    }
}
