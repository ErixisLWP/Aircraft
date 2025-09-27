package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

public class BloodProp extends BaseProp {
    private int healingHP = 30;

    public BloodProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    public int getHealingHP() {
        return healingHP;
    }

    private void heal(HeroAircraft heroAircraft) {
        heroAircraft.increaseHp(healingHP);
    }

    @Override
    public void takeEffect(HeroAircraft heroAircraft) {
        heal(heroAircraft);
        this.vanish();
    }
}
