package edu.hitsz.Factories;

import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.Enemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;
import edu.hitsz.strategy.CircleShootStrategy;
import edu.hitsz.strategy.ShootStrategy;

public class BossEnemyCreator implements EnemyCreator{

    private int speedX = Math.random() < 0.5 ? -2 : 2;
    private int speedY = 0;
    private int hp = 1000;

    @Override
    public Enemy createEnemy() {
        ShootStrategy shootStrategy = new CircleShootStrategy();
        int halfWidth = ImageManager.BOSS_ENEMY_IMAGE.getWidth() / 2;
        int spawnWidth = Math.max(1, Main.WINDOW_WIDTH - ImageManager.BOSS_ENEMY_IMAGE.getWidth());
        int locationX = halfWidth + (int) (Math.random() * spawnWidth);
        int locationY = ImageManager.BOSS_ENEMY_IMAGE.getHeight() / 4;
        Enemy bossEnemy = new BossEnemy(locationX, locationY, speedX, speedY, hp);
        bossEnemy.setShootStrategy(shootStrategy);
        return bossEnemy;
    }
}