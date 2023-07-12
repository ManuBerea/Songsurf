package com.example.songsurf.ui.playlist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.songsurf.R;
import com.example.songsurf.config.UrlProvider;
import com.example.songsurf.models.Playlist;
import com.example.songsurf.utils.adapters.PlaylistsArrayAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasePlaylistActivity extends AppCompatActivity {
    protected AdapterView<?> adapterView;
    protected GridView gridView;
    protected ListView listView;
    protected List<Playlist> playlists;
    protected List<Playlist> filteredPlaylists;
    protected ArrayAdapter<Playlist> adapter;
    protected EditText newPlaylistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());

        adapterView = findViewById(R.id.playlists_list);
        if (adapterView instanceof ListView) {
            listView = (ListView) adapterView;
        } else if (adapterView instanceof GridView) {
            gridView = (GridView) adapterView;
        }

        SearchView searchView = findViewById(R.id.search);
        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPlaylists(newText);
                return true;
            }
        });

        Button newPlaylistButton = findViewById(R.id.new_playlist_button);
        newPlaylistButton.setOnClickListener(view -> openNewPlaylistDialog());
    }

    protected abstract int getContentViewId();

    private void filterPlaylists(String text) {
        filteredPlaylists = new ArrayList<>();

        for (Playlist playlist : playlists) {
            if (playlist.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredPlaylists.add(playlist);
            }
        }

        adapter = new PlaylistsArrayAdapter(this, filteredPlaylists, isAddToPlaylistActivity());
        if (listView != null) {
            listView.setAdapter(adapter);
        } else if (gridView != null) {
            gridView.setAdapter(adapter);
        }
    }

    protected void fetchPlaylists() {
        String url = UrlProvider.PLAYLISTS_URL;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    playlists = new ArrayList<>();
                    filteredPlaylists = new ArrayList<>();
                    int firstEmptyPlaylistIndex = -1;

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jsonObject = response.getJSONObject(i);
                            int id = jsonObject.getInt("playlist_id");
                            String name = jsonObject.getString("name");
                            int songCount = jsonObject.getInt("song_count");
                            String youtubeSongId = jsonObject.optString("youtube_song_id");
                            if (songCount > 0) {
                                playlists.add(new Playlist(id, name, songCount, youtubeSongId));
                            }
                            else {
                                firstEmptyPlaylistIndex = i;
                                break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    playlists.sort(Comparator.comparingInt(Playlist::getId));

                    if (firstEmptyPlaylistIndex != -1) {
                        for (int i = firstEmptyPlaylistIndex; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);
                                int id = jsonObject.getInt("playlist_id");
                                String name = jsonObject.getString("name");
                                int songCount = jsonObject.getInt("song_count");
                                String youtubeSongId = jsonObject.optString("youtube_song_id");
                                playlists.add(new Playlist(id, name, songCount, youtubeSongId));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    adapter = new PlaylistsArrayAdapter(this, playlists, isAddToPlaylistActivity());
                    if (listView != null) {
                        listView.setAdapter(adapter);
                    } else if (gridView != null) {
                        gridView.setAdapter(adapter);
                    }

                    filterPlaylists("");
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
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
    }

    private void openNewPlaylistDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Create New Playlist");
        newPlaylistName = new EditText(this);
        alertDialogBuilder.setView(newPlaylistName);
        alertDialogBuilder.setPositiveButton("Create", (dialog, which) -> {
            String name = newPlaylistName.getText().toString();
            createNewPlaylist(name);
        });
        alertDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        alertDialogBuilder.show();
    }

    private void createNewPlaylist(String name) {
        String url = UrlProvider.PLAYLIST_URL;

        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        JSONObject json = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                response -> {
                    Toast.makeText(getApplicationContext(), "Playlist created successfully", Toast.LENGTH_LONG).show();
                    fetchPlaylists();
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
    public boolean isAddToPlaylistActivity() {
        return false;
    }

}
