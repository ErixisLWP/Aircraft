package edu.hitsz;

import edu.hitsz.Factories.BloodPropCreator;
import edu.hitsz.Factories.BombPropCreator;
import edu.hitsz.Factories.BulletPropCreator;
import edu.hitsz.aircraft.Enemy;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.prop.BaseProp;

import java.util.LinkedList;
import java.util.List;

public class BossEnemy extends Enemy {

    private int shootNum = 20; // 20颗子弹
    private int power = 3;
    private int bulletSpeed = 8;

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        setScore(100);
    }

    @Override
    public void dropProp(List<BaseProp> props, int locationX, int locationY) {
        int propCount = (int)(Math.random() * 4); // 0~3个
        for (int i = 0; i < propCount; i++) {
            double randomNum = Math.random();
            if (randomNum < 1 / 3.0d) {
                propCreator = new BloodPropCreator();
            } else if (randomNum < 2 / 3.0d) {
                propCreator = new BombPropCreator();
            } else {
                propCreator = new BulletPropCreator();
            }
            props.add(propCreator.createProp(locationX, locationY));
        }
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        int x = this.getLocationX();
        int y = this.getLocationY() + 2;
        for (int i = 0; i < shootNum; i++) {
            double angle = 2 * Math.PI * i / shootNum;
            int vx = (int)(bulletSpeed * Math.cos(angle));
            int vy = (int)(bulletSpeed * Math.sin(angle));
            res.add(new EnemyBullet(x, y, vx, vy, power));
        }
        return res;
    }
}
