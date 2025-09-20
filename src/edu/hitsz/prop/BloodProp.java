package edu.hitsz.prop;

public class BloodProp extends BaseProp {
    private int healingHP;

    public BloodProp(int locationX, int locationY, int speedX, int speedY, int healingHP) {
        super(locationX, locationY, speedX, speedY);
        this.healingHP = healingHP;
    }

    public int getHealingHP() {
        return healingHP;
    }
}
