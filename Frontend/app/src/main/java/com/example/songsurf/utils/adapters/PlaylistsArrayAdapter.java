package com.example.songsurf.utils.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.songsurf.R;
import com.example.songsurf.models.Playlist;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class PlaylistsArrayAdapter extends ArrayAdapter<Playlist> {
    private final boolean isAddToPlaylistActivity;

    public PlaylistsArrayAdapter(Context context, List<Playlist> playlists, boolean isAddToPlaylistActivity) {
        super(context, 0, playlists);
        this.isAddToPlaylistActivity = isAddToPlaylistActivity;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Playlist playlist = getItem(position);

        int layout = isAddToPlaylistActivity ? R.layout.item_playlist : R.layout.item_playlist2;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(layout, parent, false);
        }

        ImageView thumbnailImageView = convertView.findViewById(R.id.thumbnail);
        TextView nameTextView = convertView.findViewById(R.id.name);
        TextView songCountTextView = convertView.findViewById(R.id.song_count);
        String thumbnailUrl = "https://img.youtube.com/vi/" + playlist.getYoutubeSongId() + "/0.jpg";

        if (playlist.getSongCount() > 0) {
            Picasso.get().load(thumbnailUrl).transform(new RoundedCornersTransformation(60, 0)).into(thumbnailImageView);
        } else {
            Picasso.get().load(R.drawable.no_image_placeholder_rounded).resize(480, 360).centerCrop().transform(new RoundedCornersTransformation(60, 0)).into(thumbnailImageView);
        }

        nameTextView.setText(playlist.getName());

        String songCountText;
        if (playlist.getSongCount() == 1) {
            songCountText = playlist.getSongCount() + " song";
        }
        else if (playlist.getSongCount() == 0) {
            songCountText = "No songs";
        }
        else {
            songCountText = playlist.getSongCount() + " songs";
        }
        songCountTextView.setText(songCountText);

        return convertView;
    }

}
