package edu.hitsz.template;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.Game;

public class NormalGameTemplateMethod extends  GameTemplateMethod {
    private Game game;

    public NormalGameTemplateMethod(Game game) {
        this.game = game;
        this.enemyMaxNumber = 7;
        this.heroAircraftShootCycle = 600;
        this.enemyCycle = 500;
        this.eliteEnemyProbability = 0.3f;
        this.elitePlusEnemyProbability = 0.1f;
        this.bossThreshold = 800;
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
        int currentHeroAircraftPower = heroAircraft.getPower();
        if (currentHeroAircraftPower > 30) {
            currentHeroAircraftPower--;
            System.out.println("难度提升！我方英雄机伤害降低 1 点，当前伤害为 " + currentHeroAircraftPower + " 点");
            heroAircraft.setPower(currentHeroAircraftPower);
        }
        else {
            System.out.println("难度已提升至最高！我方英雄机当前伤害为 " + currentHeroAircraftPower + " 点");
        }
    }
}
