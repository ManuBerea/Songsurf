package com.example.songsurf.ui.playlist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.songsurf.R;
import com.example.songsurf.config.UrlProvider;
import com.example.songsurf.models.Playlist;
import com.example.songsurf.utils.adapters.PlaylistsArrayAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddToPlaylistActivity extends BasePlaylistActivity {
    private int songId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        songId = intent.getIntExtra("song_id", -1);

        if (playlists == null) {
            playlists = new ArrayList<>();
        }

        PlaylistsArrayAdapter playlistAdapter = new PlaylistsArrayAdapter(this, playlists, true);

        ListView listView = findViewById(R.id.playlists_list);

        listView.setAdapter(playlistAdapter);

        adapterView.setOnItemClickListener((parent, view, position, id) -> {
            Playlist selectedPlaylist = (filteredPlaylists != null && !filteredPlaylists.isEmpty()) ? filteredPlaylists.get(position) : playlists.get(position);
            addSongToPlaylist(selectedPlaylist.getId());
        });

        fetchPlaylists();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_add_to_playlist;
    }

    private void addSongToPlaylist(int playlistId) {
        String url = UrlProvider.PLAYLIST_URL + "/" + playlistId + "/add";

        Map<String, String> params = new HashMap<>();
        params.put("song_id", String.valueOf(songId));

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> {
                    Toast.makeText(getApplicationContext(), "Song added to playlist successfully", Toast.LENGTH_LONG).show();
                    finish();
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String errorMessage = new JSONObject(new String(error.networkResponse.data)).getString("error");
                            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                SharedPreferences sharedPreferences = getSharedPreferences("com.example.songsurf", Context.MODE_PRIVATE);
                String jwt_token = sharedPreferences.getString("accessToken", null);
                headers.put("Authorization", "Bearer " + jwt_token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }
    @Override
    public boolean isAddToPlaylistActivity() {
        return true;
    }
}
