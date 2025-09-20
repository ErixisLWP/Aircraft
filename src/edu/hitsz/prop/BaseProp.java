package edu.hitsz.prop;

import edu.hitsz.application.Main;
import edu.hitsz.basic.AbstractFlyingObject;

public abstract class BaseProp extends AbstractFlyingObject {
    public BaseProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void forward() {
        super.forward();
        if (locationY >= Main.WINDOW_HEIGHT) {
            vanish();
        }
    }
}
