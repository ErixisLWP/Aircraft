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

    private static double propRate = 0.75;

    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        setScore(20);
        setPower(1);
//        setShootNum(1);
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
            if (randomNum2 < 1 / 2.0d) {
                propCreator = new BloodPropCreator();
                props.add(propCreator.createProp(locationX, locationY));
            }else if (randomNum2 < 3 / 4.0d) {
                propCreator = new BombPropCreator();
                props.add(propCreator.createProp(locationX, locationY));
            } else {
                propCreator = new BulletPropCreator();
                props.add(propCreator.createProp(locationX, locationY));
            }
        }
        return;
    }

    public double getPropRate() {
        return propRate;
    }
}
