package edu.hitsz.template;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.Game;

public class SimpleGameTemplateMethod extends GameTemplateMethod {

    private Game game;

    public SimpleGameTemplateMethod(Game game) {
        this.game = game;
        this.enemyMaxNumber = 5;
        this.heroAircraftShootCycle = 400;
        this.enemyCycle = 600;
        this.eliteEnemyProbability = 0.2f;
        this.elitePlusEnemyProbability = 0.05f;
        this.bossThreshold = 1000;
    }

    @Override
    public void setEnemyMaxNumber(int enemyMaxNumber) {
        game.setEnemyMaxNumber(enemyMaxNumber);
    }

    @Override
    public void setHeroAircraftShootCycle(int heroAircraftShootCycle) {
        game.setHeroAircraftCycleDuration(heroAircraftShootCycle);
    }

    @Override
    public void setEnemyBirthAndShootCycle(int enemyCycle) {
        game.setEnemyCycleDuration(enemyCycle);
    }

    @Override
    public void setEliteAndElitePlusEnemyProbability(float eliteEnemyProbability, float elitePlusEnemyProbability) {
        game.setEnemyProbability(eliteEnemyProbability, elitePlusEnemyProbability);
    }

    @Override
    public void setBossThreshold(int bossThreshold) {
        game.setBossThreshold(bossThreshold);
    }

    @Override
    public void getHarderByTime(HeroAircraft heroAircraft) {

    }
}
