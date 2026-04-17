package edu.hitsz.network;

import java.util.ArrayList;
import java.util.List;

import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.aircraft.Enemy;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.application.core.CombatResolutionCore;
import edu.hitsz.application.core.EnemyBattleCore;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.HeroBullet;

final class NetworkHostBattleController {

    private static final long FRAME_MS = 40L;

    private static final long PLAYER_SHOOT_INTERVAL_MS = 420L;
    private static final long ENEMY_SPAWN_INTERVAL_MS = 900L;
    private static final long ENEMY_SHOOT_INTERVAL_MS = 850L;

    private static final float ELITE_ENEMY_PROBABILITY = 0.2f;
    private static final float ELITE_PLUS_ENEMY_PROBABILITY = 0.05f;

    private static final int ENEMY_KIND_MOB = 0;
    private static final int ENEMY_KIND_ELITE = 1;
    private static final int ENEMY_KIND_ELITE_PLUS = 3;
    private static final int ENEMY_KIND_BOSS = 2;

    private static final int PLAYER_BULLET_DAMAGE_PVE = 50;
    private static final int PLAYER_BULLET_DAMAGE_PVP = 15;
    private static final int ENEMY_COLLISION_DAMAGE = 22;
    private static final int ENEMY_ESCAPE_DAMAGE = 8;

    private static final int PVE_MAX_ENEMIES = 6;
    private static final int BOSS_SCORE_THRESHOLD = 24;

    private final boolean pve;
    private final CombatResolutionCore combatResolutionCore = new CombatResolutionCore();
    private final EnemyBattleCore enemyBattleCore = new EnemyBattleCore();

    private final List<BaseBullet> playerBulletEntities = new ArrayList<>();
    private final List<Enemy> enemyEntities = new ArrayList<>();
    private final List<BaseBullet> enemyBulletEntities = new ArrayList<>();

    private long elapsedMs = 0L;
    private long nextShootAt = 0L;
    private long nextEnemySpawnAt = 0L;
    private long nextEnemyShootAt = 0L;

    private int sharedScore = 0;
    private boolean bossSpawned = false;
    private boolean bossDefeated = false;

    NetworkHostBattleController(boolean pve) {
        this.pve = pve;
    }

    void reset() {
        playerBulletEntities.clear();
        enemyEntities.clear();
        enemyBulletEntities.clear();

        elapsedMs = 0L;
        nextShootAt = PLAYER_SHOOT_INTERVAL_MS;
        nextEnemySpawnAt = ENEMY_SPAWN_INTERVAL_MS;
        nextEnemyShootAt = ENEMY_SHOOT_INTERVAL_MS;

        sharedScore = 0;
        bossSpawned = false;
        bossDefeated = false;
        enemyBattleCore.reset();
    }

    void tick(PlayerState[] players,
              int localPlayerIndex,
              int remotePlayerIndex,
              float localInputX,
              float localInputY,
              float remoteInputX,
              float remoteInputY,
              int worldWidth,
              int worldHeight,
              float heroHalfWidth,
              float heroHalfHeight,
              float bulletRadius,
              float heroRadius) {
        if (players == null || players.length < 2) {
            return;
        }

        elapsedMs += FRAME_MS;

        players[localPlayerIndex].x = clamp(localInputX, heroHalfWidth, worldWidth - heroHalfWidth);
        players[localPlayerIndex].y = clampYForPlayer(localPlayerIndex, localInputY, heroHalfHeight, worldHeight);
        players[remotePlayerIndex].x = clamp(remoteInputX, heroHalfWidth, worldWidth - heroHalfWidth);
        players[remotePlayerIndex].y = clampYForPlayer(remotePlayerIndex, remoteInputY, heroHalfHeight, worldHeight);

        if (elapsedMs >= nextShootAt) {
            spawnPlayerBullets(players, heroHalfHeight);
            nextShootAt += PLAYER_SHOOT_INTERVAL_MS;
        }

        if (pve && elapsedMs >= nextEnemySpawnAt) {
            EnemyBattleCore.SpawnResult spawnResult = enemyBattleCore.spawnEnemies(
                    enemyEntities,
                    sharedScore,
                    PVE_MAX_ENEMIES,
                    ELITE_ENEMY_PROBABILITY,
                    ELITE_PLUS_ENEMY_PROBABILITY,
                    BOSS_SCORE_THRESHOLD
            );
            if (spawnResult.isBossSpawned()) {
                bossSpawned = true;
            }
            nextEnemySpawnAt += ENEMY_SPAWN_INTERVAL_MS;
        }

        if (pve && elapsedMs >= nextEnemyShootAt) {
            enemyBattleCore.appendEnemyBullets(enemyEntities, enemyBulletEntities);
            nextEnemyShootAt += ENEMY_SHOOT_INTERVAL_MS;
        }

        movePlayerBullets();
        moveEnemyBullets();
        moveEnemies(players);

        if (pve) {
            resolvePveCollisions(players, bulletRadius, heroRadius);
        } else {
            resolvePvpCollisions(players, bulletRadius, heroRadius);
        }
    }

