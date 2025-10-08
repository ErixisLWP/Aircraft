package edu.hitsz.aircraft;

import edu.hitsz.Factories.PropCreator;
import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.prop.BaseProp;

import java.util.Collections;
import java.util.List;

public abstract class Enemy extends AbstractAircraft {

    private int score;

    protected PropCreator propCreator;

    public Enemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public void forward() {
        super.forward();
        if (locationY >= Main.WINDOW_HEIGHT) {
            vanish();
        }
    }

    /**
     * 获取坠机分数
     * @return
     */
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    /**
     * 掉落道具
     * @param props 道具列表
     * @param locationX 道具位置x坐标
     * @param locationY 道具位置y坐标
     */
    abstract public void dropProp(List<BaseProp> props, int locationX, int locationY);
}
