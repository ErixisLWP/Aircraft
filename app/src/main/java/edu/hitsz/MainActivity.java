package edu.hitsz;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;
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
        View settingsButton = findViewById(R.id.settingsButton);

        simpleGameButton.setOnClickListener(view -> startGame(Game.GameMode.SIMPLE));
        normalGameButton.setOnClickListener(view -> startGame(Game.GameMode.NORMAL));
        hardGameButton.setOnClickListener(view -> startGame(Game.GameMode.HARD));
        settingsButton.setOnClickListener(view -> showSettingsDialog());
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        CheckBox musicCheckBox = dialogView.findViewById(R.id.musicCheckBox);
        CheckBox soundCheckBox = dialogView.findViewById(R.id.soundCheckBox);

        musicCheckBox.setChecked(AudioManager.isMusicEnabled());
        soundCheckBox.setChecked(AudioManager.isSoundEffectEnabled());

        musicCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioManager.setMusicEnabled(isChecked);
        });

        soundCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioManager.setSoundEffectEnabled(isChecked);
        });

        builder.setView(dialogView);
        builder.setPositiveButton("确定", null);
        builder.show();
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
