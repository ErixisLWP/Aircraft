package edu.hitsz.template;

import edu.hitsz.aircraft.HeroAircraft;

public abstract class GameTemplateMethod {

    protected int enemyMaxNumber;
    protected int heroAircraftShootCycle;
    protected int enemyCycle;
    protected float eliteEnemyProbability;
    protected float elitePlusEnemyProbability;
    protected int bossThreshold;

    public abstract void setEnemyMaxNumber(int enemyMaxNumber);
    public abstract void setHeroAircraftShootCycle(int heroAircraftShootCycle);
    public abstract void setEnemyBirthAndShootCycle(int enemyCycle);
    public abstract void setEliteAndElitePlusEnemyProbability(float eliteEnemyProbability, float elitePlusEnemyProbability);
    public abstract void setBossThreshold(int bossThreshold);
    public abstract void getHarderByTime(HeroAircraft heroAircraft);

    public final void setGameDifficulty() {
        setEnemyMaxNumber(enemyMaxNumber);
        setHeroAircraftShootCycle(heroAircraftShootCycle);
        setEnemyBirthAndShootCycle(enemyCycle);
        setEliteAndElitePlusEnemyProbability(eliteEnemyProbability, elitePlusEnemyProbability);
        setBossThreshold(bossThreshold);
    }
}
