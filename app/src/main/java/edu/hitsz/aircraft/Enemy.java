package edu.hitsz.aircraft;

import edu.hitsz.Factories.PropCreator;
import edu.hitsz.application.Game;
import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.observer.EnemyObserver;
import edu.hitsz.prop.BaseProp;
import edu.hitsz.prop.BombProp;

import java.util.Collections;
import java.util.List;

public abstract class Enemy extends AbstractAircraft implements EnemyObserver {

    private int score;

    protected PropCreator propCreator;

    public Enemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        setDirection(1);
        setBulletSpeed(15);
    }

    @Override
    public void forward() {
        super.forward();
        if (locationY >= Main.WINDOW_HEIGHT) {
            vanish();
        }
    }

    /**
     * 鑾峰彇鍧犳満鍒嗘暟
     * @return
     */
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    /**
     * 鎺夎惤閬撳叿
     * @param props 閬撳叿鍒楄〃
     * @param locationX 閬撳叿浣嶇疆x鍧愭爣
     * @param locationY 閬撳叿浣嶇疆y鍧愭爣
     */
    abstract public void dropProp(List<BaseProp> props, int locationX, int locationY);

    @Override
    public void update(BaseProp prop) {
        if (prop instanceof BombProp) {
            Game.addScore(score);
            this.vanish();
        }
    }
}
