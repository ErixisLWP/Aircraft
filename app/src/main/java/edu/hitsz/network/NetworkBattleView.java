package edu.hitsz.network;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.hitsz.R;

public class NetworkBattleView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    public interface ExitListener {
        void onExitRequested();
    }

    private static final String TYPE_INPUT = "INPUT";
    private static final String TYPE_STATE = "STATE";

    private static final String RESULT_NONE = "NONE";
    private static final String RESULT_PVE_WIN = "PVE_WIN";
    private static final String RESULT_PVE_FAIL = "PVE_FAIL";
    private static final String RESULT_PVP_P0_WIN = "PVP_P0_WIN";
    private static final String RESULT_PVP_P1_WIN = "PVP_P1_WIN";
    private static final String RESULT_PVP_DRAW = "PVP_DRAW";

    private static final long FRAME_MS = 40L;
    private static final long SHOOT_INTERVAL_MS = 350L;
    private static final long ENEMY_SPAWN_MS = 800L;
    private static final long STATE_PUSH_MS = 80L;
    private static final long INPUT_PUSH_MS = 50L;
    private static final long PVP_TIMEOUT_MS = 90_000L;

    private final NetworkBattleConfig config;
    private final ExitListener exitListener;
    private final SurfaceHolder holder;
    private final NetworkPeerSession session;

    private final Object stateLock = new Object();
    private final Random random = new Random();

    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint enemyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bulletP0Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bulletP1Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint player0Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint player1Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint disabledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final PlayerState[] players = new PlayerState[] {new PlayerState(), new PlayerState()};
    private final List<BulletState> bullets = new ArrayList<>();
    private final List<EnemyState> enemies = new ArrayList<>();

    private Thread renderThread;
    private volatile boolean drawing = false;

    private int worldWidth;
    private int worldHeight;
    private boolean worldReady = false;

    private final boolean host;
    private final boolean pve;
    private final int localPlayerIndex;
    private final int remotePlayerIndex;

    private volatile boolean connected = false;
    private volatile boolean disconnected = false;
    private volatile boolean gameOver = false;

    private volatile float localInputX;
    private volatile float localInputY;
    private volatile float remoteInputX;
    private volatile float remoteInputY;
    private volatile long nextInputPushAt = 0L;
    private volatile boolean inputDirty = true;
    private volatile float lastSentInputX = Float.NaN;
    private volatile float lastSentInputY = Float.NaN;

    private volatile String statusText;
    private volatile String resultCode = RESULT_NONE;

    private long elapsedMs = 0L;
    private long nextShootAt = 0L;
    private long nextEnemySpawnAt = 0L;
    private long nextStatePushAt = 0L;
    private int sharedScore = 0;

    public NetworkBattleView(Context context, NetworkBattleConfig config, ExitListener exitListener) {
        super(context);
        this.config = config;
        this.exitListener = exitListener;
        this.host = config.isHost();
        this.pve = config.getMode() == NetworkBattleConfig.Mode.PVE;
        this.localPlayerIndex = host ? 0 : 1;
        this.remotePlayerIndex = host ? 1 : 0;
        this.statusText = host ? string(R.string.network_waiting_client) : string(R.string.network_waiting_host);

        holder = getHolder();
        holder.addCallback(this);
        setKeepScreenOn(true);

        initPaints();
        setOnTouchListener((v, event) -> handleTouch(event));

        session = new NetworkPeerSession(config, new NetworkPeerSession.Listener() {
            @Override
            public void onConnected() {
                connected = true;
                disconnected = false;
                statusText = "";
                inputDirty = true;
                maybeSendClientInput(true);
            }

            @Override
            public void onMessage(JSONObject message) {
                handleNetworkMessage(message);
            }

            @Override
            public void onError(String message) {
                disconnected = true;
                if (message == null || message.trim().isEmpty()) {
                    statusText = string(R.string.network_disconnect);
                } else {
                    statusText = message;
                }
            }

            @Override
            public void onDisconnected() {
                if (!gameOver) {
                    disconnected = true;
                    statusText = string(R.string.network_disconnect);
                }
            }
        });
        session.start();
    }

    private void initPaints() {
        float density = getResources().getDisplayMetrics().density;

        backgroundPaint.setColor(Color.rgb(12, 20, 28));

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(22f * density);
        textPaint.setFakeBoldText(true);

        subTextPaint.setColor(Color.rgb(220, 228, 240));
        subTextPaint.setTextSize(16f * density);

        overlayPaint.setColor(Color.argb(180, 0, 0, 0));

        enemyPaint.setColor(Color.rgb(255, 107, 107));
        bulletP0Paint.setColor(Color.rgb(122, 229, 130));
        bulletP1Paint.setColor(Color.rgb(110, 193, 255));
        player0Paint.setColor(Color.rgb(122, 229, 130));
        player1Paint.setColor(Color.rgb(110, 193, 255));
        disabledPaint.setColor(Color.GRAY);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        if (renderThread != null && renderThread.isAlive()) {
            return;
        }
        drawing = true;
        renderThread = new Thread(this, "network-battle-loop");
        renderThread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int format, int width, int height) {
        synchronized (stateLock) {
            worldWidth = width;
            worldHeight = height;
            initWorldStateLocked();
            worldReady = true;
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        stopLoop();
        session.close();
    }

    @Override
    public void run() {
        while (drawing) {
            long frameStart = SystemClock.uptimeMillis();
            if (connected && host && worldReady && !gameOver) {
                updateHostGame();
            } else if (connected && !host && worldReady && !gameOver && !disconnected) {
                maybeSendClientInput(false);
            }
            drawFrame();
            long elapsed = SystemClock.uptimeMillis() - frameStart;
            long sleepTime = Math.max(0L, FRAME_MS - elapsed);
            SystemClock.sleep(sleepTime);
        }
    }

    private void stopLoop() {
        drawing = false;
        if (renderThread == null) {
            return;
        }
        try {
            renderThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void initWorldStateLocked() {
        if (worldWidth <= 0 || worldHeight <= 0) {
            return;
        }

        if (pve) {
            players[0].x = worldWidth * 0.35f;
            players[0].y = worldHeight * 0.82f;
            players[1].x = worldWidth * 0.65f;
            players[1].y = worldHeight * 0.82f;
        } else {
            players[0].x = worldWidth * 0.5f;
            players[0].y = worldHeight * 0.82f;
            players[1].x = worldWidth * 0.5f;
            players[1].y = worldHeight * 0.18f;
        }
        players[0].hp = 120;
        players[1].hp = 120;

        localInputX = players[localPlayerIndex].x;
        localInputY = players[localPlayerIndex].y;
        remoteInputX = players[remotePlayerIndex].x;
        remoteInputY = players[remotePlayerIndex].y;
        inputDirty = true;
        lastSentInputX = Float.NaN;
        lastSentInputY = Float.NaN;
        nextInputPushAt = 0L;

        bullets.clear();
        enemies.clear();

        elapsedMs = 0L;
        nextShootAt = SHOOT_INTERVAL_MS;
        nextEnemySpawnAt = ENEMY_SPAWN_MS;
        nextStatePushAt = STATE_PUSH_MS;
        sharedScore = 0;
        gameOver = false;
        resultCode = RESULT_NONE;
    }

    private boolean handleTouch(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN && event.getAction() != MotionEvent.ACTION_MOVE) {
            if ((gameOver || disconnected) && event.getAction() == MotionEvent.ACTION_UP && exitListener != null) {
                exitListener.onExitRequested();
            }
            return true;
        }

        if (!worldReady) {
            return true;
        }

        if (gameOver || disconnected) {
            return true;
        }

        float x = clamp(event.getX(), 32f, worldWidth - 32f);
        float y = clampYForPlayer(localPlayerIndex, event.getY());
        localInputX = x;
        localInputY = y;
        inputDirty = true;

        if (!host) {
            maybeSendClientInput(false);
        }
        return true;
    }

    private float clampYForPlayer(int playerIndex, float y) {
        float minY = 32f;
        float maxY = worldHeight - 32f;
        if (!pve) {
            if (playerIndex == 0) {
                minY = worldHeight * 0.5f;
            } else {
                maxY = worldHeight * 0.5f;
            }
        } else {
            minY = worldHeight * 0.42f;
        }
        return clamp(y, minY, maxY);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void maybeSendClientInput(boolean force) {
        if (!connected || host || !worldReady || disconnected) {
            return;
        }

        long now = SystemClock.uptimeMillis();
        if (!force && now < nextInputPushAt) {
            return;
        }

        float x = localInputX;
        float y = localInputY;
        boolean moved = Float.isNaN(lastSentInputX)
                || Math.abs(lastSentInputX - x) > 0.5f
                || Math.abs(lastSentInputY - y) > 0.5f;
        if (!force && !inputDirty && !moved) {
            nextInputPushAt = now + INPUT_PUSH_MS;
            return;
        }

        JSONObject input = new JSONObject();
        try {
            input.put("type", TYPE_INPUT);
            input.put("x", x);
            input.put("y", y);
            session.send(input);
            lastSentInputX = x;
            lastSentInputY = y;
            inputDirty = false;
            nextInputPushAt = now + INPUT_PUSH_MS;
        } catch (JSONException ignored) {
        }
    }

    private void handleNetworkMessage(JSONObject message) {
        if (host) {
            String type = message.optString("type", "");
            if (!TYPE_INPUT.equals(type)) {
                return;
            }
            remoteInputX = (float) message.optDouble("x", remoteInputX);
            remoteInputY = (float) message.optDouble("y", remoteInputY);
            return;
        }

        String type = message.optString("type", "");
        if (!TYPE_STATE.equals(type)) {
            return;
        }
        applyStateFromHost(message);
    }

    private void applyStateFromHost(JSONObject stateMessage) {
        synchronized (stateLock) {
            elapsedMs = stateMessage.optLong("time", elapsedMs);
            sharedScore = stateMessage.optInt("score", sharedScore);
            gameOver = stateMessage.optBoolean("gameOver", gameOver);
            resultCode = stateMessage.optString("result", resultCode);

            JSONArray playerArray = stateMessage.optJSONArray("players");
            if (playerArray != null && playerArray.length() >= 2) {
                applyPlayerFromJson(playerArray.optJSONObject(0), players[0]);
                applyPlayerFromJson(playerArray.optJSONObject(1), players[1]);
            }

            bullets.clear();
            JSONArray bulletArray = stateMessage.optJSONArray("bullets");
            if (bulletArray != null) {
                for (int i = 0; i < bulletArray.length(); i++) {
                    JSONObject item = bulletArray.optJSONObject(i);
                    if (item == null) {
                        continue;
                    }
                    BulletState bullet = new BulletState();
                    bullet.x = (float) item.optDouble("x", 0);
                    bullet.y = (float) item.optDouble("y", 0);
                    bullet.vx = (float) item.optDouble("vx", 0);
                    bullet.vy = (float) item.optDouble("vy", 0);
                    bullet.owner = item.optInt("owner", 0);
                    bullets.add(bullet);
                }
            }

            enemies.clear();
            JSONArray enemyArray = stateMessage.optJSONArray("enemies");
            if (enemyArray != null) {
                for (int i = 0; i < enemyArray.length(); i++) {
                    JSONObject item = enemyArray.optJSONObject(i);
                    if (item == null) {
                        continue;
                    }
                    EnemyState enemy = new EnemyState();
                    enemy.x = (float) item.optDouble("x", 0);
                    enemy.y = (float) item.optDouble("y", 0);
                    enemy.vx = (float) item.optDouble("vx", 0);
                    enemy.vy = (float) item.optDouble("vy", 0);
                    enemy.hp = item.optInt("hp", 0);
                    enemies.add(enemy);
                }
            }
        }
    }

    private void applyPlayerFromJson(JSONObject json, PlayerState target) {
        if (json == null || target == null) {
            return;
        }
        target.x = (float) json.optDouble("x", target.x);
        target.y = (float) json.optDouble("y", target.y);
        target.hp = json.optInt("hp", target.hp);
    }

    private void updateHostGame() {
        synchronized (stateLock) {
            elapsedMs += FRAME_MS;

            players[localPlayerIndex].x = clamp(localInputX, 32f, worldWidth - 32f);
            players[localPlayerIndex].y = clampYForPlayer(localPlayerIndex, localInputY);
            players[remotePlayerIndex].x = clamp(remoteInputX, 32f, worldWidth - 32f);
            players[remotePlayerIndex].y = clampYForPlayer(remotePlayerIndex, remoteInputY);

            if (elapsedMs >= nextShootAt) {
                spawnBulletsForAlivePlayersLocked();
                nextShootAt += SHOOT_INTERVAL_MS;
            }

            if (pve && elapsedMs >= nextEnemySpawnAt) {
                spawnEnemyLocked();
                nextEnemySpawnAt += ENEMY_SPAWN_MS;
            }

            moveBulletsLocked();
            if (pve) {
                moveEnemiesLocked();
                resolvePveCollisionsLocked();
                resolvePveResultLocked();
            } else {
                resolvePvpCollisionsLocked();
                resolvePvpResultLocked();
            }

            if (elapsedMs >= nextStatePushAt) {
                sendHostStateLocked();
                nextStatePushAt += STATE_PUSH_MS;
            }
        }
    }

    private void spawnBulletsForAlivePlayersLocked() {
        for (int i = 0; i < players.length; i++) {
            if (players[i].hp <= 0) {
                continue;
            }
            BulletState bullet = new BulletState();
            bullet.owner = i;
            bullet.x = players[i].x;
            bullet.y = players[i].y + (i == 0 ? -22f : 22f);
            bullet.vx = 0f;
            if (pve) {
                bullet.vy = -16f;
            } else {
                bullet.vy = i == 0 ? -16f : 16f;
            }
            bullets.add(bullet);
        }
    }

    private void spawnEnemyLocked() {
        EnemyState enemy = new EnemyState();
        enemy.x = 40f + random.nextFloat() * (worldWidth - 80f);
        enemy.y = -30f;
        enemy.vx = random.nextBoolean() ? 1.5f : -1.5f;
        enemy.vy = 4f + random.nextFloat() * 2.2f;
        enemy.hp = 45;
        enemies.add(enemy);
    }

    private void moveBulletsLocked() {
        bullets.removeIf(b -> {
            b.x += b.vx;
            b.y += b.vy;
            return b.y < -40f || b.y > worldHeight + 40f || b.x < -40f || b.x > worldWidth + 40f;
        });
    }

    private void moveEnemiesLocked() {
        enemies.removeIf(enemy -> {
            enemy.x += enemy.vx;
            enemy.y += enemy.vy;
            if (enemy.x < 20f || enemy.x > worldWidth - 20f) {
                enemy.vx = -enemy.vx;
            }
            if (enemy.y > worldHeight + 40f) {
                players[0].hp = Math.max(0, players[0].hp - 10);
                players[1].hp = Math.max(0, players[1].hp - 10);
                return true;
            }
            return false;
        });
    }

    private void resolvePveCollisionsLocked() {
        List<BulletState> removedBullets = new ArrayList<>();
        List<EnemyState> removedEnemies = new ArrayList<>();

        for (BulletState bullet : bullets) {
            for (EnemyState enemy : enemies) {
                if (isColliding(bullet.x, bullet.y, 8f, enemy.x, enemy.y, 24f)) {
                    enemy.hp -= 22;
                    removedBullets.add(bullet);
                    if (enemy.hp <= 0) {
                        removedEnemies.add(enemy);
                        sharedScore += 1;
                    }
                    break;
                }
            }
        }

        for (EnemyState enemy : enemies) {
            for (PlayerState player : players) {
                if (player.hp <= 0) {
                    continue;
                }
                if (isColliding(enemy.x, enemy.y, 24f, player.x, player.y, 22f)) {
                    player.hp = Math.max(0, player.hp - 30);
                    removedEnemies.add(enemy);
                    break;
                }
            }
        }

        bullets.removeAll(removedBullets);
        enemies.removeAll(removedEnemies);
    }

    private void resolvePvpCollisionsLocked() {
        List<BulletState> removedBullets = new ArrayList<>();

        for (BulletState bullet : bullets) {
            int targetIndex = bullet.owner == 0 ? 1 : 0;
            PlayerState target = players[targetIndex];
            if (target.hp <= 0) {
                continue;
            }
            if (isColliding(bullet.x, bullet.y, 8f, target.x, target.y, 24f)) {
                target.hp = Math.max(0, target.hp - 12);
                removedBullets.add(bullet);
            }
        }

        bullets.removeAll(removedBullets);
    }

    private boolean isColliding(float x1, float y1, float r1, float x2, float y2, float r2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        float r = r1 + r2;
        return dx * dx + dy * dy <= r * r;
    }

    private void resolvePveResultLocked() {
        if (gameOver) {
            return;
        }
        if (sharedScore >= 25) {
            gameOver = true;
            resultCode = RESULT_PVE_WIN;
            sendHostStateLocked();
            return;
        }
        if (players[0].hp <= 0 && players[1].hp <= 0) {
            gameOver = true;
            resultCode = RESULT_PVE_FAIL;
            sendHostStateLocked();
        }
    }

    private void resolvePvpResultLocked() {
        if (gameOver) {
            return;
        }
        boolean p0Dead = players[0].hp <= 0;
        boolean p1Dead = players[1].hp <= 0;

        if (p0Dead && p1Dead) {
            gameOver = true;
            resultCode = RESULT_PVP_DRAW;
            sendHostStateLocked();
            return;
        }
        if (p0Dead) {
            gameOver = true;
            resultCode = RESULT_PVP_P1_WIN;
            sendHostStateLocked();
            return;
        }
        if (p1Dead) {
            gameOver = true;
            resultCode = RESULT_PVP_P0_WIN;
            sendHostStateLocked();
            return;
        }
        if (elapsedMs >= PVP_TIMEOUT_MS) {
            gameOver = true;
            if (players[0].hp == players[1].hp) {
                resultCode = RESULT_PVP_DRAW;
            } else {
                resultCode = players[0].hp > players[1].hp ? RESULT_PVP_P0_WIN : RESULT_PVP_P1_WIN;
            }
            sendHostStateLocked();
        }
    }

    private void sendHostStateLocked() {
        if (!host || !connected) {
            return;
        }
        JSONObject root = new JSONObject();
        try {
            root.put("type", TYPE_STATE);
            root.put("time", elapsedMs);
            root.put("score", sharedScore);
            root.put("gameOver", gameOver);
            root.put("result", resultCode);

            JSONArray playerArray = new JSONArray();
            playerArray.put(playerToJson(players[0]));
            playerArray.put(playerToJson(players[1]));
            root.put("players", playerArray);

            JSONArray bulletArray = new JSONArray();
            for (BulletState bullet : bullets) {
                JSONObject item = new JSONObject();
                item.put("x", bullet.x);
                item.put("y", bullet.y);
                item.put("vx", bullet.vx);
                item.put("vy", bullet.vy);
                item.put("owner", bullet.owner);
                bulletArray.put(item);
            }
            root.put("bullets", bulletArray);

            JSONArray enemyArray = new JSONArray();
            for (EnemyState enemy : enemies) {
                JSONObject item = new JSONObject();
                item.put("x", enemy.x);
                item.put("y", enemy.y);
                item.put("vx", enemy.vx);
                item.put("vy", enemy.vy);
                item.put("hp", enemy.hp);
                enemyArray.put(item);
            }
            root.put("enemies", enemyArray);
            session.send(root);
        } catch (JSONException ignored) {
        }
    }

    private JSONObject playerToJson(PlayerState player) throws JSONException {
        JSONObject item = new JSONObject();
        item.put("x", player.x);
        item.put("y", player.y);
        item.put("hp", player.hp);
        return item;
    }

    private void drawFrame() {
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            return;
        }
        try {
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);

            synchronized (stateLock) {
                if (!pve) {
                    float mid = worldHeight * 0.5f;
                    canvas.drawLine(0, mid, worldWidth, mid, subTextPaint);
                }

                for (EnemyState enemy : enemies) {
                    canvas.drawCircle(enemy.x, enemy.y, 24f, enemyPaint);
                }

                for (BulletState bullet : bullets) {
                    Paint paint = bullet.owner == 0 ? bulletP0Paint : bulletP1Paint;
                    canvas.drawCircle(bullet.x, bullet.y, 8f, paint);
                }

                drawPlayer(canvas, players[0], 24f, player0Paint);
                drawPlayer(canvas, players[1], 24f, player1Paint);

                canvas.drawText("P1 HP: " + players[0].hp, 20f, 42f, textPaint);
                canvas.drawText("P2 HP: " + players[1].hp, 20f, 74f, textPaint);
                if (pve) {
                    canvas.drawText("Score: " + sharedScore + " / 25", 20f, 106f, textPaint);
                } else {
                    canvas.drawText("Time: " + (elapsedMs / 1000) + "s", 20f, 106f, textPaint);
                }
            }

            if (!connected || disconnected) {
                drawOverlay(canvas, statusText.isEmpty() ? string(R.string.network_connecting) : statusText, string(R.string.network_retry));
            } else if (gameOver) {
                drawOverlay(canvas, getResultTextForLocalPlayer(), string(R.string.network_retry));
            }
        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawPlayer(Canvas canvas, PlayerState player, float radius, Paint activePaint) {
        Paint paint = player.hp > 0 ? activePaint : disabledPaint;
        canvas.drawCircle(player.x, player.y, radius, paint);
    }

    private void drawOverlay(Canvas canvas, String title, String subtitle) {
        canvas.drawRect(0, 0, worldWidth, worldHeight, overlayPaint);
        float centerX = worldWidth * 0.5f;
        float centerY = worldHeight * 0.5f;
        float titleWidth = textPaint.measureText(title);
        float subtitleWidth = subTextPaint.measureText(subtitle);
        canvas.drawText(title, centerX - titleWidth / 2f, centerY, textPaint);
        canvas.drawText(subtitle, centerX - subtitleWidth / 2f, centerY + 56f, subTextPaint);
    }

    private String getResultTextForLocalPlayer() {
        if (RESULT_PVE_WIN.equals(resultCode)) {
            return string(R.string.network_result_pve_success);
        }
        if (RESULT_PVE_FAIL.equals(resultCode)) {
            return string(R.string.network_result_pve_fail);
        }
        if (RESULT_PVP_DRAW.equals(resultCode)) {
            return string(R.string.network_result_draw);
        }
        if (RESULT_PVP_P0_WIN.equals(resultCode)) {
            return localPlayerIndex == 0 ? string(R.string.network_result_win) : string(R.string.network_result_lose);
        }
        if (RESULT_PVP_P1_WIN.equals(resultCode)) {
            return localPlayerIndex == 1 ? string(R.string.network_result_win) : string(R.string.network_result_lose);
        }
        return string(R.string.network_connecting);
    }

    private String string(int resId) {
        return getResources().getString(resId);
    }

    @Override
    protected void onDetachedFromWindow() {
        stopLoop();
        session.close();
        super.onDetachedFromWindow();
    }

    public void release() {
        stopLoop();
        session.close();
    }

    private static final class PlayerState {
        float x;
        float y;
        int hp;
    }

    private static final class BulletState {
        float x;
        float y;
        float vx;
        float vy;
        int owner;
    }

    private static final class EnemyState {
        float x;
        float y;
        float vx;
        float vy;
        int hp;
    }
}
