package edu.hitsz.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class NetworkSession {

    public interface Listener {
        void onConnected();

        void onMessage(String line);

        void onDisconnected(String reason);
    }

    private final boolean hostRole;
    private final String hostAddress;
    private final int port;
    private final Listener listener;

    private volatile boolean running = false;
    private volatile Socket socket;
    private volatile ServerSocket serverSocket;
    private volatile BufferedWriter writer;

    public NetworkSession(boolean hostRole, String hostAddress, int port, Listener listener) {
        this.hostRole = hostRole;
        this.hostAddress = hostAddress;
        this.port = port;
        this.listener = listener;
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        new Thread(this::connectAndReadLoop, "network-session-connect").start();
    }

    public synchronized void sendLine(String line) {
        if (!running || writer == null) {
            return;
        }
        try {
            writer.write(line);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            closeWithReason("发送失败");
        }
    }

    public void close() {
        closeWithReason(null);
    }

    private void connectAndReadLoop() {
        try {
            if (hostRole) {
                serverSocket = new ServerSocket(port);
                socket = serverSocket.accept();
            } else {
                Socket client = new Socket();
                client.connect(new InetSocketAddress(hostAddress, port), 8000);
                socket = client;
            }

            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            listener.onConnected();

            String line;
            while (running && (line = reader.readLine()) != null) {
                listener.onMessage(line);
            }

            if (running) {
                closeWithReason("连接已断开");
            }
        } catch (IOException e) {
            if (running) {
                closeWithReason(hostRole ? "等待连接失败" : "连接房主失败");
            }
        }
    }

    private synchronized void closeWithReason(String reason) {
        if (!running) {
            return;
        }
        running = false;

        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ignored) {
            }
            writer = null;
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            socket = null;
        }

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
            serverSocket = null;
        }

        if (reason != null) {
            listener.onDisconnected(reason);
        }
    }
}
