package com.karolina.songapp;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.SeekBar;

import com.karolina.musicplayer.R;


public class MyService extends Service {

    public static final int DELAY_MILLIS = 1000;
    private Handler handler = new Handler();
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private final IBinder mBinder = (IBinder) new LocalBinder();
    Runnable runnable;

    static boolean isPlaying = false;

    @Override
    public void onCreate() {

        handler = new Handler();

        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.hari);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(true);
    }

    public class LocalBinder extends Binder {
        MyService getService() {
            //zwracamy instancje serwisu, przez nią odwołamy się następnie do metod.
            return MyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //metoda którą zapewniamy.
    public void playMusic() {
        handler.post(new Runnable() {
            public void run() {
                mediaPlayer.start();
                playCycle();
                isPlaying = true;
            }
        });
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

    public void setSeekBar(SeekBar seekBar)
    {
        this.seekBar = seekBar;

        seekBar.setMax(mediaPlayer.getDuration());



        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekbar, int progress, boolean input) {
                if(input){
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    public void playCycle(){
        seekBar.setProgress(mediaPlayer.getCurrentPosition());

        if(mediaPlayer.isPlaying()){
            runnable = new Runnable() {
                @Override
                public void run() {
                    playCycle();
                }
            };
            handler.postDelayed(runnable, DELAY_MILLIS);
        }
    }

}