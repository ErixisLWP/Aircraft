package edu.hitsz.aircraft;

import edu.hitsz.Factories.*;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.prop.BaseProp;

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

    private int power = 1;

    private int direction = 1;

    private int bulletSpeedX = 0;
    private int bulletSpeedY = 10;

    private static double propRate = 0.75;

    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        setScore(20);
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

    /**
     * 精英敌机掉落道具
     * @param props 道具列表
     * @param locationX 道具位置x坐标
     * @param locationY 道具位置y坐标
     */
    @Override
    public void dropProp(List<BaseProp> props, int locationX, int locationY) {
        double randomNum1 = Math.random();
        if (randomNum1 < propRate) {
            // 随机数判定道具种类
            double randomNum2 = Math.random();
            if (randomNum2 < 1 / 3.0d) {
                BloodPropCreator bloodPropCreator = new BloodPropCreator();
                props.add(bloodPropCreator.createProp(locationX, locationY));
            }else if (randomNum2 < 2 / 3.0d) {
                BombPropCreator bombPropCreator = new BombPropCreator();
                props.add(bombPropCreator.createProp(locationX, locationY));
            } else {
                BulletPropCreator bulletPropCreator = new BulletPropCreator();
                props.add(bulletPropCreator.createProp(locationX, locationY));
            }
        }
        return;
    }

    public double getPropRate() {
        return propRate;
    }
}
