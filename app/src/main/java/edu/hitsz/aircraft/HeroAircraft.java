package edu.hitsz.aircraft;

import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

public class HeroAircraft extends AbstractAircraft {

    private volatile static HeroAircraft instance = null;

    private long propDuration = 0;
    private long propEffectiveEndTime = 0;

    private HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        setDirection(-1);
        setPower(50);
        setBulletSpeed(15);
    }

    public static HeroAircraft getInstance() {
        if (instance == null) {
            synchronized (HeroAircraft.class) {
                if (instance == null) {
                    instance = new HeroAircraft(
                            Main.WINDOW_WIDTH / 2,
                            Main.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight(),
                            0,
                            0,
                            100
                    );
                }
            }
        }
        return instance;
    }

    public static synchronized void resetForNewGame() {
        instance = null;
    }

    @Override
    public void forward() {
    }

    public long getPropDuration() {
        return propDuration;
    }

    public void setPropDuration(long propDuration) {
        this.propDuration = propDuration;
    }

    public long getPropEffectiveEndTime() {
        return propEffectiveEndTime;
    }

    public void setPropEffectiveEndTime(long propEffectiveEndTime) {
        this.propEffectiveEndTime = propEffectiveEndTime;
    }
}
