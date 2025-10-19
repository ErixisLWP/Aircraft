package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

public class FanShootStrategy implements ShootStrategy{

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        aircraft.setShootNum(3);
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + aircraft.getDirection() * 2;
        double[] angle = {-15, 0, 15}; // 扇形角度
        for (int i = 0; i < aircraft.getShootNum(); i++) {
            double rad = Math.toRadians(angle[i]);
            int vx = (int)(aircraft.getBulletSpeed() * Math.sin(rad));
            int vy = (int)(aircraft.getBulletSpeed() * Math.cos(rad)) * aircraft.getDirection();
            res.add((aircraft instanceof HeroAircraft) ? new HeroBullet(x, y, vx, vy, aircraft.getPower()) :
                    new EnemyBullet(x, y, vx, vy, aircraft.getPower()));
        }
        return res;
    }
}
