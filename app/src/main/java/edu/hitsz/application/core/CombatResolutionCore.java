package edu.hitsz.application.core;

import java.util.List;

import edu.hitsz.aircraft.Enemy;
import edu.hitsz.bullet.BaseBullet;

public class CombatResolutionCore {

    public interface EnemyDestroyedCallback {
        void onDestroyed(Enemy enemy);
    }

    public static final class BulletEnemyCollisionResult {
        private final int hitCount;
        private final int scoreGain;

        public BulletEnemyCollisionResult(int hitCount, int scoreGain) {
            this.hitCount = hitCount;
            this.scoreGain = scoreGain;
        }

        public int getHitCount() {
            return hitCount;
        }

        public int getScoreGain() {
            return scoreGain;
        }
    }

    public BulletEnemyCollisionResult collidePlayerBulletsWithEnemies(
            List<BaseBullet> playerBullets,
            List<Enemy> enemies,
            EnemyDestroyedCallback destroyedCallback
    ) {
        if (playerBullets == null || enemies == null) {
            return new BulletEnemyCollisionResult(0, 0);
        }

        int hitCount = 0;
        int scoreGain = 0;

        for (BaseBullet bullet : playerBullets) {
            if (bullet.notValid()) {
                continue;
            }
            for (Enemy enemy : enemies) {
                if (enemy.notValid()) {
                    continue;
                }
                if (!enemy.crash(bullet)) {
                    continue;
                }

                hitCount++;
                enemy.decreaseHp(bullet.getPower());
                bullet.vanish();

                if (enemy.notValid()) {
                    scoreGain += enemy.getScore();
                    if (destroyedCallback != null) {
                        destroyedCallback.onDestroyed(enemy);
                    }
                }
                break;
            }
        }

        return new BulletEnemyCollisionResult(hitCount, scoreGain);
    }
}
