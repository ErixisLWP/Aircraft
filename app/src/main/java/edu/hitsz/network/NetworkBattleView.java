package edu.hitsz.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
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
import edu.hitsz.R;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

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
    private static final long STATE_PUSH_MS = 40L;
    private static final long INPUT_PUSH_MS = 50L;

    private static final long PLAYER_SHOOT_INTERVAL_MS = 420L;
    private static final long PVP_TIMEOUT_MS = 90_000L;

    private static final int ENEMY_KIND_MOB = 0;
    private static final int ENEMY_KIND_ELITE = 1;
    private static final int ENEMY_KIND_ELITE_PLUS = 3;
    private static final int ENEMY_KIND_BOSS = 2;

    private static final int PLAYER_MAX_HP = 100;

    private final NetworkBattleConfig config;
    private final ExitListener exitListener;
    private final SurfaceHolder holder;
    private final NetworkPeerSession session;
    private final NetworkHostBattleController hostController;

    private final Object stateLock = new Object();

    private final Paint scorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hudPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint heroBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint enemyBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayBodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint splitLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint playerLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final PlayerState[] players = new PlayerState[] {new PlayerState(), new PlayerState()};
    private final List<BulletState> playerBullets = new ArrayList<>();
    private final List<BulletState> enemyBullets = new ArrayList<>();
    private final List<EnemyState> enemies = new ArrayList<>();

    private Thread renderThread;
    private volatile boolean drawing = false;

    private int worldWidth;
    private int worldHeight;
    private boolean worldReady = false;
    private int backGroundTop = 0;

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

    private Bitmap cachedHeroSource;
    private Bitmap cachedHeroFlipped;

    private long elapsedMs = 0L;
    private long nextStatePushAt = 0L;

    private int sharedScore = 0;
    private boolean bossSpawned = false;
    private boolean bossDefeated = false;

    public NetworkBattleView(Context context, NetworkBattleConfig config, ExitListener exitListener) {
        super(context);
        this.config = config;
        this.exitListener = exitListener;
        this.host = config.isHost();
        this.pve = config.getMode() == NetworkBattleConfig.Mode.PVE;
        this.hostController = new NetworkHostBattleController(this.pve);
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
                nextInputPushAt = 0L;
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
                    if (statusText == null || statusText.trim().isEmpty()) {
                        statusText = string(R.string.network_disconnect);
                    }
                }
            }
        });
        session.start();
    }

    private void initPaints() {
        float density = getResources().getDisplayMetrics().density;

        scorePaint.setColor(Color.RED);
        scorePaint.setTextSize(21f * density);
        scorePaint.setFakeBoldText(true);

        hudPaint.setColor(Color.WHITE);
        hudPaint.setTextSize(16f * density);

        heroBarPaint.setColor(Color.GREEN);
        enemyBarPaint.setColor(Color.RED);

        barBgPaint.setColor(Color.DKGRAY);
        barBorderPaint.setColor(Color.WHITE);
        barBorderPaint.setStyle(Paint.Style.STROKE);
        barBorderPaint.setStrokeWidth(Math.max(2f, density));

        overlayPaint.setColor(Color.argb(180, 0, 0, 0));
        overlayTitlePaint.setColor(Color.WHITE);
        overlayTitlePaint.setTextSize(28f * density);
        overlayTitlePaint.setFakeBoldText(true);
        overlayBodyPaint.setColor(Color.WHITE);
        overlayBodyPaint.setTextSize(18f * density);

        splitLinePaint.setColor(Color.argb(170, 255, 255, 255));
        splitLinePaint.setStyle(Paint.Style.STROKE);
        splitLinePaint.setStrokeWidth(Math.max(2f, density));
        splitLinePaint.setPathEffect(new DashPathEffect(new float[] {12f, 12f}, 0f));

        playerLabelPaint.setColor(Color.WHITE);
        playerLabelPaint.setTextSize(14f * density);
        playerLabelPaint.setFakeBoldText(true);
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
            Main.setWindowSize(width, height);
            ImageManager.init(getContext(), width, height);
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
                maybeSendClientInput();
                synchronized (stateLock) {
                    players[localPlayerIndex].x = clamp(localInputX, heroHalfWidth(), worldWidth - heroHalfWidth());
                    players[localPlayerIndex].y = clampYForPlayer(localPlayerIndex, localInputY);
                }
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

        players[0].hp = PLAYER_MAX_HP;
        players[1].hp = PLAYER_MAX_HP;

        localInputX = players[localPlayerIndex].x;
        localInputY = players[localPlayerIndex].y;
        remoteInputX = players[remotePlayerIndex].x;
        remoteInputY = players[remotePlayerIndex].y;

        inputDirty = true;
        lastSentInputX = Float.NaN;
        lastSentInputY = Float.NaN;
        nextInputPushAt = 0L;

        playerBullets.clear();
        enemyBullets.clear();
        enemies.clear();
        hostController.reset();

        elapsedMs = 0L;
        nextStatePushAt = STATE_PUSH_MS;

        sharedScore = 0;
        backGroundTop = 0;
        bossSpawned = false;
        bossDefeated = false;
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

        if (!worldReady || gameOver || disconnected) {
            return true;
        }

        float x = clamp(event.getX(), heroHalfWidth(), worldWidth - heroHalfWidth());
        float y = clampYForPlayer(localPlayerIndex, event.getY());

        localInputX = x;
        localInputY = y;
        inputDirty = true;
        nextInputPushAt = 0L;

        // Client-side prediction: move local plane immediately instead of waiting for host snapshot.
        if (!host) {
            synchronized (stateLock) {
                players[localPlayerIndex].x = x;
                players[localPlayerIndex].y = y;
            }
        }
        return true;
    }

    private float clampYForPlayer(int playerIndex, float y) {
        float half = heroHalfHeight();
        float minY = half;
        float maxY = worldHeight - half;
        if (!pve) {
            if (playerIndex == 0) {
                minY = worldHeight * 0.5f + half;
            } else {
                maxY = worldHeight * 0.5f - half;
            }
        } else {
            minY = worldHeight * 0.42f;
        }
        return clamp(y, minY, maxY);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void maybeSendClientInput() {
        if (!connected || host || !worldReady || disconnected) {
            return;
        }

        long now = SystemClock.uptimeMillis();
        if (now < nextInputPushAt) {
            return;
        }

        float x = localInputX;
        float y = localInputY;
        boolean moved = Float.isNaN(lastSentInputX)
                || Math.abs(lastSentInputX - x) > 0.5f
                || Math.abs(lastSentInputY - y) > 0.5f;

        if (!inputDirty && !moved) {
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
            if (!TYPE_INPUT.equals(message.optString("type", ""))) {
                return;
            }
            remoteInputX = (float) message.optDouble("x", remoteInputX);
            remoteInputY = (float) message.optDouble("y", remoteInputY);
            return;
        }

        if (!TYPE_STATE.equals(message.optString("type", ""))) {
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
            bossSpawned = stateMessage.optBoolean("bossSpawned", bossSpawned);
            bossDefeated = stateMessage.optBoolean("bossDefeated", bossDefeated);

            JSONArray playerArray = stateMessage.optJSONArray("players");
            if (playerArray != null && playerArray.length() >= 2) {
                NetworkBattleStateMapper.applyPlayer(playerArray.optJSONObject(0), players[0]);
                NetworkBattleStateMapper.applyPlayer(playerArray.optJSONObject(1), players[1]);
            }

            playerBullets.clear();
            playerBullets.addAll(NetworkBattleStateMapper.parseBulletArray(stateMessage.optJSONArray("playerBullets"), 0));

            enemyBullets.clear();
            enemyBullets.addAll(NetworkBattleStateMapper.parseBulletArray(stateMessage.optJSONArray("enemyBullets"), -1));

            enemies.clear();
            enemies.addAll(NetworkBattleStateMapper.parseEnemyArray(
                    stateMessage.optJSONArray("enemies"),
                    ENEMY_KIND_MOB,
                    ENEMY_KIND_BOSS
            ));
        }
    }

    private void updateHostGame() {
        synchronized (stateLock) {
            hostController.tick(
                    players,
                    localPlayerIndex,
                    remotePlayerIndex,
                    localInputX,
                    localInputY,
                    remoteInputX,
                    remoteInputY,
                    worldWidth,
                    worldHeight,
                    heroHalfWidth(),
                    heroHalfHeight(),
                    bulletRadius(),
                    heroRadius()
            );

            elapsedMs = hostController.getElapsedMs();
            sharedScore = hostController.getSharedScore();
            bossSpawned = hostController.isBossSpawned();
            bossDefeated = hostController.isBossDefeated();
            hostController.fillSnapshot(playerBullets, enemyBullets, enemies);

            if (pve) {
                resolvePveResultLocked();
            } else {
                resolvePvpResultLocked();
            }

            if (elapsedMs >= nextStatePushAt) {
                sendHostStateLocked();
                nextStatePushAt += STATE_PUSH_MS;
            }
        }
    }

    private void resolvePveResultLocked() {
        if (gameOver) {
            return;
        }

        if (bossDefeated) {
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
            root.put("bossSpawned", bossSpawned);
            root.put("bossDefeated", bossDefeated);

            root.put("players", NetworkBattleStateMapper.toPlayerArray(players));
            root.put("playerBullets", NetworkBattleStateMapper.toBulletArray(playerBullets, true));
            root.put("enemyBullets", NetworkBattleStateMapper.toBulletArray(enemyBullets, false));
            root.put("enemies", NetworkBattleStateMapper.toEnemyArray(enemies));
            session.send(root);
        } catch (JSONException ignored) {
        }
    }

    private void drawFrame() {
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            return;
        }

        try {
            drawBackground(canvas);

            synchronized (stateLock) {
                if (!pve) {
                    float mid = worldHeight * 0.5f;
                    canvas.drawLine(0, mid, worldWidth, mid, splitLinePaint);
                }

                drawEnemies(canvas);
                drawBullets(canvas, enemyBullets, ImageManager.ENEMY_BULLET_IMAGE);
                drawBullets(canvas, playerBullets, ImageManager.HERO_BULLET_IMAGE);
                drawPlayers(canvas);
                drawHealthBars(canvas);
                drawHud(canvas);
            }

            if (!connected || disconnected) {
                String title = statusText == null || statusText.trim().isEmpty()
                        ? string(R.string.network_connecting)
                        : statusText;
                drawOverlay(canvas, title, string(R.string.network_retry));
            } else if (gameOver) {
                drawOverlay(canvas, getResultTextForLocalPlayer(), string(R.string.network_retry));
            }
        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawBackground(Canvas canvas) {
        Bitmap background = pve ? ImageManager.BACKGROUND_IMAGE_NORMAL : ImageManager.BACKGROUND_IMAGE_HARD;
        if (background == null) {
            canvas.drawColor(Color.BLACK);
            return;
        }

        canvas.drawBitmap(background, 0, backGroundTop - worldHeight, null);
        canvas.drawBitmap(background, 0, backGroundTop, null);

        backGroundTop += 2;
        if (backGroundTop >= worldHeight) {
            backGroundTop = 0;
        }
    }

    private void drawEnemies(Canvas canvas) {
        for (EnemyState enemy : enemies) {
            Bitmap bitmap = enemyBitmap(enemy);
            if (bitmap == null) {
                continue;
            }
            canvas.drawBitmap(bitmap, enemy.x - bitmap.getWidth() / 2f, enemy.y - bitmap.getHeight() / 2f, null);
        }
    }

    private void drawBullets(Canvas canvas, List<BulletState> bullets, Bitmap bulletBitmap) {
        if (bulletBitmap == null) {
            return;
        }
        for (BulletState bullet : bullets) {
            canvas.drawBitmap(
                    bulletBitmap,
                    bullet.x - bulletBitmap.getWidth() / 2f,
                    bullet.y - bulletBitmap.getHeight() / 2f,
                    null
            );
        }
    }

    private void drawPlayers(Canvas canvas) {
        Bitmap hero = ImageManager.HERO_IMAGE;
        if (hero == null) {
            return;
        }

        for (int i = 0; i < players.length; i++) {
            PlayerState player = players[i];
            Paint paint = null;
            if (player.hp <= 0) {
                paint = new Paint();
                paint.setAlpha(110);
            }

            float left = player.x - hero.getWidth() / 2f;
            float top = player.y - hero.getHeight() / 2f;
            boolean faceDown = !pve && player.y < worldHeight * 0.5f;
            Bitmap renderHero = faceDown ? getFlippedHero(hero) : hero;
            canvas.drawBitmap(renderHero, left, top, paint);

            String label = i == 0 ? "P1" : "P2";
            canvas.drawText(label, player.x - playerLabelPaint.measureText(label) / 2f, player.y - hero.getHeight() / 2f - 8f, playerLabelPaint);
        }
    }

    private Bitmap getFlippedHero(Bitmap hero) {
        if (hero == null) {
            return null;
        }
        if (cachedHeroSource == hero && cachedHeroFlipped != null
                && !cachedHeroFlipped.isRecycled()
                && cachedHeroFlipped.getWidth() == hero.getWidth()
                && cachedHeroFlipped.getHeight() == hero.getHeight()) {
            return cachedHeroFlipped;
        }

        Matrix matrix = new Matrix();
        matrix.preScale(1f, -1f);
        cachedHeroFlipped = Bitmap.createBitmap(hero, 0, 0, hero.getWidth(), hero.getHeight(), matrix, false);
        cachedHeroSource = hero;
        return cachedHeroFlipped;
    }

    private void drawHealthBars(Canvas canvas) {
        Bitmap hero = ImageManager.HERO_IMAGE;
        int heroBarWidth = hero == null ? 50 : hero.getWidth();
        int barHeight = Math.max(6, Math.round(getResources().getDisplayMetrics().density * 4));

        for (PlayerState player : players) {
            drawHealthBar(canvas, player.x - heroBarWidth / 2f, player.y + heroHalfHeight() + 8f,
                    heroBarWidth, barHeight, player.hp, PLAYER_MAX_HP, heroBarPaint);
        }

        for (EnemyState enemy : enemies) {
            int enemyBarWidth;
            if (enemy.boss || enemy.kind == ENEMY_KIND_BOSS) {
                enemyBarWidth = ImageManager.BOSS_ENEMY_IMAGE == null ? 80 : ImageManager.BOSS_ENEMY_IMAGE.getWidth();
            } else if (enemy.kind == ENEMY_KIND_ELITE_PLUS) {
                enemyBarWidth = ImageManager.ELITEPLUS_ENEMY_IMAGE == null ? 56 : ImageManager.ELITEPLUS_ENEMY_IMAGE.getWidth();
            } else if (enemy.kind == ENEMY_KIND_ELITE) {
                enemyBarWidth = ImageManager.ELITE_ENEMY_IMAGE == null ? 56 : ImageManager.ELITE_ENEMY_IMAGE.getWidth();
            } else {
                enemyBarWidth = ImageManager.MOB_ENEMY_IMAGE == null ? 56 : ImageManager.MOB_ENEMY_IMAGE.getWidth();
            }
            drawHealthBar(canvas, enemy.x - enemyBarWidth / 2f, enemy.y + enemyHalfHeight(enemy) + 6f,
                    enemyBarWidth, barHeight, enemy.hp, enemy.maxHp, enemyBarPaint);
        }
    }

    private void drawHealthBar(Canvas canvas,
                               float x,
                               float y,
                               int width,
                               int height,
                               int hp,
                               int maxHp,
                               Paint foreground) {
        if (maxHp <= 0) {
            return;
        }

        float ratio = Math.max(0f, Math.min(1f, hp / (float) maxHp));
        canvas.drawRect(x, y, x + width, y + height, barBgPaint);
        canvas.drawRect(x, y, x + width * ratio, y + height, foreground);
        canvas.drawRect(x, y, x + width, y + height, barBorderPaint);
    }

    private void drawHud(Canvas canvas) {
        canvas.drawText("SCORE: " + sharedScore, 16f, 40f, scorePaint);
        canvas.drawText("P1 HP: " + players[0].hp + "   P2 HP: " + players[1].hp, 16f, 72f, scorePaint);

        if (pve) {
            String target = bossDefeated ? "Boss defeated" : (bossSpawned ? "Boss battle" : "Clear mobs to summon Boss");
            canvas.drawText(target, 16f, 102f, hudPaint);
        } else {
            long remain = Math.max(0L, (PVP_TIMEOUT_MS - elapsedMs) / 1000L);
            canvas.drawText("PVP Time: " + remain + "s", 16f, 102f, hudPaint);
        }
    }

    private void drawOverlay(Canvas canvas, String title, String subtitle) {
        canvas.drawRect(0, 0, worldWidth, worldHeight, overlayPaint);
        float centerX = worldWidth / 2f;
        float centerY = worldHeight / 2f;

        float titleWidth = overlayTitlePaint.measureText(title);
        canvas.drawText(title, centerX - titleWidth / 2f, centerY, overlayTitlePaint);

        float subWidth = overlayBodyPaint.measureText(subtitle);
        canvas.drawText(subtitle, centerX - subWidth / 2f, centerY + 56f, overlayBodyPaint);
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

    private boolean isPlayerFacingUp(int playerIndex) {
        return pve || playerIndex == 0;
    }

    private float heroHalfWidth() {
        Bitmap hero = ImageManager.HERO_IMAGE;
        return hero == null ? 24f : hero.getWidth() / 2f;
    }

    private float heroHalfHeight() {
        Bitmap hero = ImageManager.HERO_IMAGE;
        return hero == null ? 24f : hero.getHeight() / 2f;
    }

    private float heroRadius() {
        return Math.min(heroHalfWidth(), heroHalfHeight()) * 0.8f;
    }

    private float bulletRadius() {
        Bitmap bullet = ImageManager.HERO_BULLET_IMAGE;
        return bullet == null ? 8f : Math.max(4f, Math.min(bullet.getWidth(), bullet.getHeight()) * 0.4f);
    }

    private float enemyHalfWidth(EnemyState enemy) {
        Bitmap bitmap = enemyBitmap(enemy);
        return bitmap == null ? 24f : bitmap.getWidth() / 2f;
    }

    private float enemyHalfHeight(EnemyState enemy) {
        Bitmap bitmap = enemyBitmap(enemy);
        return bitmap == null ? 24f : bitmap.getHeight() / 2f;
    }

    private Bitmap enemyBitmap(EnemyState enemy) {
        return enemyBitmapByKind(enemy.kind, enemy.boss);
    }

    private Bitmap enemyBitmapByKind(int kind, boolean boss) {
        if (kind == ENEMY_KIND_BOSS || boss) {
            return ImageManager.BOSS_ENEMY_IMAGE;
        }
        if (kind == ENEMY_KIND_ELITE_PLUS) {
            return ImageManager.ELITEPLUS_ENEMY_IMAGE;
        }
        if (kind == ENEMY_KIND_ELITE) {
            return ImageManager.ELITE_ENEMY_IMAGE;
        }
        return ImageManager.MOB_ENEMY_IMAGE;
    }

    private float enemyRadius(EnemyState enemy) {
        return Math.min(enemyHalfWidth(enemy), enemyHalfHeight(enemy)) * (enemy.boss ? 0.78f : 0.72f);
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

}
