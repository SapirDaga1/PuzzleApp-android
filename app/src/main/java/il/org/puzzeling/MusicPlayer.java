package il.org.puzzeling;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MusicPlayer {
    private static final String TAG = "MusicManager";
    public static final int MUSIC_PREVIOUS = -1;
    public static final int MUSIC_MENU = 0;
    public static final int MUSIC_GAME = 1;
    public static final int MUSIC_END_GAME = 2;

    private static HashMap players = new HashMap();
    private static int currentMusic = -1;
    private static int previousMusic = -1;

    public static void start(Context context, int music) {
        start(context, music, false);
    }

    public static void start(Context context, int music, boolean force) {
        if (!force && currentMusic > -1) {
            // already playing some music and not forced to change
            return;
        }
        if (music == MUSIC_PREVIOUS) {
            music = previousMusic;
        }
        if (currentMusic == music) {
            // already playing this music
            return;
        }
        if (currentMusic != -1) {
            previousMusic = currentMusic;
            pause();
        }
        currentMusic = music;
        MediaPlayer mp = (MediaPlayer) players.get(music);
        if (mp != null) {
            if (!mp.isPlaying()) {
                mp.start();
            }
        } else {
            if (music == MUSIC_MENU) {
                mp = MediaPlayer.create(context, R.raw.feeling_free);
            } else if (music == MUSIC_GAME) {
                mp = MediaPlayer.create(context, R.raw.feeling_free);
            } else if (music == MUSIC_END_GAME) {
                mp = MediaPlayer.create(context, R.raw.feeling_free);
            } else {
                Log.e(TAG, "invalid music number - " + music);
                return;
            }
            players.put(music, mp);
            mp.setVolume(600, 600);
            if (mp == null) {
                Log.e(TAG, "failed");
            } else {
                try {
                    mp.setLooping(true);
                    mp.start();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }

    public static void pause() {
        Collection mps = players.values();
        Iterator hmIterator = players.entrySet().iterator();
        while (hmIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry)hmIterator.next();
            MediaPlayer p = (MediaPlayer) mapElement.getValue();
            if (p.isPlaying()) {
                p.pause();
            }
        }
        // previousMusic should always be something valid
        if (currentMusic != -1) {
            previousMusic = currentMusic;
        }
        currentMusic = -1;
    }

    public static void updateVolumeFromPrefs(Context context) {
        try {
            //float volume = getMusicVolume(context);
            //Log.d(TAG, "Setting music volume to " + volume);
            Collection mps = players.values();
            Iterator hmIterator = players.entrySet().iterator();
            while (hmIterator.hasNext()) {
                Map.Entry mapElement = (Map.Entry)hmIterator.next();
                MediaPlayer p = (MediaPlayer) mapElement.getValue();
                p.setVolume(100, 100);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static void release() {
        Collection mps = players.values();
        Iterator hmIterator = players.entrySet().iterator();
        while (hmIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry)hmIterator.next();
            MediaPlayer mp = (MediaPlayer) mapElement.getValue();
            try {
                if (mp != null) {
                    if (mp.isPlaying()) {
                        mp.stop();
                    }
                    mp.release();
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        mps.clear();
        if (currentMusic != -1) {
            previousMusic = currentMusic;
        }
        currentMusic = -1;
    }

}