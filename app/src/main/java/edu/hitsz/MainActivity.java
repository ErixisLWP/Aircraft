package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import edu.hitsz.application.AudioManager;
import edu.hitsz.application.Game;
import edu.hitsz.network.NetworkBattleActivity;

public class MainActivity extends AppCompatActivity implements Game.GameStateListener {

    private static final int DEFAULT_NETWORK_PORT = 24567;

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
        MaterialButton networkBattleButton = findViewById(R.id.networkBattleButton);
        View settingsButton = findViewById(R.id.settingsButton);

        simpleGameButton.setOnClickListener(view -> startGame(Game.GameMode.SIMPLE));
        normalGameButton.setOnClickListener(view -> startGame(Game.GameMode.NORMAL));
        hardGameButton.setOnClickListener(view -> startGame(Game.GameMode.HARD));
        networkBattleButton.setOnClickListener(view -> showNetworkBattleDialog());
        settingsButton.setOnClickListener(view -> showSettingsDialog());
    }

    private void showNetworkBattleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.network_dialog_title));

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_network_battle, null);
        RadioGroup modeGroup = dialogView.findViewById(R.id.networkModeGroup);
        RadioGroup roleGroup = dialogView.findViewById(R.id.networkRoleGroup);
        RadioButton roleHostButton = dialogView.findViewById(R.id.networkRoleHost);
        EditText hostInput = dialogView.findViewById(R.id.networkHostInput);
        EditText portInput = dialogView.findViewById(R.id.networkPortInput);

        roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isHost = checkedId == R.id.networkRoleHost;
            hostInput.setEnabled(!isHost);
            hostInput.setAlpha(isHost ? 0.5f : 1f);
        });
        roleHostButton.setChecked(true);

        builder.setView(dialogView);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(R.string.network_confirm, null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            boolean isPve = modeGroup.getCheckedRadioButtonId() == R.id.networkModePve;
            boolean isHost = roleGroup.getCheckedRadioButtonId() == R.id.networkRoleHost;

            String host = hostInput.getText() == null ? "" : hostInput.getText().toString().trim();
            String portText = portInput.getText() == null ? "" : portInput.getText().toString().trim();
            int port;
            try {
                port = portText.isEmpty() ? DEFAULT_NETWORK_PORT : Integer.parseInt(portText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.network_invalid_port, Toast.LENGTH_SHORT).show();
                return;
            }

            if (port < 1024 || port > 65535) {
                Toast.makeText(this, R.string.network_invalid_port, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isHost && host.isEmpty()) {
                Toast.makeText(this, R.string.network_host_required, Toast.LENGTH_SHORT).show();
                return;
            }

            startNetworkBattle(isPve, isHost, host, port);
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void startNetworkBattle(boolean isPve, boolean isHost, String host, int port) {
        Intent intent = new Intent(this, NetworkBattleActivity.class);
        intent.putExtra(NetworkBattleActivity.EXTRA_MODE,
                isPve ? NetworkBattleActivity.MODE_PVE : NetworkBattleActivity.MODE_PVP);
        intent.putExtra(NetworkBattleActivity.EXTRA_ROLE_HOST, isHost);
        intent.putExtra(NetworkBattleActivity.EXTRA_HOST, host);
        intent.putExtra(NetworkBattleActivity.EXTRA_PORT, port);
        startActivity(intent);
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        CheckBox musicCheckBox = dialogView.findViewById(R.id.musicCheckBox);
        CheckBox soundCheckBox = dialogView.findViewById(R.id.soundCheckBox);

        musicCheckBox.setChecked(AudioManager.isMusicEnabled());
        soundCheckBox.setChecked(AudioManager.isSoundEffectEnabled());

        musicCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> AudioManager.setMusicEnabled(isChecked));
        soundCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> AudioManager.setSoundEffectEnabled(isChecked));

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
