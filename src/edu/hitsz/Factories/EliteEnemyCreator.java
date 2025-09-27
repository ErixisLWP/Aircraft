package edu.hitsz.Factories;

import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.Enemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

public class EliteEnemyCreator implements EnemyCreator {

    private int speedX = 0;
    private int speedY = 5;
    private int hp = 100;

    @Override
    public Enemy createEnemy() {
        return new EliteEnemy((int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.ELITE_ENEMY_IMAGE.getWidth())),
                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05), speedX, speedY, hp);
    }
}