package com.example.songsurf.utils.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.songsurf.R;
import com.example.songsurf.models.Song;
import com.squareup.picasso.Picasso;

import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class SongsArrayAdapter extends RecyclerView.Adapter<SongsArrayAdapter.ViewHolder> {

    private final List<Song> songList;
    private final OnSongListener onSongListener;

    public SongsArrayAdapter(List<Song> songList, OnSongListener onSongListener) {
        this.songList = songList;
        this.onSongListener = onSongListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item, parent, false);
        return new ViewHolder(view, onSongListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.tvTitle.setText(song.getTitle());

        String thumbnailUrl = "https://img.youtube.com/vi/" + song.getYoutubeSongId() + "/0.jpg";
        Picasso.get().load(thumbnailUrl).transform(new RoundedCornersTransformation(60, 0)).into(holder.imgThumbnail);

        holder.btnDelete.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView tvTitle;
        ImageView imgThumbnail;
        ImageButton btnDelete;
        OnSongListener onSongListener;

        public ViewHolder(@NonNull View itemView, OnSongListener onSongListener) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            imgThumbnail = itemView.findViewById(R.id.img_thumbnail);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            this.onSongListener = onSongListener;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            btnDelete.setOnClickListener(v -> onSongListener.onDeleteSongClick(getAdapterPosition()));
        }

        @Override
        public void onClick(View v) {
            onSongListener.onSongClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            if (btnDelete.getVisibility() == View.VISIBLE) {
                btnDelete.setVisibility(View.GONE);
            }
            else {
                btnDelete.setVisibility(View.VISIBLE);
            }
            return true;
        }
    }

    public interface OnSongListener {
        void onSongClick(int position);
        void onDeleteSongClick(int position);
    }

}
