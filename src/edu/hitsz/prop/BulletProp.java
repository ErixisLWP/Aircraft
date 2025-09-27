package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

public class BulletProp extends BaseProp {
    public BulletProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    private void fireSupply(HeroAircraft heroAircraft) {
        System.out.println("FireSupply active!");
    }

    @Override
    public void takeEffect(HeroAircraft heroAircraft) {
        fireSupply(heroAircraft);
        this.vanish();
    }
}
