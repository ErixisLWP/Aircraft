package edu.hitsz.application;

import edu.hitsz.aircraft.Enemy;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.prop.BaseProp;

import java.util.ArrayList;
import java.util.List;

public class ObserverManager {
    private List<BaseBullet> enemyBulletObserverList;
    private List<Enemy> enemyAircraftObserverList;

    public ObserverManager() {
        enemyAircraftObserverList = new ArrayList<Enemy>();
        enemyBulletObserverList = new ArrayList<BaseBullet>();
    }

    public void registerBulletObserver(List<BaseBullet> enemyBullets) {
        enemyBulletObserverList.addAll(enemyBullets);
    }

    public void registerAircraftObserver(Enemy enemy) {
        enemyAircraftObserverList.add(enemy);
    }

    public void removeBulletObserver(BaseBullet enemyBullet) {
        enemyBulletObserverList.remove(enemyBullet);
    }

    public void removeAircraftObserver(Enemy enemy) {
        enemyAircraftObserverList.remove(enemy);
    }

    public void notifyAllObservers(BaseProp prop) {
        for(BaseBullet bullet : enemyBulletObserverList) {
            bullet.update(prop);
        }

        for(Enemy enemy : enemyAircraftObserverList) {
            enemy.update(prop);
        }
    }

    // 定期清理无效的子弹和敌机
    public void clear() {
        // 使用 removeIf 来安全地移除无效的观察者
        // 这不会导致 ConcurrentModificationException
        enemyBulletObserverList.removeIf(BaseBullet::notValid);
        enemyAircraftObserverList.removeIf(Enemy::notValid);
    }
}
