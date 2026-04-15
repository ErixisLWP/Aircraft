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

    public NetworkBattleConfig(Mode mode, boolean host, String hostAddress, int port) {
        this.mode = mode;
        this.host = host;
        this.hostAddress = hostAddress;
        this.port = port;
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
}