    private void spawnPlayerBullets(PlayerState[] players, float heroHalfHeight) {
        for (int i = 0; i < players.length; i++) {
            if (players[i].hp <= 0) {
                continue;
            }
            int bulletSpeedY = isPlayerFacingUp(i) ? -18 : 18;
            int bulletY = (int) (players[i].y + (isPlayerFacingUp(i) ? -heroHalfHeight : heroHalfHeight));
            BaseBullet bullet = new HeroBullet(
                    (int) players[i].x,
                    bulletY,
                    0,
                    bulletSpeedY,
                    pve ? PLAYER_BULLET_DAMAGE_PVE : PLAYER_BULLET_DAMAGE_PVP
            );
            playerBulletEntities.add(bullet);
        }
    }

    private void movePlayerBullets() {
        for (BaseBullet bullet : playerBulletEntities) {
            bullet.forward();
        }
        playerBulletEntities.removeIf(BaseBullet::notValid);
    }

    private void moveEnemyBullets() {
        for (BaseBullet bullet : enemyBulletEntities) {
            bullet.forward();
        }
        enemyBulletEntities.removeIf(BaseBullet::notValid);
    }

    private void moveEnemies(PlayerState[] players) {
        for (Enemy enemy : enemyEntities) {
            int previousHp = enemy.getHp();
            enemy.forward();
            if (!(enemy instanceof BossEnemy) && previousHp > 0 && enemy.notValid()) {
                players[0].hp = Math.max(0, players[0].hp - ENEMY_ESCAPE_DAMAGE);
                players[1].hp = Math.max(0, players[1].hp - ENEMY_ESCAPE_DAMAGE);
            }
        }
        enemyEntities.removeIf(Enemy::notValid);
    }

    private void resolvePveCollisions(PlayerState[] players, float bulletRadius, float heroRadius) {
        List<BaseBullet> consumedEnemyBullets = new ArrayList<>();

        CombatResolutionCore.BulletEnemyCollisionResult bulletEnemyResult =
                combatResolutionCore.collidePlayerBulletsWithEnemies(
                        playerBulletEntities,
                        enemyEntities,
                        enemy -> {
                            if (enemy instanceof BossEnemy) {
                                bossDefeated = true;
                            }
                        }
                );
        sharedScore += bulletEnemyResult.getScoreGain();

        for (BaseBullet bullet : enemyBulletEntities) {
            if (bullet.notValid()) {
                continue;
            }
            for (PlayerState player : players) {
                if (player.hp <= 0) {
                    continue;
                }
                if (isColliding(bullet.getLocationX(), bullet.getLocationY(), bulletRadius,
                        player.x, player.y, heroRadius)) {
                    player.hp = Math.max(0, player.hp - bullet.getPower());
                    consumedEnemyBullets.add(bullet);
                    bullet.vanish();
                    break;
                }
            }
        }

        for (Enemy enemy : enemyEntities) {
            if (enemy.notValid()) {
                continue;
            }
            for (PlayerState player : players) {
                if (player.hp <= 0) {
                    continue;
                }
                if (isColliding(enemy.getLocationX(), enemy.getLocationY(), enemyRadius(enemy),
                        player.x, player.y, heroRadius)) {
                    player.hp = Math.max(0, player.hp - (enemy instanceof BossEnemy ? ENEMY_COLLISION_DAMAGE + 10 : ENEMY_COLLISION_DAMAGE));
                    enemy.vanish();
                    break;
                }
            }
        }

        playerBulletEntities.removeIf(BaseBullet::notValid);
        enemyBulletEntities.removeAll(consumedEnemyBullets);
        enemyEntities.removeIf(Enemy::notValid);
    }

