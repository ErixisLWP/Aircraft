package edu.hitsz.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.Enemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.application.core.CombatResolutionCore;
import edu.hitsz.application.core.EnemyBattleCore;
import edu.hitsz.prop.BaseProp;
import edu.hitsz.strategy.NormalShootStrategy;
import edu.hitsz.template.GameTemplateMethod;
import edu.hitsz.template.HardGameTemplateMethod;
import edu.hitsz.template.NormalGameTemplateMethod;
import edu.hitsz.template.SimpleGameTemplateMethod;

public class Game extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    public interface GameStateListener {
        void onGameOver();
    }

    public enum GameMode {SIMPLE, NORMAL, HARD}

    public static GameMode gameMode = GameMode.SIMPLE;

    private static int score = 0;

    private final SurfaceHolder surfaceHolder;
    private final Paint scorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint heroBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint enemyBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayBodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint buffPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Thread renderThread;
    private volatile boolean isDrawing = false;

    private int backGroundTop = 0;
    private final int timeInterval = 40;
    private int enemyMaxNumber = 5;
    private float eliteEnemyProbability = 0.2f;
    private float elitePlusEnemyProbability = 0.05f;
    private int bossThreshold = 1000;
    private int time = 0;
    private int cycleDuration = 600;
    private int cycleTime = 0;
    private int enemyCycleDuration = 600;
    private int heroAircraftCycleDuration = 600;
    private int enemyCycleTime = 0;
    private int heroAircraftCycleTime = 0;
    private int getHarderCycleDuration = 6000;
    private int getHarderCycleTime = 0;
    private boolean gameOverFlag = false;
    private boolean gameOverHandled = false;

    private final GameStateListener gameStateListener;

    private HeroAircraft heroAircraft;
    private final List<Enemy> enemyAircrafts = new LinkedList<>();
    private final List<BaseBullet> heroBullets = new LinkedList<>();
    private final List<BaseBullet> enemyBullets = new LinkedList<>();
    private final List<BaseProp> props = new LinkedList<>();

    private final CombatResolutionCore combatResolutionCore = new CombatResolutionCore();
    private final EnemyBattleCore enemyBattleCore = new EnemyBattleCore();
    public ObserverManager observerManager;
    GameTemplateMethod gameTemplateMethod;

    public Game(Context context, GameStateListener gameStateListener) {
        super(context);
        this.gameStateListener = gameStateListener;
        AudioManager.init(context);
        Main.initWindowSize(context);
        ImageManager.init(context, Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        setKeepScreenOn(true);
        initGameMode();
        initGameState();
        initPaints();
        setOnTouchListener((view, motionEvent) -> handleTouch(motionEvent));
    }

    public static void addScore(int increment) {
        score += increment;
    }

    public void setEnemyMaxNumber(int enemyMaxNumber) {
        this.enemyMaxNumber = enemyMaxNumber;
    }

    public void setHeroAircraftCycleDuration(int heroAircraftCycleDuration) {
        this.heroAircraftCycleDuration = heroAircraftCycleDuration;
    }

    public void setEnemyCycleDuration(int enemyCycleDuration) {
        this.enemyCycleDuration = enemyCycleDuration;
    }

    public void setEnemyProbability(float eliteEnemyProbability, float elitePlusEnemyProbability) {
        this.eliteEnemyProbability = eliteEnemyProbability;
        this.elitePlusEnemyProbability = elitePlusEnemyProbability;
    }

    public void setBossThreshold(int bossThreshold) {
        this.bossThreshold = bossThreshold;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (renderThread != null && renderThread.isAlive()) {
            return;
        }
        isDrawing = true;
        renderThread = new Thread(this, "aircraft-war-loop");
        renderThread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Main.setWindowSize(width, height);
        if (heroAircraft != null) {
            heroAircraft.setLocation(width / 2.0, height - ImageManager.HERO_IMAGE.getHeight());
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        isDrawing = false;
        AudioManager.stopAllSounds();
        if (renderThread == null) {
            return;
        }
        try {
            renderThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        while (isDrawing) {
            long frameStart = SystemClock.uptimeMillis();
            if (!gameOverFlag) {
                updateGame();
            }
            drawFrame();
            long frameCost = SystemClock.uptimeMillis() - frameStart;
            long sleepTime = Math.max(0, timeInterval - frameCost);
            SystemClock.sleep(sleepTime);
        }
    }

    private void initGameMode() {
        switch (gameMode) {
            case NORMAL:
                gameTemplateMethod = new NormalGameTemplateMethod(this);
                break;
            case HARD:
                gameTemplateMethod = new HardGameTemplateMethod(this);
                break;
            case SIMPLE:
            default:
                gameTemplateMethod = new SimpleGameTemplateMethod(this);
                break;
        }
        gameTemplateMethod.setGameDifficulty();
    }

    private void initGameState() {
        HeroAircraft.resetForNewGame();
        heroAircraft = HeroAircraft.getInstance();
        heroAircraft.setShootStrategy(new NormalShootStrategy());
        observerManager = new ObserverManager();
        enemyAircrafts.clear();
        heroBullets.clear();
        enemyBullets.clear();
        props.clear();
        score = 0;
        backGroundTop = 0;
        enemyBattleCore.reset();
        time = 0;
        cycleTime = 0;
        enemyCycleTime = 0;
        heroAircraftCycleTime = 0;
        getHarderCycleTime = 0;
        gameOverFlag = false;
        gameOverHandled = false;
        AudioManager.playBgm();
    }

    private void initPaints() {
        float density = getResources().getDisplayMetrics().density;
        scorePaint.setColor(Color.RED);
        scorePaint.setTextSize(22f * density);
        scorePaint.setFakeBoldText(true);

        heroBarPaint.setColor(Color.GREEN);
        enemyBarPaint.setColor(Color.RED);
        barBackgroundPaint.setColor(Color.DKGRAY);
        barBorderPaint.setColor(Color.WHITE);
        barBorderPaint.setStyle(Paint.Style.STROKE);
        barBorderPaint.setStrokeWidth(Math.max(2f, density));

        overlayPaint.setColor(Color.argb(180, 0, 0, 0));
        overlayTitlePaint.setColor(Color.WHITE);
        overlayTitlePaint.setTextSize(28f * density);
        overlayTitlePaint.setFakeBoldText(true);
        overlayBodyPaint.setColor(Color.WHITE);
        overlayBodyPaint.setTextSize(18f * density);

        buffPaint.setTextSize(14f * density);
        buffPaint.setFakeBoldText(true);
    }

    private boolean handleTouch(MotionEvent motionEvent) {
        if (motionEvent.getAction() != MotionEvent.ACTION_DOWN
                && motionEvent.getAction() != MotionEvent.ACTION_MOVE) {
            return true;
        }

        if (gameOverFlag) {
            return true;
        }

        int halfWidth = heroAircraft.getWidth() / 2;
        int halfHeight = heroAircraft.getHeight() / 2;
        float x = Math.max(halfWidth, Math.min(motionEvent.getX(), Main.WINDOW_WIDTH - halfWidth));
        float y = Math.max(halfHeight, Math.min(motionEvent.getY(), Main.WINDOW_HEIGHT - halfHeight));
        heroAircraft.setLocation(x, y);
        return true;
    }

    private void updateGame() {
        time += timeInterval;

        if (getHarderTimeCountAndNewCycleJudge()) {
            gameTemplateMethod.getHarderByTime(heroAircraft);
        }

        if (timeCountAndNewCycleJudge()) {
            checkPropDuration();
        }

        if (heroAircraftTimeCountAndNewCycleJudge()) {
            heroAircraftShootAction();
        }

        if (enemyTimeCountAndNewCycleJudge()) {
            createNewEnemy();
            enemyShootAction();
        }

        bulletsMoveAction();
        aircraftsMoveAction();
        propsMoveAction();
        crashCheckAction();
        postProcessAction();

        if (heroAircraft.getHp() <= 0) {
            gameOverFlag = true;
            AudioManager.playGameOverSound();
            notifyGameOverIfNeeded();
        }
    }

    private void notifyGameOverIfNeeded() {
        if (gameOverHandled || gameStateListener == null) {
            return;
        }
        gameOverHandled = true;
        postDelayed(gameStateListener::onGameOver, 1400);
    }
    private void createNewEnemy() {
        EnemyBattleCore.SpawnResult spawnResult = enemyBattleCore.spawnEnemies(
                enemyAircrafts,
                score,
                enemyMaxNumber,
                eliteEnemyProbability,
                elitePlusEnemyProbability,
                bossThreshold
        );
        for (Enemy enemy : spawnResult.getSpawnedEnemies()) {
            observerManager.registerAircraftObserver(enemy);
        }
        if (spawnResult.isBossSpawned()) {
            AudioManager.playBossBgm();
        }
    }

    private boolean hasActiveBoss() {
        for (Enemy enemyAircraft : enemyAircrafts) {
            if (enemyAircraft instanceof BossEnemy && !enemyAircraft.notValid()) {
                return true;
            }
        }
        return false;
    }
    private boolean getHarderTimeCountAndNewCycleJudge() {
        getHarderCycleTime += timeInterval;
        if (getHarderCycleTime >= getHarderCycleDuration) {
            getHarderCycleTime %= getHarderCycleDuration;
            return true;
        }
        return false;
    }

    private boolean timeCountAndNewCycleJudge() {
        cycleTime += timeInterval;
        if (cycleTime >= cycleDuration) {
            cycleTime %= cycleDuration;
            return true;
        }
        return false;
    }

    private boolean heroAircraftTimeCountAndNewCycleJudge() {
        heroAircraftCycleTime += timeInterval;
        if (heroAircraftCycleTime >= heroAircraftCycleDuration) {
            heroAircraftCycleTime %= heroAircraftCycleDuration;
            return true;
        }
        return false;
    }

    private boolean enemyTimeCountAndNewCycleJudge() {
        enemyCycleTime += timeInterval;
        if (enemyCycleTime >= enemyCycleDuration) {
            enemyCycleTime %= enemyCycleDuration;
            return true;
        }
        return false;
    }

    private void enemyShootAction() {
        int beforeSize = enemyBullets.size();
        enemyBattleCore.appendEnemyBullets(enemyAircrafts, enemyBullets);
        if (enemyBullets.size() > beforeSize) {
            observerManager.registerBulletObserver(new ArrayList<>(enemyBullets.subList(beforeSize, enemyBullets.size())));
        }
    }

    private void heroAircraftShootAction() {
        heroBullets.addAll(heroAircraft.shoot());
        AudioManager.playShootSound();
    }

    private void checkPropDuration() {
        if (heroAircraft.getPropEffectiveEndTime() < System.currentTimeMillis()) {
            heroAircraft.setShootStrategy(new NormalShootStrategy());
        }
    }

    private void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    private void aircraftsMoveAction() {
        for (Enemy enemyAircraft : enemyAircrafts) {
            enemyAircraft.forward();
        }
    }

    private void propsMoveAction() {
        for (BaseProp prop : props) {
            prop.forward();
        }
    }

    private void crashCheckAction() {
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) {
                continue;
            }
            if (heroAircraft.crash(bullet)) {
                AudioManager.playBulletHitSound();
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
            }
        }

        CombatResolutionCore.BulletEnemyCollisionResult bulletEnemyResult =
                combatResolutionCore.collidePlayerBulletsWithEnemies(
                        heroBullets,
                        enemyAircrafts,
                        enemy -> enemy.dropProp(props, enemy.getLocationX(), enemy.getLocationY())
                );
        if (bulletEnemyResult.getHitCount() > 0) {
            AudioManager.playBulletHitSound();
        }
        score += bulletEnemyResult.getScoreGain();

        for (Enemy enemyAircraft : enemyAircrafts) {
            if (enemyAircraft.notValid()) {
                continue;
            }
            if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                enemyAircraft.vanish();
                heroAircraft.decreaseHp(Integer.MAX_VALUE);
            }
        }

        for (BaseProp prop : props) {
            if (prop.crash(heroAircraft) || heroAircraft.crash(prop)) {
                observerManager.notifyAllObservers(prop);
                prop.takeEffect(heroAircraft);
            }
        }
    }

    private void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        props.removeIf(AbstractFlyingObject::notValid);
        observerManager.clear();
    }

    private void drawFrame() {
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas == null) {
            return;
        }
        try {
            drawBackground(canvas);
            paintImageWithPositionRevised(canvas, props);
            paintImageWithPositionRevised(canvas, enemyBullets);
            paintImageWithPositionRevised(canvas, heroBullets);
            paintImageWithPositionRevised(canvas, enemyAircrafts);
            drawHero(canvas);
            drawHealthBarsForHeroAircraft(canvas);
            drawHealthBarsForEnemies(canvas);
            paintScoreAndLife(canvas);
            paintBuffTimerBar(canvas);
            if (gameOverFlag) {
                drawGameOverOverlay(canvas);
            }
        } finally {
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawBackground(Canvas canvas) {
        Bitmap image = gameMode == GameMode.SIMPLE
                ? ImageManager.BACKGROUND_IMAGE_SIMPLE
                : gameMode == GameMode.NORMAL
                ? ImageManager.BACKGROUND_IMAGE_NORMAL
                : ImageManager.BACKGROUND_IMAGE_HARD;
        canvas.drawBitmap(image, 0, backGroundTop - Main.WINDOW_HEIGHT, null);
        canvas.drawBitmap(image, 0, backGroundTop, null);
        backGroundTop += 1;
        if (backGroundTop >= Main.WINDOW_HEIGHT) {
            backGroundTop = 0;
        }
    }

    private void drawHero(Canvas canvas) {
        Bitmap heroImage = ImageManager.HERO_IMAGE;
        canvas.drawBitmap(
                heroImage,
                heroAircraft.getLocationX() - heroImage.getWidth() / 2f,
                heroAircraft.getLocationY() - heroImage.getHeight() / 2f,
                null
        );
    }

    private void drawHealthBarsForHeroAircraft(Canvas canvas) {
        drawHealthBar(canvas, heroAircraft, heroBarPaint);
    }

    private void drawHealthBarsForEnemies(Canvas canvas) {
        for (Enemy enemy : enemyAircrafts) {
            if (!enemy.notValid()) {
                drawHealthBar(canvas, enemy, enemyBarPaint);
            }
        }
    }

    private void drawHealthBar(Canvas canvas, AbstractAircraft aircraft, Paint foregroundPaint) {
        int hp = aircraft.getHp();
        int maxHp = aircraft.getMaxHp();
        if (hp <= 0 || hp == maxHp) {
            return;
        }

        int barWidth = aircraft.getWidth();
        int barHeight = Math.max(6, Math.round(getResources().getDisplayMetrics().density * 4));
        int barX = aircraft.getLocationX() - barWidth / 2;
        int barY = aircraft.getLocationY() + aircraft.getHeight() / 2 + barHeight + 5;
        float percentage = hp / (float) maxHp;

        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, barBackgroundPaint);
        canvas.drawRect(barX, barY, barX + (barWidth * percentage), barY + barHeight, foregroundPaint);
    }

    private void paintImageWithPositionRevised(Canvas canvas, List<? extends AbstractFlyingObject> objects) {
        for (AbstractFlyingObject object : objects) {
            Bitmap image = object.getImage();
            if (image == null) {
                continue;
            }
            canvas.drawBitmap(
                    image,
                    object.getLocationX() - image.getWidth() / 2f,
                    object.getLocationY() - image.getHeight() / 2f,
                    null
            );
        }
    }

    private void paintScoreAndLife(Canvas canvas) {
        canvas.drawText("SCORE:" + score, 16, 40, scorePaint);
        canvas.drawText("LIFE:" + heroAircraft.getHp(), 16, 78, scorePaint);
    }

    private void paintBuffTimerBar(Canvas canvas) {
        long endTime = heroAircraft.getPropEffectiveEndTime();
        long currentTime = System.currentTimeMillis();
        if (endTime <= currentTime) {
            return;
        }

        long totalDuration = heroAircraft.getPropDuration();
        long remainingTime = endTime - currentTime;
        float percentage = remainingTime / (float) totalDuration;

        int barWidth = Math.min(300, Main.WINDOW_WIDTH / 2);
        int barHeight = Math.max(18, Math.round(getResources().getDisplayMetrics().density * 12));
        int x = (Main.WINDOW_WIDTH - barWidth) / 2;
        int y = Main.WINDOW_HEIGHT - 80;

        buffPaint.setColor(Color.GRAY);
        canvas.drawRect(x, y, x + barWidth, y + barHeight, buffPaint);
        buffPaint.setColor(Color.CYAN);
        canvas.drawRect(x, y, x + barWidth * percentage, y + barHeight, buffPaint);
        canvas.drawRect(x, y, x + barWidth, y + barHeight, barBorderPaint);
        buffPaint.setColor(Color.BLACK);
        canvas.drawText("BUFF", x + barWidth / 2f - 20, y + barHeight - 4, buffPaint);
    }

    private void drawGameOverOverlay(Canvas canvas) {
        canvas.drawRect(0, 0, Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT, overlayPaint);
        float centerX = Main.WINDOW_WIDTH / 2f;
        float centerY = Main.WINDOW_HEIGHT / 2f;
        String scoreText = "Score: " + score;
        canvas.drawText("Game Over", centerX - overlayTitlePaint.measureText("Game Over") / 2f, centerY, overlayTitlePaint);
        canvas.drawText(scoreText, centerX - overlayBodyPaint.measureText(scoreText) / 2f, centerY + 48, overlayBodyPaint);
        canvas.drawText("Tap to restart", centerX - overlayBodyPaint.measureText("Tap to restart") / 2f, centerY + 96, overlayBodyPaint);
    }
}
