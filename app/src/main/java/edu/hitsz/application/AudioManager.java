package edu.hitsz.application;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Map;

import edu.hitsz.R;

public final class AudioManager {

    public static boolean audioEffect = true;

    private static final String BGM = "bgm";
    private static final String BGM_BOSS = "bgm_boss";
    private static final String SOUND_BOMB_EXPLOSION = "sound_bomb_explosion";
    private static final String SOUND_SHOOT = "sound_shoot";
    private static final String SOUND_BULLET_HIT = "sound_bullet_hit";
    private static final String SOUND_GAME_OVER = "sound_game_over";
    private static final String SOUND_PROP_GET = "sound_prop_get";

    private static final Map<String, Integer> rawResMap = new HashMap<>();
    private static final Map<String, Integer> soundPoolIds = new HashMap<>();

    private static Context appContext;
    private static SoundPool soundPool;
    private static MediaPlayer bgmPlayer;
    private static MediaPlayer bossBgmPlayer;
    private static String currentMusicKey;

    static {
        rawResMap.put(BGM, R.raw.bgm);
        rawResMap.put(BGM_BOSS, R.raw.bgm_boss);
        rawResMap.put(SOUND_BOMB_EXPLOSION, R.raw.bomb_explosion);
        rawResMap.put(SOUND_SHOOT, R.raw.bullet);
        rawResMap.put(SOUND_BULLET_HIT, R.raw.bullet_hit);
        rawResMap.put(SOUND_GAME_OVER, R.raw.game_over);
        rawResMap.put(SOUND_PROP_GET, R.raw.get_supply);
    }

    private AudioManager() {
    }

    public static synchronized void init(Context context) {
        if (context == null) {
            return;
        }
        appContext = context.getApplicationContext();
        initSoundPool();
        prepareMusicPlayers();
    }

    private static void initSoundPool() {
        if (soundPool != null || appContext == null) {
            return;
        }
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(audioAttributes)
                .build();

        soundPoolIds.clear();
        soundPoolIds.put(SOUND_BOMB_EXPLOSION, soundPool.load(appContext, rawResMap.get(SOUND_BOMB_EXPLOSION), 1));
        soundPoolIds.put(SOUND_SHOOT, soundPool.load(appContext, rawResMap.get(SOUND_SHOOT), 1));
        soundPoolIds.put(SOUND_BULLET_HIT, soundPool.load(appContext, rawResMap.get(SOUND_BULLET_HIT), 1));
        soundPoolIds.put(SOUND_GAME_OVER, soundPool.load(appContext, rawResMap.get(SOUND_GAME_OVER), 1));
        soundPoolIds.put(SOUND_PROP_GET, soundPool.load(appContext, rawResMap.get(SOUND_PROP_GET), 1));
    }

    private static void prepareMusicPlayers() {
        if (appContext == null) {
            return;
        }
        if (bgmPlayer == null) {
            bgmPlayer = createLoopingPlayer(BGM);
        }
        if (bossBgmPlayer == null) {
            bossBgmPlayer = createLoopingPlayer(BGM_BOSS);
        }
    }

    private static MediaPlayer createLoopingPlayer(String key) {
        Integer resId = rawResMap.get(key);
        if (appContext == null || resId == null) {
            return null;
        }
        MediaPlayer player = MediaPlayer.create(appContext, resId);
        if (player == null) {
            return null;
        }
        player.setLooping(true);
        player.setVolume(0.45f, 0.45f);
        return player;
    }

    private static MediaPlayer getPlayer(String key) {
        if (BGM.equals(key)) {
            return bgmPlayer;
        }
        if (BGM_BOSS.equals(key)) {
            return bossBgmPlayer;
        }
        return null;
    }

    private static void pauseMusic(MediaPlayer player) {
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    private static void playMusic(String key) {
        if (!audioEffect || appContext == null) {
            return;
        }
        prepareMusicPlayers();
        if (key.equals(currentMusicKey)) {
            MediaPlayer currentPlayer = getPlayer(key);
            if (currentPlayer != null && !currentPlayer.isPlaying()) {
                currentPlayer.start();
            }
            return;
        }

        pauseMusic(bgmPlayer);
        pauseMusic(bossBgmPlayer);

        MediaPlayer targetPlayer = getPlayer(key);
        if (targetPlayer == null) {
            return;
        }
        targetPlayer.seekTo(0);
        targetPlayer.start();
        currentMusicKey = key;
    }

    private static void playEffect(String key, float volume) {
        if (!audioEffect || soundPool == null || appContext == null) {
            return;
        }
        Integer soundId = soundPoolIds.get(key);
        if (soundId == null) {
            return;
        }
        soundPool.autoResume();
        soundPool.play(soundId, volume, volume, 1, 0, 1f);
    }

    public static synchronized void playBgm() {
        playMusic(BGM);
    }

    public static synchronized void playBossBgm() {
        playMusic(BGM_BOSS);
    }

    public static synchronized void playBombExplosionSound() {
        playEffect(SOUND_BOMB_EXPLOSION, 0.9f);
    }

    public static synchronized void playShootSound() {
        playEffect(SOUND_SHOOT, 0.35f);
    }

    public static synchronized void playBulletHitSound() {
        playEffect(SOUND_BULLET_HIT, 0.55f);
    }

    public static synchronized void playGameOverSound() {
        stopAllSounds();
        playEffect(SOUND_GAME_OVER, 0.9f);
    }

    public static synchronized void playPropGetSound() {
        playEffect(SOUND_PROP_GET, 0.7f);
    }

    public static synchronized void stopAllSounds() {
        pauseMusic(bgmPlayer);
        pauseMusic(bossBgmPlayer);
        if (bgmPlayer != null) {
            bgmPlayer.seekTo(0);
        }
        if (bossBgmPlayer != null) {
            bossBgmPlayer.seekTo(0);
        }
        currentMusicKey = null;
        if (soundPool != null) {
            soundPool.autoPause();
        }
    }

    public static synchronized void release() {
        stopAllSounds();
        if (bgmPlayer != null) {
            bgmPlayer.release();
            bgmPlayer = null;
        }
        if (bossBgmPlayer != null) {
            bossBgmPlayer.release();
            bossBgmPlayer = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        soundPoolIds.clear();
        appContext = null;
    }
}