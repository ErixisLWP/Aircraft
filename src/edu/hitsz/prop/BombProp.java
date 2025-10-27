package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.AudioManager;

public class BombProp extends BaseProp {

    private int damage;
    public BombProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
        this.damage = 100;
    }

    private void boom(HeroAircraft heroAircraft) {
        System.out.println("BombSupply active!");
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    @Override
    public void takeEffect(HeroAircraft heroAircraft) {
        // 爆炸音效
        AudioManager.playBombExplosionSound();
        boom(heroAircraft);
        this.vanish();
    }
}
