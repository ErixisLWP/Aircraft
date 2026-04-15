package edu.hitsz.network;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class NetworkPeerSession {

    public interface Listener {
        void onConnected();

        void onMessage(JSONObject message);

        void onError(String message);

        void onDisconnected();
    }

    private final NetworkBattleConfig config;
    private final Listener listener;
    private final Object sendLock = new Object();

    private volatile boolean running = false;

    private Thread ioThread;
    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private LanHostDiscovery.HostResponder hostResponder;

    public NetworkPeerSession(NetworkBattleConfig config, Listener listener) {
        this.config = config;
        this.listener = listener;
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        ioThread = new Thread(this::runIoLoop, "network-peer-session");
        ioThread.start();
    }

    private void runIoLoop() {
        try {
            establishConnection();
            listener.onConnected();
            readLoop();
        } catch (Exception e) {
            if (running) {
                listener.onError(e.getMessage() == null ? "Connection failed" : e.getMessage());
            }
        } finally {
            running = false;
            stopHostDiscoveryResponder();
            closeQuietly(reader);
            closeQuietly(writer);
            closeQuietly(socket);
            closeQuietly(serverSocket);
            listener.onDisconnected();
        }
    }

    private void establishConnection() throws IOException {
        if (config.isHost()) {
            startHostDiscoveryResponder();
            serverSocket = new ServerSocket(config.getPort());
            socket = serverSocket.accept();
            stopHostDiscoveryResponder();
        } else {
            socket = new Socket(config.getHostAddress(), config.getPort());
        }
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    private void readLoop() throws Exception {
        String line;
        while (running && (line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            JSONObject message = new JSONObject(line);
            listener.onMessage(message);
        }
    }

    public void send(JSONObject message) {
        if (!running || writer == null || message == null) {
            return;
        }
        synchronized (sendLock) {
            try {
                writer.write(message.toString());
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                listener.onError("Send failed");
                close();
            }
        }
    }

    public void close() {
        running = false;
        stopHostDiscoveryResponder();
        closeQuietly(socket);
        closeQuietly(serverSocket);
    }

    private void startHostDiscoveryResponder() {
        if (hostResponder != null) {
            return;
        }
        try {
            hostResponder = new LanHostDiscovery.HostResponder(config.getPort(), config.getMode().name());
            hostResponder.start();
        } catch (IOException ignored) {
            hostResponder = null;
        }
    }

    private void stopHostDiscoveryResponder() {
        if (hostResponder == null) {
            return;
        }
        hostResponder.close();
        hostResponder = null;
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }
}
