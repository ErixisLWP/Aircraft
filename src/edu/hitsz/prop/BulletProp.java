package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.strategy.FanShootStrategy;

public class BulletProp extends BaseProp {

    private long duration = 6000;  // 持续6秒

    public BulletProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    private void fireSupply(HeroAircraft heroAircraft) {
        System.out.println("FireSupply active!");
        // 道具过期时间 = 当前时间 + 持续时间
        heroAircraft.setPropDuration(duration);
        heroAircraft.setPropEffectiveEndTime(System.currentTimeMillis() + duration);
//        heroAircraft.setShootNum(3);
        heroAircraft.setShootStrategy(new FanShootStrategy());
    }

    @Override
    public void takeEffect(HeroAircraft heroAircraft) {
        super.takeEffect(heroAircraft);
        fireSupply(heroAircraft);
        this.vanish();
    }
}
