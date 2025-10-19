package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.AudioManager;

public class BombProp extends BaseProp {

    public BombProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    private void boom(HeroAircraft heroAircraft) {
        System.out.println("BombSupply active!");
    }

    @Override
    public void takeEffect(HeroAircraft heroAircraft) {
        // 爆炸音效
        AudioManager.playBombExplosionSound();
        boom(heroAircraft);
        this.vanish();
    }
}
