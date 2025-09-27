package edu.hitsz.Factories;
import edu.hitsz.aircraft.Enemy;
import edu.hitsz.aircraft.MobEnemy;

public interface EnemyCreator {
    public abstract Enemy createEnemy();
}
