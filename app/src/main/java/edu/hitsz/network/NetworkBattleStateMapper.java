package edu.hitsz.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

final class NetworkBattleStateMapper {

    private NetworkBattleStateMapper() {
    }

    static void applyPlayer(JSONObject json, PlayerState target) {
        if (json == null || target == null) {
            return;
        }
        target.x = (float) json.optDouble("x", target.x);
        target.y = (float) json.optDouble("y", target.y);
        target.hp = json.optInt("hp", target.hp);
    }

    static JSONArray toPlayerArray(PlayerState[] players) throws JSONException {
        JSONArray playerArray = new JSONArray();
        if (players == null) {
            return playerArray;
        }
        for (PlayerState player : players) {
            JSONObject item = new JSONObject();
            item.put("x", player.x);
            item.put("y", player.y);
            item.put("hp", player.hp);
            playerArray.put(item);
        }
        return playerArray;
    }

    static List<BulletState> parseBulletArray(JSONArray bulletArray, int defaultOwner) {
        List<BulletState> bullets = new ArrayList<>();
        if (bulletArray == null) {
            return bullets;
        }

        for (int i = 0; i < bulletArray.length(); i++) {
            JSONObject item = bulletArray.optJSONObject(i);
            if (item == null) {
                continue;
            }
            BulletState bullet = new BulletState();
            bullet.x = (float) item.optDouble("x", 0);
            bullet.y = (float) item.optDouble("y", 0);
            bullet.vx = (float) item.optDouble("vx", 0);
            bullet.vy = (float) item.optDouble("vy", 0);
            bullet.owner = item.has("owner") ? item.optInt("owner", defaultOwner) : defaultOwner;
            bullets.add(bullet);
        }
        return bullets;
    }

    static JSONArray toBulletArray(List<BulletState> bullets, boolean includeOwner) throws JSONException {
        JSONArray bulletArray = new JSONArray();
        if (bullets == null) {
            return bulletArray;
        }

        for (BulletState bullet : bullets) {
            JSONObject item = new JSONObject();
            item.put("x", bullet.x);
            item.put("y", bullet.y);
            item.put("vx", bullet.vx);
            item.put("vy", bullet.vy);
            if (includeOwner) {
                item.put("owner", bullet.owner);
            }
            bulletArray.put(item);
        }
        return bulletArray;
    }

    static List<EnemyState> parseEnemyArray(JSONArray enemyArray, int defaultMobKind, int bossKind) {
        List<EnemyState> enemies = new ArrayList<>();
        if (enemyArray == null) {
            return enemies;
        }

        for (int i = 0; i < enemyArray.length(); i++) {
            JSONObject item = enemyArray.optJSONObject(i);
            if (item == null) {
                continue;
            }
            EnemyState enemy = new EnemyState();
            enemy.x = (float) item.optDouble("x", 0);
            enemy.y = (float) item.optDouble("y", 0);
            enemy.vx = (float) item.optDouble("vx", 0);
            enemy.vy = (float) item.optDouble("vy", 0);
            enemy.hp = item.optInt("hp", 0);
            enemy.maxHp = item.optInt("maxHp", enemy.hp);
            enemy.boss = item.optBoolean("boss", false);
            enemy.kind = item.optInt("kind", enemy.boss ? bossKind : defaultMobKind);
            enemy.score = item.optInt("score", enemy.boss ? 100 : 10);
            enemies.add(enemy);
        }
        return enemies;
    }

    static JSONArray toEnemyArray(List<EnemyState> enemies) throws JSONException {
        JSONArray enemyArray = new JSONArray();
        if (enemies == null) {
            return enemyArray;
        }

        for (EnemyState enemy : enemies) {
            JSONObject item = new JSONObject();
            item.put("x", enemy.x);
            item.put("y", enemy.y);
            item.put("vx", enemy.vx);
            item.put("vy", enemy.vy);
            item.put("hp", enemy.hp);
            item.put("maxHp", enemy.maxHp);
            item.put("boss", enemy.boss);
            item.put("kind", enemy.kind);
            item.put("score", enemy.score);
            enemyArray.put(item);
        }
        return enemyArray;
    }
}
