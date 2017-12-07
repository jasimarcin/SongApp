package com.karolina.songapp;

import android.Manifest;
import android.app.ActivityManager;
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
import android.widget.Toast;

import com.karolina.musicplayer.R;

import java.io.IOException;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;


public class MainActivity extends AppCompatActivity {

    private static final int READ_EXTERNAL_STORAGE_REQUEST = 32354;
    public static final int SELECT_FILE_TO_PLAY = R.string.select_file_to_play;
    public static final int PLAY = R.string.play;
    public static final int PAUSE = R.string.pause;

    private SeekBar seekBar;
    private Button playPause;
    private Button stop;
    private RecyclerView recyclerView;

    private MyService musicService;
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

        playPause.setOnClickListener(onPlayPauseClickListener());
        stop.setOnClickListener(onStopClickListener());

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
                    musicService.setSong(uri);
                    musicService.playMusic();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        requestForPermission(READ_EXTERNAL_STORAGE);
        bindPlayingService();
    }

    private void bindPlayingService() {
        Intent intent = new Intent(this, MyService.class);

        if (isMyServiceRunning(MyService.class))
            bindService(intent, serviceConnection, 0);
        else
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public View.OnClickListener onStopClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callStopMusic();
            }
        };
    }

    private void callStopMusic() {
        musicService.stopMusic();
        updatePlayPauseButton();
    }

    public View.OnClickListener onPlayPauseClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService.isPlaying() == MyService.IS_PLAYING) {
                    callPauseMusic();
                } else {
                    callPlayMusic();
                }
            }
        };
    }

    private void callPauseMusic() {
        musicService.pauseMusic();
    }

    private void callPlayMusic() {
        if (!musicService.playMusic()) {
            Toast.makeText(MainActivity.this, SELECT_FILE_TO_PLAY, Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePlayPauseButton() {
        if (musicService.isPlaying() == MyService.IS_PLAYING)
            playPause.setText(getString(PAUSE));
        else
            playPause.setText(getString(PLAY));
    }

    @Override
    protected void onDestroy() {
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
        }

        super.onDestroy();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            MainActivity.this.musicService = binder.getService();
            MainActivity.this.musicService.setupSeekBar(seekBar);
            MainActivity.this.musicService.setPlayingStatusChange(getPlayingStatusChange());
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @NonNull
    private MyService.PlayingStatusChange getPlayingStatusChange() {
        return new MyService.PlayingStatusChange() {
            @Override
            public void onPlayStatusChangeListener(int status) {
                updatePlayPauseButton();
            }
        };
    }

    public void requestForPermission(String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                ActivityCompat.requestPermissions(this, new String[]{permission}, READ_EXTERNAL_STORAGE_REQUEST);
            }
        } else {
            loadData();
        }
    }

    private void loadData() {
        musicListAdapter.setFilesList(musicFilesHelper.getFilesList());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                musicListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_REQUEST: {
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                } else {
                    loadData();
                }
            }
        }
    }

}


