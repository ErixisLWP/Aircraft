package edu.hitsz.Factories;

import edu.hitsz.prop.BulletProp;

public class BulletPropCreator implements PropCreator{

    private int speedX = 0;
    private int speedY = 10;

    @Override
    public BulletProp createProp(int locationX, int locationY) {
        return new BulletProp(locationX, locationY, speedX, speedY);
    }
}
