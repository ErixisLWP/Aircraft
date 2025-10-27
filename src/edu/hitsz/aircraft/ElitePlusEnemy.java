package edu.hitsz.aircraft;

import edu.hitsz.Factories.BloodPropCreator;
import edu.hitsz.Factories.BombPropCreator;
import edu.hitsz.Factories.BulletPlusPropCreator;
import edu.hitsz.Factories.BulletPropCreator;
import edu.hitsz.application.Game;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.prop.BaseProp;
import edu.hitsz.prop.BombProp;

import java.util.LinkedList;
import java.util.List;

public class ElitePlusEnemy extends Enemy{

    private static double propRate = 0.7; // 掉落概率

    public ElitePlusEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        setScore(50);
        setPower(2);
//        setShootNum(3);
    }

    @Override
    public void dropProp(List<BaseProp> props, int locationX, int locationY) {
        if (Math.random() < propRate) {
            double randomNum = Math.random();
            if (randomNum < 1 / 2.0d) {
                propCreator = new BloodPropCreator();
            } else if (randomNum < 7 / 10.0d) {
                propCreator = new BombPropCreator();
            } else if (randomNum < 9 / 10.0d) {
                propCreator = new BulletPropCreator();
            }
            else {
                propCreator = new BulletPlusPropCreator();
            }
            props.add(propCreator.createProp(locationX, locationY));
        }
    }

    @Override
    public void update(BaseProp prop) {
        if (prop instanceof BombProp) {
            this.decreaseHp(((BombProp) prop).getDamage());
            if (hp == 0) {
                Game.addScore(this.getScore());
            }
        }
    }
}
