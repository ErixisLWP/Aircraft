package edu.hitsz.Factories;

import edu.hitsz.prop.BombProp;

public class BombPropCreator implements PropCreator {

    private int speedX = 0;
    private int speedY = 10;

    @Override
    public BombProp createProp(int locationX, int locationY) {
        return new BombProp(locationX, locationY, speedX, speedY);
    }
}
