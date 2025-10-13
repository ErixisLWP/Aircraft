package edu.hitsz.Factories;

import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.Enemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;
import edu.hitsz.strategy.CircleShootStrategy;
import edu.hitsz.strategy.ShootStrategy;

public class BossEnemyCreator implements EnemyCreator{

    private int speedX = Math.random() < 0.5 ? -5 : 5;
    private int speedY = 0;
    private int hp = 500;

    @Override
    public Enemy createEnemy() {
        ShootStrategy shootStrategy = new CircleShootStrategy();
        Enemy bossEnemy = new BossEnemy((int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.BOSS_ENEMY_IMAGE.getWidth())),
                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05), speedX, speedY, hp);
        bossEnemy.setShootStrategy(shootStrategy);
        return bossEnemy;
    }
}
