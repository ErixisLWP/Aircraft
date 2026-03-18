package edu.hitsz.Factories;

import edu.hitsz.prop.BaseProp;
import edu.hitsz.prop.BloodProp;

public class BloodPropCreator implements PropCreator{

    private int speedX = 0;
    private int speedY = 10;

    @Override
    public BloodProp createProp(int locationX, int locationY) {
        return new BloodProp(locationX, locationY, speedX, speedY);
    }
}
