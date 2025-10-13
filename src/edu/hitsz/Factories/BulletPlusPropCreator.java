package edu.hitsz.Factories;

import edu.hitsz.prop.BulletPlusProp;
import edu.hitsz.prop.BulletProp;

public class BulletPlusPropCreator implements PropCreator{

    private int speedX = 0;
    private int speedY = 10;

    @Override
    public BulletPlusProp createProp(int locationX, int locationY) {
        return new BulletPlusProp(locationX, locationY, speedX, speedY);
    }
}
