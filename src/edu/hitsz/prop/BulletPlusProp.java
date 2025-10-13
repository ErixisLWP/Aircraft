package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.strategy.CircleShootStrategy;
import edu.hitsz.strategy.FanShootStrategy;

public class BulletPlusProp extends BaseProp {

    public BulletPlusProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    private void firePlusSupply(HeroAircraft heroAircraft) {
        System.out.println("FirePlusSupply active!");
        heroAircraft.setShootNum(20);
        heroAircraft.setShootStrategy(new CircleShootStrategy());
    }

    @Override
    public void takeEffect(HeroAircraft heroAircraft) {
        firePlusSupply(heroAircraft);
        this.vanish();
    }
}
