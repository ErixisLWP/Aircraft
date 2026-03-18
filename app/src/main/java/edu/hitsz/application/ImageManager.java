package edu.hitsz.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.HashMap;
import java.util.Map;

import edu.hitsz.R;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.prop.BloodProp;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.BulletPlusProp;
import edu.hitsz.prop.BulletProp;

public final class ImageManager {
    private static final float SPRITE_SCALE_FACTOR = 0.75f;
    private static final float BOSS_SCALE_FACTOR = 0.6f;

    private static final Map<String, Bitmap> CLASSNAME_IMAGE_MAP = new HashMap<>();

    public static Bitmap BACKGROUND_IMAGE_SIMPLE;
    public static Bitmap BACKGROUND_IMAGE_NORMAL;
    public static Bitmap BACKGROUND_IMAGE_HARD;
    public static Bitmap HERO_IMAGE;
    public static Bitmap HERO_BULLET_IMAGE;
    public static Bitmap ENEMY_BULLET_IMAGE;
    public static Bitmap MOB_ENEMY_IMAGE;
    public static Bitmap ELITE_ENEMY_IMAGE;
    public static Bitmap ELITEPLUS_ENEMY_IMAGE;
    public static Bitmap BOSS_ENEMY_IMAGE;
    public static Bitmap PROP_BLOOD_IMAGE;
    public static Bitmap PROP_BOMB_IMAGE;
    public static Bitmap PROP_BULLET_IMAGE;
    public static Bitmap PROP_BULLETPLUS_IMAGE;

    private static int loadedWidth = -1;
    private static int loadedHeight = -1;

    private ImageManager() {
    }

    public static synchronized void init(Context context, int windowWidth, int windowHeight) {
        if (windowWidth <= 0 || windowHeight <= 0) {
            return;
        }
        if (windowWidth == loadedWidth && windowHeight == loadedHeight && HERO_IMAGE != null) {
            return;
        }

        float spriteScale = Math.min(windowWidth / 512f, windowHeight / 768f) * SPRITE_SCALE_FACTOR;
        BACKGROUND_IMAGE_SIMPLE = loadBitmap(context, R.drawable.bg, windowWidth, windowHeight);
        BACKGROUND_IMAGE_NORMAL = loadBitmap(context, R.drawable.bg4, windowWidth, windowHeight);
        BACKGROUND_IMAGE_HARD = loadBitmap(context, R.drawable.bg5, windowWidth, windowHeight);

        HERO_IMAGE = loadBitmap(context, R.drawable.hero, spriteScale);
        MOB_ENEMY_IMAGE = loadBitmap(context, R.drawable.mob, spriteScale);
        ELITE_ENEMY_IMAGE = loadBitmap(context, R.drawable.elite, spriteScale);
        ELITEPLUS_ENEMY_IMAGE = loadBitmap(context, R.drawable.elite_plus, spriteScale);
        BOSS_ENEMY_IMAGE = loadBitmap(context, R.drawable.boss, spriteScale * BOSS_SCALE_FACTOR);
        HERO_BULLET_IMAGE = loadBitmap(context, R.drawable.bullet_hero, spriteScale);
        ENEMY_BULLET_IMAGE = loadBitmap(context, R.drawable.bullet_enemy, spriteScale);
        PROP_BLOOD_IMAGE = loadBitmap(context, R.drawable.prop_blood, spriteScale);
        PROP_BOMB_IMAGE = loadBitmap(context, R.drawable.prop_bomb, spriteScale);
        PROP_BULLET_IMAGE = loadBitmap(context, R.drawable.prop_bullet, spriteScale);
        PROP_BULLETPLUS_IMAGE = loadBitmap(context, R.drawable.prop_bullet_plus, spriteScale);

        CLASSNAME_IMAGE_MAP.clear();
        CLASSNAME_IMAGE_MAP.put(HeroAircraft.class.getName(), HERO_IMAGE);
        CLASSNAME_IMAGE_MAP.put(MobEnemy.class.getName(), MOB_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(EliteEnemy.class.getName(), ELITE_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(ElitePlusEnemy.class.getName(), ELITEPLUS_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BossEnemy.class.getName(), BOSS_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(HeroBullet.class.getName(), HERO_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(EnemyBullet.class.getName(), ENEMY_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BloodProp.class.getName(), PROP_BLOOD_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BombProp.class.getName(), PROP_BOMB_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BulletProp.class.getName(), PROP_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BulletPlusProp.class.getName(), PROP_BULLETPLUS_IMAGE);

        loadedWidth = windowWidth;
        loadedHeight = windowHeight;
    }

    public static Bitmap get(String className) {
        return CLASSNAME_IMAGE_MAP.get(className);
    }

    public static Bitmap get(Object obj) {
        if (obj == null) {
            return null;
        }
        return get(obj.getClass().getName());
    }

    private static Bitmap loadBitmap(Context context, int resId, int width, int height) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private static Bitmap loadBitmap(Context context, int resId, float scale) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        int width = Math.max(1, Math.round(bitmap.getWidth() * scale));
        int height = Math.max(1, Math.round(bitmap.getHeight() * scale));
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
}

