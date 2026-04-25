package edu.hitsz.network;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class NetworkBattleActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "extra_mode";
    public static final String EXTRA_ROLE_HOST = "extra_role_host";
    public static final String EXTRA_HOST = "extra_host";
    public static final String EXTRA_PORT = "extra_port";
    public static final String EXTRA_EMULATOR_MODE = "extra_emulator_mode";

    public static final int MODE_PVE = 1;
    public static final int MODE_PVP = 2;

    private NetworkBattleView networkBattleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        int mode = getIntent().getIntExtra(EXTRA_MODE, MODE_PVE);
        boolean hostRole = getIntent().getBooleanExtra(EXTRA_ROLE_HOST, true);
        String host = getIntent().getStringExtra(EXTRA_HOST);
        int port = getIntent().getIntExtra(EXTRA_PORT, 24667);
        boolean emulatorMode = getIntent().getBooleanExtra(EXTRA_EMULATOR_MODE, false);

        NetworkBattleConfig.Mode battleMode = mode == MODE_PVP
            ? NetworkBattleConfig.Mode.PVP
            : NetworkBattleConfig.Mode.PVE;
        NetworkBattleConfig config = new NetworkBattleConfig(
            battleMode,
            hostRole,
            host == null ? "" : host,
            port,
            emulatorMode
        );

        networkBattleView = new NetworkBattleView(this, config, this::finish);
        setContentView(networkBattleView);
    }

    @Override
    protected void onDestroy() {
        if (networkBattleView != null) {
            networkBattleView.release();
        }
        super.onDestroy();
    }
}
