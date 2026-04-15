package edu.hitsz.network;

import android.os.Build;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LanHostDiscovery {

    private static final String MAGIC = "AIRCRAFT_DISCOVERY_V1";
    private static final String TYPE_REQUEST = "REQ";
    private static final String TYPE_RESPONSE = "RES";

    public static final int DISCOVERY_PORT = 24568;
    public static final long DEFAULT_SCAN_DURATION_MS = 1800L;

    private LanHostDiscovery() {
    }

    public interface ScanListener {
        void onHostFound(HostInfo hostInfo);

        void onScanFinished();

        void onScanError(String message);
    }

    public static final class HostInfo {
        public final String ipAddress;
        public final int gamePort;
        public final String mode;
        public final String deviceName;

        public HostInfo(String ipAddress, int gamePort, String mode, String deviceName) {
            this.ipAddress = ipAddress;
            this.gamePort = gamePort;
            this.mode = mode;
            this.deviceName = deviceName;
        }

        public String toDisplayText() {
            return deviceName + " (" + ipAddress + ":" + gamePort + ", " + mode + ")";
        }
    }

    public static final class ScanHandle implements Closeable {
        private final AtomicBoolean cancelled;
        private final DatagramSocket socket;
        private final Thread thread;

        private ScanHandle(AtomicBoolean cancelled, DatagramSocket socket, Thread thread) {
            this.cancelled = cancelled;
            this.socket = socket;
            this.thread = thread;
        }

        public void cancel() {
            cancelled.set(true);
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (thread != null) {
                thread.interrupt();
            }
        }

        @Override
        public void close() {
            cancel();
        }
    }

    public static final class HostResponder implements Closeable {
        private final int gamePort;
        private final String mode;
        private final String deviceName;

        private final AtomicBoolean running = new AtomicBoolean(false);
        private DatagramSocket socket;
        private Thread worker;

        public HostResponder(int gamePort, String mode) {
            this.gamePort = gamePort;
            this.mode = mode == null ? "UNKNOWN" : mode;
            this.deviceName = sanitize(Build.MODEL == null ? "Android" : Build.MODEL);
        }

        public synchronized void start() throws SocketException {
            if (running.get()) {
                return;
            }

            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(DISCOVERY_PORT));
            socket.setSoTimeout(500);
            running.set(true);

            worker = new Thread(this::runLoop, "lan-host-responder");
            worker.start();
        }

        private void runLoop() {
            byte[] buffer = new byte[256];
            while (running.get()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException ignored) {
                    continue;
                } catch (IOException e) {
                    if (running.get()) {
                        break;
                    }
                    continue;
                }

                String request = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                if (!isValidRequest(request)) {
                    continue;
                }

                String response = MAGIC + "|" + TYPE_RESPONSE + "|" + gamePort + "|" + mode + "|" + deviceName;
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                DatagramPacket responsePacket = new DatagramPacket(
                        responseBytes,
                        responseBytes.length,
                        packet.getAddress(),
                        packet.getPort()
                );
                try {
                    socket.send(responsePacket);
                } catch (IOException ignored) {
                }
            }
        }

        private boolean isValidRequest(String request) {
            if (request == null) {
                return false;
            }
            String[] parts = request.split("\\|");
            if (parts.length < 2) {
                return false;
            }
            return MAGIC.equals(parts[0]) && TYPE_REQUEST.equals(parts[1]);
        }

        @Override
        public synchronized void close() {
            running.set(false);
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (worker != null) {
                worker.interrupt();
                worker = null;
            }
            socket = null;
        }
    }

    public static ScanHandle scanHosts(int expectedGamePort, long durationMs, ScanListener listener) {
        AtomicBoolean cancelled = new AtomicBoolean(false);
        DatagramSocket socket;
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setSoTimeout(250);
        } catch (IOException e) {
            listener.onScanError(e.getMessage() == null ? "扫描初始化失败" : e.getMessage());
            listener.onScanFinished();
            return new ScanHandle(cancelled, null, null);
        }

        Thread worker = new Thread(() -> runScanLoop(socket, cancelled, expectedGamePort, durationMs, listener), "lan-host-scan");
        worker.start();
        return new ScanHandle(cancelled, socket, worker);
    }

    private static void runScanLoop(
            DatagramSocket socket,
            AtomicBoolean cancelled,
            int expectedGamePort,
            long durationMs,
            ScanListener listener
    ) {
        Set<String> dedupe = new HashSet<>();
        try {
            byte[] requestBytes = (MAGIC + "|" + TYPE_REQUEST + "|" + expectedGamePort)
                    .getBytes(StandardCharsets.UTF_8);
            DatagramPacket requestPacket = new DatagramPacket(
                    requestBytes,
                    requestBytes.length,
                    InetAddress.getByName("255.255.255.255"),
                    DISCOVERY_PORT
            );
            socket.send(requestPacket);

            long deadline = System.currentTimeMillis() + Math.max(500L, durationMs);
            byte[] buffer = new byte[512];

            while (!cancelled.get() && System.currentTimeMillis() < deadline) {
                DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(responsePacket);
                } catch (SocketTimeoutException ignored) {
                    continue;
                }

                HostInfo hostInfo = parseResponse(responsePacket, expectedGamePort);
                if (hostInfo == null) {
                    continue;
                }

                String key = hostInfo.ipAddress + ":" + hostInfo.gamePort;
                if (dedupe.add(key)) {
                    listener.onHostFound(hostInfo);
                }
            }
        } catch (Exception e) {
            if (!cancelled.get()) {
                listener.onScanError(e.getMessage() == null ? "扫描失败" : e.getMessage());
            }
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (!cancelled.get()) {
                listener.onScanFinished();
            }
        }
    }

    private static HostInfo parseResponse(DatagramPacket packet, int expectedGamePort) {
        String response = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
        String[] parts = response.split("\\|");
        if (parts.length < 5) {
            return null;
        }
        if (!MAGIC.equals(parts[0]) || !TYPE_RESPONSE.equals(parts[1])) {
            return null;
        }

        int gamePort;
        try {
            gamePort = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return null;
        }

        if (expectedGamePort > 0 && gamePort != expectedGamePort) {
            return null;
        }

        String mode = parts[3];
        String deviceName = parts[4].isEmpty() ? "Android" : parts[4];
        String ipAddress = packet.getAddress().getHostAddress();
        return new HostInfo(ipAddress, gamePort, mode, deviceName);
    }

    private static String sanitize(String text) {
        return text.replace("|", "_");
    }
}