package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.prop.BulletProp;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class NoShootStrategy implements ShootStrategy{

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        return new LinkedList<>();
    }
}
