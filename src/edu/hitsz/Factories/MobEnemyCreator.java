package edu.hitsz.Factories;

import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

public class MobEnemyCreator implements EnemyCreator
{

    private int speedX = 0;
    private int speedY = 10;
    private int hp = 30;

    @Override
    public MobEnemy createEnemy() {
        return new MobEnemy((int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.MOB_ENEMY_IMAGE.getWidth())),
                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05), speedX, speedY, hp);
    }
}
