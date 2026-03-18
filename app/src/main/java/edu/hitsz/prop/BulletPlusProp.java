package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.strategy.CircleShootStrategy;
import edu.hitsz.strategy.FanShootStrategy;

public class BulletPlusProp extends BaseProp {

    private long duration = 6000;  // 持续6秒

    public BulletPlusProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    private void firePlusSupply(HeroAircraft heroAircraft) {
        System.out.println("FirePlusSupply active!");
        // 道具过期时间 = 当前时间 + 持续时间
        heroAircraft.setPropDuration(duration);
        heroAircraft.setPropEffectiveEndTime(System.currentTimeMillis() + duration);
//        heroAircraft.setShootNum(20);
        heroAircraft.setShootStrategy(new CircleShootStrategy());
    }

    @Override
    public void takeEffect(HeroAircraft heroAircraft) {
        super.takeEffect(heroAircraft);
        firePlusSupply(heroAircraft);
        this.vanish();
    }
}
