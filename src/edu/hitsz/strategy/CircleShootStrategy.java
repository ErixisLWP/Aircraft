package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

public class CircleShootStrategy implements ShootStrategy{
    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + 2;
        for (int i = 0; i < aircraft.getShootNum() ; i++) {
            double angle = 2 * Math.PI * i / aircraft.getShootNum() ;
            int vx = (int)(aircraft.getBulletSpeed() * Math.cos(angle));
            int vy = (int)(aircraft.getBulletSpeed() * Math.sin(angle)) * aircraft.getDirection();
            res.add((aircraft instanceof HeroAircraft) ? new HeroBullet(x, y, vx, vy, aircraft.getPower()) :
                    new EnemyBullet(x, y, vx, vy, aircraft.getPower()));
        }
        return res;
    }
}
