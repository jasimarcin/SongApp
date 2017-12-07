package com.karolina.songapp;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class MusicFilesHelper {

    private static final String SELECTION = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
    private static final String SORT_ORDER = MediaStore.Audio.Media.TITLE + " ASC";

    private static final Uri EXTERNAL_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final Uri INTERNAL_URI = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;

    private ContentResolver contentResolver;

    public MusicFilesHelper(Context context) {
        contentResolver = context.getContentResolver();
    }

    public List<MusicItem> getFilesList() {
        List<MusicItem> musicFilesList = new ArrayList<>();

        loadDataFromUri(musicFilesList, EXTERNAL_URI);
        loadDataFromUri(musicFilesList, INTERNAL_URI);

        return musicFilesList;
    }

    private void loadDataFromUri(List<MusicItem> musicFilesList, Uri uri) {
        Cursor externalCursor = contentResolver.query(uri, null, SELECTION, null, SORT_ORDER);

        if (externalCursor != null && externalCursor.getCount() > 0)
            while (externalCursor.moveToNext()) {
                String url = externalCursor.getString(externalCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = externalCursor.getString(externalCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                musicFilesList.add(new MusicItem(title, url));
            }
    }

}
