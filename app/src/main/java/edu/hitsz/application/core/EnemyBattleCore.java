package edu.hitsz.application.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.hitsz.Factories.BossEnemyCreator;
import edu.hitsz.Factories.EliteEnemyCreator;
import edu.hitsz.Factories.ElitePlusEnemyCreator;
import edu.hitsz.Factories.MobEnemyCreator;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.Enemy;
import edu.hitsz.bullet.BaseBullet;

public class EnemyBattleCore {

    public static final class SpawnResult {
        private final List<Enemy> spawnedEnemies;
        private final boolean bossSpawned;

        public SpawnResult(List<Enemy> spawnedEnemies, boolean bossSpawned) {
            this.spawnedEnemies = spawnedEnemies;
            this.bossSpawned = bossSpawned;
        }

        public List<Enemy> getSpawnedEnemies() {
            return spawnedEnemies;
        }

        public boolean isBossSpawned() {
            return bossSpawned;
        }
    }

    private final MobEnemyCreator mobEnemyCreator = new MobEnemyCreator();
    private final EliteEnemyCreator eliteEnemyCreator = new EliteEnemyCreator();
    private final ElitePlusEnemyCreator elitePlusEnemyCreator = new ElitePlusEnemyCreator();
    private final BossEnemyCreator bossEnemyCreator = new BossEnemyCreator();

    private int bossAppearCount = 0;

    public void reset() {
        bossAppearCount = 0;
    }

    public SpawnResult spawnEnemies(List<Enemy> enemies,
                                    int score,
                                    int enemyMaxNumber,
                                    float eliteEnemyProbability,
                                    float elitePlusEnemyProbability,
                                    int bossThreshold) {
        if (enemies == null) {
            return new SpawnResult(Collections.emptyList(), false);
        }

        List<Enemy> spawned = new ArrayList<>();
        boolean bossSpawned = false;

        if (enemies.size() < enemyMaxNumber) {
            float randomNum = (float) Math.random();
            Enemy enemy;
            if (randomNum < elitePlusEnemyProbability) {
                enemy = elitePlusEnemyCreator.createEnemy();
            } else if (randomNum < elitePlusEnemyProbability + eliteEnemyProbability) {
                enemy = eliteEnemyCreator.createEnemy();
            } else {
                enemy = mobEnemyCreator.createEnemy();
            }
            enemies.add(enemy);
            spawned.add(enemy);
        }

        if (bossThreshold > 0 && score / bossThreshold > bossAppearCount && !hasActiveBoss(enemies)) {
            Enemy boss = bossEnemyCreator.createEnemy();
            enemies.add(boss);
            spawned.add(boss);
            bossAppearCount++;
            bossSpawned = true;
        }

        return new SpawnResult(spawned, bossSpawned);
    }

    public void appendEnemyBullets(List<Enemy> enemies, List<BaseBullet> outEnemyBullets) {
        if (enemies == null || outEnemyBullets == null) {
            return;
        }
        for (Enemy enemy : enemies) {
            if (enemy.notValid()) {
                continue;
            }
            List<BaseBullet> bullets = enemy.shoot();
            if (!bullets.isEmpty()) {
                outEnemyBullets.addAll(bullets);
            }
        }
    }

    private boolean hasActiveBoss(List<Enemy> enemies) {
        for (Enemy enemy : enemies) {
            if (enemy instanceof BossEnemy && !enemy.notValid()) {
                return true;
            }
        }
        return false;
    }
}
