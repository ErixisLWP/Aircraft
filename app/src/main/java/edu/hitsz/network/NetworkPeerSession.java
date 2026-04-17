package edu.hitsz.network;

import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
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
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(config.getPort()));
            socket = serverSocket.accept();
            stopHostDiscoveryResponder();
        } else {
            String hostAddress = resolveHostAddress(config.getHostAddress(), config.isEmulatorMode());
            Socket client = new Socket();
            client.connect(new InetSocketAddress(hostAddress, config.getPort()), 8000);
            socket = client;
        }
        socket.setTcpNoDelay(true);
        socket.setKeepAlive(true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    private String resolveHostAddress(String rawHostAddress, boolean emulatorMode) throws IOException {
        String hostAddress = rawHostAddress == null ? "" : rawHostAddress.trim();
        boolean shouldUseEmulatorAlias = emulatorMode || isRunningOnEmulator();

        if (hostAddress.isEmpty()) {
            if (shouldUseEmulatorAlias) {
                return "10.0.2.2";
            }
            throw new IOException("房主地址为空");
        }

        if (shouldUseEmulatorAlias && isLoopbackHost(hostAddress)) {
            return "10.0.2.2";
        }
        return hostAddress;
    }

    private boolean isLoopbackHost(String hostAddress) {
        return "127.0.0.1".equals(hostAddress) || "localhost".equalsIgnoreCase(hostAddress);
    }

    private boolean isRunningOnEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.contains("vbox")
                || Build.FINGERPRINT.contains("emulator")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || "google_sdk".equals(Build.PRODUCT);
    }

    private void readLoop() throws Exception {
        String line;
        while (running && (line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            try {
                JSONObject message = new JSONObject(line);
                listener.onMessage(message);
            } catch (JSONException ignored) {
                // Ignore malformed packets and keep the session alive.
            }
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
                String reason = e.getMessage() == null ? "Send failed" : "Send failed: " + e.getMessage();
                listener.onError(reason);
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
