package com.karolina.songapp;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.karolina.musicplayer.R;

import java.util.ArrayList;
import java.util.List;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MusicItemViewHolder> {

    private List<MusicItem> filesList;

    public MusicListAdapter() {
        this.filesList = new ArrayList<>();
    }

    @Override
    public MusicItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.music_list_row, parent, false);

        return new MusicItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MusicItemViewHolder holder, int position) {
        holder.bind(filesList.get(position));
    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }

    public void setFilesList(List<MusicItem> list) {
        if (list != null)
            filesList = list;
    }

    public class MusicItemViewHolder extends RecyclerView.ViewHolder {

        private TextView title;

        public MusicItemViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.music_row_title);
        }

        private void bind(MusicItem item) {
            title.setText(item.getName());
        }
    }
}
