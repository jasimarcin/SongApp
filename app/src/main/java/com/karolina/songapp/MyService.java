package com.karolina.songapp;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.widget.SeekBar;

import java.io.IOException;


public class MyService extends Service {

    public interface PlayingStatusChange {
        void onPlayStatusChangeListener(int status);
    }

    public static final int IS_PLAYING = 1;
    public static final int IS_STOPPED = 2;
    public static final int IS_PAUSED = 3;

    public static final int DELAY_MILLIS = 50;
    public static final int START_PROGRESS = 0;

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private SeekBar seekBar;
    private final IBinder mBinder = (IBinder) new LocalBinder();

    private PlayingStatusChange playingStatusChange;
    private boolean isInited = false;
    private int isPlaying = IS_STOPPED;

    @Override
    public void onCreate() {
        handler = new Handler();
    }

    public class LocalBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // set new song
    // create media player if not exist
    public void setSong(Uri song) throws IOException {
        if (!initMediaPlayer(song)) {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(this, song);
            mediaPlayer.prepare();
        }

        mediaPlayer.setOnCompletionListener(getOnCompletionListener());
        mediaPlayer.setOnSeekCompleteListener(getOnSeekCompleteListener());
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(false);
        seekBar.setMax(mediaPlayer.getDuration());
    }

    @NonNull
    private MediaPlayer.OnSeekCompleteListener getOnSeekCompleteListener() {
        return new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                callOnFinishPlaying();
            }
        };
    }

    @NonNull
    private MediaPlayer.OnCompletionListener getOnCompletionListener() {
        return new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                callOnFinishPlaying();
            }
        };
    }

    private void callOnFinishPlaying() {
        setIsPlaying(IS_STOPPED);
        seekBar.setProgress(START_PROGRESS);
    }

    private boolean initMediaPlayer(Uri song) {
        if (mediaPlayer != null)
            return false;

        isInited = true;
        mediaPlayer = MediaPlayer.create(getApplicationContext(), song);

        return true;
    }

    public boolean playMusic() {
        if (!isInited)
            return false;

        handler.post(new Runnable() {
            public void run() {
                mediaPlayer.start();
                playCycle();
                MyService.this.setIsPlaying(IS_PLAYING);
            }
        });

        return true;
    }

    private void setIsPlaying(int isPlaying) {
        this.isPlaying = isPlaying;

        if (playingStatusChange != null)
            playingStatusChange.onPlayStatusChangeListener(isPlaying);
    }

    public void pauseMusic() {
        handler.post(new Runnable() {
            public void run() {
                try {
                    mediaPlayer.pause();
                    setIsPlaying(IS_PAUSED);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void stopMusic() {
        handler.post(new Runnable() {
            public void run() {
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
                setIsPlaying(IS_STOPPED);
            }
        });
    }

    public void setupSeekBar(SeekBar seekBar) {
        this.seekBar = seekBar;
        seekBar.setOnSeekBarChangeListener(getOnSeekBarChangeListener());
    }

    @NonNull
    private SeekBar.OnSeekBarChangeListener getOnSeekBarChangeListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekbar, int progress, boolean input) {
                if (input && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
    }

    public void playCycle() {
        seekBar.setProgress(mediaPlayer.getCurrentPosition());

        if (mediaPlayer.isPlaying()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    playCycle();
                }
            };
            handler.postDelayed(runnable, DELAY_MILLIS);
        }
    }

    public int isPlaying() {
        return isPlaying;
    }

    public void setPlayingStatusChange(PlayingStatusChange playingStatusChange) {
        this.playingStatusChange = playingStatusChange;
    }
}