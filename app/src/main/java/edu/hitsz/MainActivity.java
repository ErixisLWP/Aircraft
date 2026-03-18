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
        MaterialButton startGameButton = findViewById(R.id.startGameButton);
        startGameButton.setOnClickListener(view -> startGame());
    }

    private void startGame() {
        Game.gameMode = Game.GameMode.SIMPLE;
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