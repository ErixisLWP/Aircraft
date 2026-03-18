package edu.hitsz.Factories;

import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.aircraft.Enemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;
import edu.hitsz.strategy.FanShootStrategy;
import edu.hitsz.strategy.ShootStrategy;

public class ElitePlusEnemyCreator implements EnemyCreator{

    private int speedX = Math.random() < 0.5 ? -5 : 5;
    private int speedY = 5;
    private int hp = 150;

    @Override
    public Enemy createEnemy() {
        ShootStrategy shootStrategy = new FanShootStrategy();
        Enemy elitePlusEnemy = new ElitePlusEnemy((int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.ELITEPLUS_ENEMY_IMAGE.getWidth())),
                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05), speedX, speedY, hp);
        elitePlusEnemy.setShootStrategy(shootStrategy);
        return elitePlusEnemy;
    }
}
