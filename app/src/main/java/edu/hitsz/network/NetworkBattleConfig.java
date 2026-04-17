package edu.hitsz.network;

public class NetworkBattleConfig {

    public enum Mode {
        PVE,
        PVP
    }

    private final Mode mode;
    private final boolean host;
    private final String hostAddress;
    private final int port;
    private final boolean emulatorMode;

    public NetworkBattleConfig(Mode mode, boolean host, String hostAddress, int port) {
        this(mode, host, hostAddress, port, false);
    }

    public NetworkBattleConfig(Mode mode, boolean host, String hostAddress, int port, boolean emulatorMode) {
        this.mode = mode;
        this.host = host;
        this.hostAddress = hostAddress;
        this.port = port;
        this.emulatorMode = emulatorMode;
    }

    public Mode getMode() {
        return mode;
    }

    public boolean isHost() {
        return host;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public int getPort() {
        return port;
    }

    public boolean isEmulatorMode() {
        return emulatorMode;
    }
}
