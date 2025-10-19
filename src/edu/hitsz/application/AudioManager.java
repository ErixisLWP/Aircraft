package edu.hitsz.application;

import edu.hitsz.thread.MusicThread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 综合管理音频的加载、播放
 * 提供音频的静态访问方法
 *
 * @author hitsz
 */
public class AudioManager {
    // 音效开关
    public static boolean audioEffect = true;

    // 存储所有音频文件的路径
    private static final Map<String, String> soundPathMap = new HashMap<>();

    // 存储当前正在循环播放的线程，使用 ConcurrentHashMap 以确保线程安全
    private static final Map<String, MusicThread> activeLoopingThreads = new ConcurrentHashMap<>();

    // 定义所有音效的键，方便外部调用，避免硬编码字符串
    public static final String BGM = "bgm";
    public static final String BGM_BOSS = "bgm_boss";
    public static final String SOUND_BOMB_EXPLOSION = "sound_bomb_explosion";
    public static final String SOUND_SHOOT = "sound_shoot";
    public static final String SOUND_BULLET_HIT = "sound_bullet_hit";
    public static final String SOUND_GAME_OVER = "sound_game_over";
    public static final String SOUND_PROP_GET = "sound_prop_get";

    static {
        // 在静态代码块中初始化所有音频文件的路径
        // 请将 "path/to/your/sound/" 替换为你的实际音频文件路径
        soundPathMap.put(BGM, "src/videos/bgm.wav");
        soundPathMap.put(BGM_BOSS, "src/videos/bgm_boss.wav");
        soundPathMap.put(SOUND_BOMB_EXPLOSION, "src/videos/bomb_explosion.wav");
        soundPathMap.put(SOUND_SHOOT, "src/videos/bullet.wav");
        soundPathMap.put(SOUND_BULLET_HIT, "src/videos/bullet_hit.wav");
        soundPathMap.put(SOUND_GAME_OVER, "src/videos/game_over.wav");
        soundPathMap.put(SOUND_PROP_GET, "src/videos/get_supply.wav");
    }

    /**
     * 播放音效
     * @param key 音效的键 (例如, AudioManager.BGM)
     * @param isLoop 是否循环播放
     */
    private static void play(String key, boolean isLoop) {
        // 由用户选择是否开启音效
        if (!audioEffect) {
            return;
        }

        String path = soundPathMap.get(key);
        if (path == null) {
            System.err.println("音频文件未找到: " + key);
            return;
        }

        // 如果是循环音效，先检查是否已有同名音效在播放，若有则停止旧的
        if (isLoop) {
            stop(key);
        }

        MusicThread musicThread = new MusicThread(path, isLoop);
        musicThread.start();

        // 如果是循环播放的音效，则将其加入管理map中，方便后续停止
        if (isLoop) {
            activeLoopingThreads.put(key, musicThread);
        }
    }

    /**
     * 停止指定的循环音效
     * @param key 音效的键
     */
    public static void stop(String key) {
        MusicThread thread = activeLoopingThreads.get(key);
        if (thread != null && thread.isAlive()) {
            thread.stopMusic();
            activeLoopingThreads.remove(key);
        }
    }

    // --- 提供给外部调用的便捷方法 ---

    public static void playBgm() {
        stop(BGM_BOSS);
        play(BGM, true);
    }

    public static void playBossBgm() {
        stop(BGM);
        play(BGM_BOSS, true);
    }

    public static void playBombExplosionSound() {
        play(SOUND_BOMB_EXPLOSION, false);
    }

    public static void playShootSound() {
        play(SOUND_SHOOT, false);
    }

    public static void playBulletHitSound() {
        play(SOUND_BULLET_HIT, false);
    }

    public static void playGameOverSound() {
        stopAllSounds();
        play(SOUND_GAME_OVER, false);
    }

    public static void playPropGetSound() {
        play(SOUND_PROP_GET, false);
    }

    /**
     * 停止所有正在循环的音效
     */
    public static void stopAllSounds() {
        // 遍历map，停止所有循环音效
        for (MusicThread thread : activeLoopingThreads.values()) {
            thread.stopMusic();
        }
        activeLoopingThreads.clear();
    }
}