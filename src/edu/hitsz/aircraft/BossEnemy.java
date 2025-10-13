package edu.hitsz.aircraft;

import edu.hitsz.Factories.BloodPropCreator;
import edu.hitsz.Factories.BombPropCreator;
import edu.hitsz.Factories.BulletPlusPropCreator;
import edu.hitsz.Factories.BulletPropCreator;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.prop.BaseProp;

import java.util.LinkedList;
import java.util.List;

public class BossEnemy extends Enemy {

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        setScore(100);
        setPower(3);
        setShootNum(20);
    }

    @Override
    public void dropProp(List<BaseProp> props, int locationX, int locationY) {
        int propCount = (int)(Math.random() * 4); // 0~3个
        for (int i = 0; i < propCount; i++) {
            double randomNum = Math.random();
            if (randomNum < 3 / 10.0d) {
                propCreator = new BloodPropCreator();
            } else if (randomNum < 6 / 10.0d) {
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
}
