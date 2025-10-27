package edu.hitsz.Factories;

import edu.hitsz.aircraft.Enemy;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;
import edu.hitsz.strategy.NoShootStrategy;
import edu.hitsz.strategy.ShootStrategy;

public class MobEnemyCreator implements EnemyCreator
{

    private int speedX = 0;
    private int speedY = 10;
//    private int speedY = 5;
    private int hp = 30;

    @Override
    public Enemy createEnemy() {
        ShootStrategy shootStrategy = new NoShootStrategy();
        Enemy mobEnemy = new MobEnemy((int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.MOB_ENEMY_IMAGE.getWidth())),
                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05), speedX, speedY, hp);
        mobEnemy.setShootStrategy(shootStrategy);
        return mobEnemy;
    }
}
