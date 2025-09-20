package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import java.util.List;
import java.util.LinkedList;

/**
 * 精英敌机
 * 可以射击
 *
 * @author Erixis
 */
public class EliteEnemy extends Enemy{

    private int shootNum = 1;

    private int power = 20;

    private int direction = 1;

    private int bulletSpeedX = 0;
    private int bulletSpeedY = 10;

    private double propRate = 0.75;

    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        int x = this.getLocationX();
        int y = this.getLocationY() + direction * 2;
        BaseBullet bullet;
        for(int i=0; i<shootNum; i++){
            bullet = new EnemyBullet(x + (i*2 - shootNum + 1)*10, y, bulletSpeedX, bulletSpeedY, power);
            res.add(bullet);
        }
        return res;
    }

    public double getPropRate() {
        return propRate;
    }
}