    private void resolvePvpCollisions(PlayerState[] players, float bulletRadius, float heroRadius) {
        List<BaseBullet> consumed = new ArrayList<>();
        for (BaseBullet bullet : playerBulletEntities) {
            if (bullet.notValid()) {
                continue;
            }
            int owner = bullet.getSpeedY() < 0 ? 0 : 1;
            int targetIndex = owner == 0 ? 1 : 0;
            PlayerState target = players[targetIndex];
            if (target.hp <= 0) {
                continue;
            }
            if (isColliding(bullet.getLocationX(), bullet.getLocationY(), bulletRadius, target.x, target.y, heroRadius)) {
                target.hp = Math.max(0, target.hp - bullet.getPower());
                bullet.vanish();
                consumed.add(bullet);
            }
        }
        playerBulletEntities.removeAll(consumed);
        enemyBulletEntities.clear();
        enemyEntities.clear();
    }

    void fillSnapshot(List<BulletState> outPlayerBullets,
                      List<BulletState> outEnemyBullets,
                      List<EnemyState> outEnemies) {
        outPlayerBullets.clear();
        for (BaseBullet bullet : playerBulletEntities) {
            if (bullet.notValid()) {
                continue;
            }
            BulletState state = new BulletState();
            state.owner = bullet.getSpeedY() < 0 ? 0 : 1;
            state.x = bullet.getLocationX();
            state.y = bullet.getLocationY();
            state.vx = bullet.getSpeedX();
            state.vy = bullet.getSpeedY();
            outPlayerBullets.add(state);
        }

        outEnemyBullets.clear();
        for (BaseBullet bullet : enemyBulletEntities) {
            if (bullet.notValid()) {
                continue;
            }
            BulletState state = new BulletState();
            state.owner = -1;
            state.x = bullet.getLocationX();
            state.y = bullet.getLocationY();
            state.vx = bullet.getSpeedX();
            state.vy = bullet.getSpeedY();
            outEnemyBullets.add(state);
        }

        outEnemies.clear();
        for (Enemy enemy : enemyEntities) {
            if (enemy.notValid()) {
                continue;
            }
            EnemyState state = new EnemyState();
            state.x = enemy.getLocationX();
            state.y = enemy.getLocationY();
            state.vx = 0f;
            state.vy = 0f;
            state.hp = enemy.getHp();
            state.maxHp = enemy.getMaxHp();
            state.boss = enemy instanceof BossEnemy;
            state.kind = enemyKindFromEntity(enemy);
            state.score = enemy.getScore();
            outEnemies.add(state);
        }
    }

    long getElapsedMs() {
        return elapsedMs;
    }

    int getSharedScore() {
        return sharedScore;
    }

    boolean isBossSpawned() {
        return bossSpawned;
    }

    boolean isBossDefeated() {
        return bossDefeated;
    }

    private int enemyKindFromEntity(Enemy enemy) {
        if (enemy instanceof BossEnemy) {
            return ENEMY_KIND_BOSS;
        }
        if (enemy instanceof ElitePlusEnemy) {
            return ENEMY_KIND_ELITE_PLUS;
        }
        if (enemy instanceof EliteEnemy) {
            return ENEMY_KIND_ELITE;
        }
        if (enemy instanceof MobEnemy) {
            return ENEMY_KIND_MOB;
        }
        return ENEMY_KIND_MOB;
    }

    private float enemyRadius(Enemy enemy) {
        int width = enemy.getWidth();
        int height = enemy.getHeight();
        float scale = enemy instanceof BossEnemy ? 0.78f : 0.72f;
        return Math.min(width, height) * 0.5f * scale;
    }

    private float clampYForPlayer(int playerIndex, float y, float heroHalfHeight, int worldHeight) {
        float minY = heroHalfHeight;
        float maxY = worldHeight - heroHalfHeight;
        if (!pve) {
            if (playerIndex == 0) {
                minY = worldHeight * 0.5f + heroHalfHeight;
            } else {
                maxY = worldHeight * 0.5f - heroHalfHeight;
            }
        } else {
            minY = worldHeight * 0.42f;
        }
        return clamp(y, minY, maxY);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private boolean isPlayerFacingUp(int playerIndex) {
        return pve || playerIndex == 0;
    }

    private boolean isColliding(float x1, float y1, float r1, float x2, float y2, float r2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        float r = r1 + r2;
        return dx * dx + dy * dy <= r * r;
    }
}
