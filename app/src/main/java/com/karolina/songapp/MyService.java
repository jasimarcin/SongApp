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

    public static final int DELAY_MILLIS = 1000;
    public static final int START_PROGRESS = 0;
    private Handler handler = new Handler();
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private final IBinder mBinder = (IBinder) new LocalBinder();
    Runnable runnable;

    private boolean isInited = false;
    private boolean isPlaying = false;

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
                callOnFInishPlaying();
            }
        };
    }

    @NonNull
    private MediaPlayer.OnCompletionListener getOnCompletionListener() {
        return new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                callOnFInishPlaying();
            }
        };
    }

    private void callOnFInishPlaying() {
        isPlaying = false;
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
                isPlaying = true;
            }
        });

        return true;
    }

    public void pauseMusic() {
        handler.post(new Runnable() {
            public void run() {

                try {
                    mediaPlayer.pause();
                    isPlaying = false;
                } catch (NullPointerException e) {
                }
            }
        });
    }

    public void stopMusic() {
        handler.post(new Runnable() {
            public void run() {
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
                isPlaying = false;
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
                if (input) {
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
            runnable = new Runnable() {
                @Override
                public void run() {
                    playCycle();
                }
            };
            handler.postDelayed(runnable, DELAY_MILLIS);
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}