package com.karolina.songapp;


import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.karolina.musicplayer.R;

import java.util.ArrayList;
import java.util.List;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MusicItemViewHolder> {

    public interface OnItemClick {
        void onItemClick(Uri uri);
    }

    private List<MusicItem> filesList;
    private OnItemClick onItemClick;

    public MusicListAdapter() {
        this.filesList = new ArrayList<>();
    }

    @Override
    public MusicItemViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.music_list_row, parent, false);

        setOnRowClickListener(viewType, itemView);

        return new MusicItemViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void setOnRowClickListener(final int viewType, View itemView) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClick != null)
                    onItemClick.onItemClick(filesList.get(viewType).getUri());
            }
        });
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

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    // viewHolder pattern
    // for holding current view state
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
