package com.example.songsurf.ui.playlist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.Toast;

import com.example.songsurf.R;
import com.example.songsurf.models.Playlist;
import com.example.songsurf.ui.MainActivity;
import com.example.songsurf.ui.MyAccountActivity;
import com.example.songsurf.utils.adapters.PlaylistsArrayAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;


public class MyPlaylistsActivity extends BasePlaylistActivity {

    private PlaylistsArrayAdapter playlistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (playlists == null) {
            playlists = new ArrayList<>();
        }

        playlistAdapter = new PlaylistsArrayAdapter(this, playlists, false);

        GridView gridView = findViewById(R.id.playlists_list);

        gridView.setAdapter(playlistAdapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Playlist selectedPlaylist = (filteredPlaylists != null && !filteredPlaylists.isEmpty()) ? filteredPlaylists.get(position) : playlists.get(position);
            if (selectedPlaylist.getSongCount() == 0) {
                Toast.makeText(getApplicationContext(), "Playlist is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            int playlistId = selectedPlaylist.getId();
            String playlistName = selectedPlaylist.getName();
            Intent intent = new Intent(getApplicationContext(), PlaylistActivity.class);
            intent.putExtra("playlistId", playlistId);
            intent.putExtra("playlistName", playlistName);
            startActivity(intent);
        });

        showBottomNavigationMenu();
    }
    @SuppressLint("NonConstantResourceId")
    private void showBottomNavigationMenu() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.navigation_playlists);

        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.navigation_songsurf:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    return true;
                case R.id.navigation_playlists:
                    return true;
                case R.id.navigation_more:
                    startActivity(new Intent(getApplicationContext(), MyAccountActivity.class));
                    return true;
                default:
                    return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_playlists);

        fetchPlaylists();
        playlistAdapter.notifyDataSetChanged();

    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_my_playlists;
    }

}
