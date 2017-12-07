package com.karolina.songapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.karolina.musicplayer.R;

import java.io.IOException;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int READ_EXTERNAL_STORAGE_REQUEST = 32354;
    public static final int PLAY = R.string.play;
    public static final int PAUSE = R.string.pause;

    private SeekBar seekBar;
    private Button playPause;
    private Button stop;
    private RecyclerView recyclerView;

    private MyService service;
    private boolean bound = false;

    private MusicListAdapter musicListAdapter;
    private MusicFilesHelper musicFilesHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicListAdapter = new MusicListAdapter();
        musicFilesHelper = new MusicFilesHelper(this);

        initViews();
    }

    private void initViews() {
        seekBar = findViewById(R.id.seek_bar);
        playPause = findViewById(R.id.play_pause);
        stop = findViewById(R.id.stop);
        recyclerView = findViewById(R.id.recycler_view);

        playPause.setOnClickListener(this);
        stop.setOnClickListener(this);

        initRecyclerView();
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(musicListAdapter);
        musicListAdapter.setOnItemClick(getOnSongClickListener());
    }

    @NonNull
    private MusicListAdapter.OnItemClick getOnSongClickListener() {
        return new MusicListAdapter.OnItemClick() {
            @Override
            public void onItemClick(Uri uri) {
                try {
                    service.setSong(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        requestForPermission(READ_EXTERNAL_STORAGE);

        musicListAdapter.setFilesList(musicFilesHelper.getFilesList());

        Intent intent = new Intent(this, MyService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View view) {

        service.setSeekBar(seekBar);

        if (view == playPause) {

            if (MyService.isPlaying) {
                service.pauseMusic();
            } else {
                service.playMusic();
            }

        } else if (view == stop) {
            service.stopMusic();
        }

        updatePlayPauseButton();
    }

    private void updatePlayPauseButton() {
        if (MyService.isPlaying)
            playPause.setText(getString(PAUSE));
        else
            playPause.setText(getString(PLAY));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(mConnection);
            bound = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            MainActivity.this.service = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    public void requestForPermission(String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                ActivityCompat.requestPermissions(this, new String[]{permission}, READ_EXTERNAL_STORAGE_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_REQUEST: {
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
            }
        }
    }

}


