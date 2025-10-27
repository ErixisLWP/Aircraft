package edu.hitsz.template;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.Game;

public class HardGameTemplateMethod extends  GameTemplateMethod {
    private Game game;

    public HardGameTemplateMethod(Game game) {
        this.game = game;
        this.enemyMaxNumber = 7;
        this.heroAircraftShootCycle = 800;
        this.enemyCycle = 400;
        this.eliteEnemyProbability = 0.4f;
        this.elitePlusEnemyProbability = 0.15f;
        this.bossThreshold = 500;
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
        if (currentHeroAircraftPower > 10) {
            currentHeroAircraftPower -= 2;
            System.out.println("难度提升！我方英雄机伤害降低 2 点，当前伤害为 " + currentHeroAircraftPower + " 点");
            heroAircraft.setPower(currentHeroAircraftPower);
        }
        else {
            System.out.println("难度已提升至最高！我方英雄机当前伤害为 " + currentHeroAircraftPower + " 点");
        }
    }
}
