package edu.hitsz.aircraft;

import edu.hitsz.Factories.BloodPropCreator;
import edu.hitsz.Factories.BombPropCreator;
import edu.hitsz.Factories.BulletPropCreator;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.prop.BaseProp;

import java.util.LinkedList;
import java.util.List;

public class ElitePlusEnemy extends Enemy{

    private int shootNum = 3; // 3颗子弹
    private int power = 2;
    private int direction = 1;
    private int bulletSpeed = 10;
    private static double propRate = 0.7; // 掉落概率

    public ElitePlusEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        setScore(50);
    }

    @Override
    public void dropProp(List<BaseProp> props, int locationX, int locationY) {
        if (Math.random() < propRate) {
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
        int y = this.getLocationY() + direction * 2;
        double[] angle = {-15, 0, 15}; // 扇形角度
        for (int i = 0; i < shootNum; i++) {
            double rad = Math.toRadians(angle[i]);
            int vx = (int)(bulletSpeed * Math.sin(rad));
            int vy = (int)(bulletSpeed * Math.cos(rad)) * direction;
            res.add(new EnemyBullet(x, y, vx, vy, power));
        }
        return res;
    }
}
