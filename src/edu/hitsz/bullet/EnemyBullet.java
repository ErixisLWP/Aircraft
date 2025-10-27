package edu.hitsz.bullet;

import edu.hitsz.observer.EnemyObserver;
import edu.hitsz.prop.BaseProp;
import edu.hitsz.prop.BombProp;

/**
 * @Author hitsz
 */
public class EnemyBullet extends BaseBullet {

    public EnemyBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
    }

}
