package com.example.songsurf.ui.playlist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.songsurf.R;
import com.example.songsurf.config.UrlProvider;
import com.example.songsurf.models.Song;
import com.example.songsurf.utils.adapters.SongsArrayAdapter;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PlaylistActivity extends AppCompatActivity {
        private ArrayList<Song> songList = new ArrayList<>();
        private SongsArrayAdapter songAdapter;
        private RecyclerView rvSongs;
        private String playlistName;
        private int playlistId;
        private SongsArrayAdapter.OnSongListener onSongListener;
        private ImageView imgThumbnail;
        ArrayList<String> songIdList = new ArrayList<>();
        ArrayList<String> songTitleList = new ArrayList<>();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_playlist);

            playlistName = getIntent().getStringExtra("playlistName");
            playlistId = getIntent().getIntExtra("playlistId", -1);

            imgThumbnail = findViewById(R.id.playlist_thumbnail);
            ImageButton btnPlay = findViewById(R.id.btn_play);
            ImageButton btnSort = findViewById(R.id.btn_sort);
            ImageButton btnMore = findViewById(R.id.btn_more);
            TextView tvPlaylistName = findViewById(R.id.playlist_name);
            rvSongs = findViewById(R.id.rv_songs);

            tvPlaylistName.bringToFront();
            tvPlaylistName.setText(playlistName);

            songList = new ArrayList<>();
            btnSort.setOnClickListener(v -> showSortOptions());
            btnMore.setOnClickListener(v -> showMoreOptions());

            loadSongsFromPlaylist();

            onSongListener = new SongsArrayAdapter.OnSongListener() {
                @Override
                public void onSongClick(int position) {
                    Song clickedSong = songList.get(position);
                    Intent intent = new Intent(PlaylistActivity.this, VideoPlayerActivity.class);
                    intent.putExtra("youtubeSongId", clickedSong.getYoutubeSongId());
                    intent.putExtra("currentSongPosition", position);
                    intent.putExtra("currentSongTitle", clickedSong.getTitle());
                    intent.putStringArrayListExtra("songIdList", songIdList);
                    intent.putStringArrayListExtra("songTitleList", songTitleList);
                    startActivity(intent);
                }
                @Override
                public void onDeleteSongClick(int position) {
                    deleteSongFromPlaylist(position);
                    songIdList.remove(position);
                    songTitleList.remove(position);
                }
            };

            songAdapter = new SongsArrayAdapter(songList, onSongListener);
            rvSongs.setAdapter(songAdapter);
            rvSongs.setLayoutManager(new LinearLayoutManager(this));
            btnPlay.setOnClickListener(v -> {
                if(!songList.isEmpty()){
                    playPlaylist();
                } else {
                    Toast.makeText(getApplicationContext(), "No songs in the playlist.", Toast.LENGTH_SHORT).show();
                }
            });
        }

    private void loadSongsFromPlaylist() {
        String url = UrlProvider.PLAYLIST_URL + "/" + playlistId + "/songs";
        @SuppressLint("NotifyDataSetChanged") JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    songList = new ArrayList<>();
                    songIdList = new ArrayList<>();
                    songTitleList = new ArrayList<>();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jsonObject = response.getJSONObject(i);
                            int song_id = jsonObject.getInt("id");
                            String youtube_song_id = jsonObject.getString("youtube_song_id");
                            String artist_name = jsonObject.getString("artist_name");
                            String genre = jsonObject.getString("genre");
                            String language = jsonObject.getString("language");
                            String year = jsonObject.getString("year");
                            String rating = jsonObject.getString("rating");
                            String playlist_id = jsonObject.getString("playlist_id");
                            String title = jsonObject.getString("title");

                            songList.add(new Song(song_id, youtube_song_id, title, artist_name, genre, language, year, rating, playlist_id));
                            songIdList.add(youtube_song_id);
                            songTitleList.add(title);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("songList Adding time");
                    for(int i = 0; i < songList.size(); i++) {
                        System.out.println(songList.get(i).getTitle());
                    }

                    if (!songList.isEmpty()) {
                        String thumbnailUrl = "https://img.youtube.com/vi/" + songList.get(0).getYoutubeSongId() + "/0.jpg";
                        Picasso.get().load(thumbnailUrl).into(imgThumbnail);
                    }
                    songAdapter = new SongsArrayAdapter(songList, onSongListener);
                    rvSongs.setAdapter(songAdapter);
                    songAdapter.notifyDataSetChanged();
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


    private void playPlaylist() {
        Song clickedSong = songList.get(0);
        Intent intent = new Intent(PlaylistActivity.this, VideoPlayerActivity.class);
        intent.putExtra("youtubeSongId", clickedSong.getYoutubeSongId());
        intent.putExtra("currentSongPosition", 0);
        intent.putExtra("currentSongTitle", clickedSong.getTitle());
        intent.putStringArrayListExtra("songIdList", songIdList);
        intent.putStringArrayListExtra("songTitleList", songTitleList);
        startActivity(intent);
    }
    private void deleteSongFromPlaylist(int position) {
        int songId = songList.get(position).getSongId();
        String url = UrlProvider.PLAYLIST_URL + "/" + playlistId + "/remove_song/" + songId;

        StringRequest deleteRequest = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    songList.remove(position);
                    songAdapter.notifyItemRemoved(position);
                    Toast.makeText(getApplicationContext(), "Song removed from playlist", Toast.LENGTH_SHORT).show();

                    // Update thumbnail
                    if (!songList.isEmpty()) {
                        String thumbnailUrl = "https://img.youtube.com/vi/" + songList.get(0).getYoutubeSongId() + "/0.jpg";
                        Picasso.get().load(thumbnailUrl).into(imgThumbnail);
                    }
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
        requestQueue.add(deleteRequest);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void sortSongsAlphabetically() {
        List<Integer> indices = IntStream.range(0, songList.size()).boxed().sorted(Comparator.comparing(i -> songList.get(i).getTitle())).collect(Collectors.toList());

        List<String> sortedSongIdList = indices.stream().map(songIdList::get).collect(Collectors.toList());
        List<String> sortedSongTitleList = indices.stream().map(songTitleList::get).collect(Collectors.toList());

        songIdList = new ArrayList<>(sortedSongIdList);
        songTitleList = new ArrayList<>(sortedSongTitleList);

        songList.sort((s1, s2) -> s1.getTitle().compareToIgnoreCase(s2.getTitle()));
        System.out.println("songList Alphabetically");
        for(int i = 0; i < songList.size(); i++) {
            System.out.println(songList.get(i).getTitle());
        }

        if (!songList.isEmpty()) {
            String thumbnailUrl = "https://img.youtube.com/vi/" + songList.get(0).getYoutubeSongId() + "/0.jpg";
            Picasso.get().load(thumbnailUrl).into(imgThumbnail);
        }

        songAdapter.notifyDataSetChanged();
    }


    @SuppressLint("NotifyDataSetChanged")
    private void sortSongsRandomly() {

        List<Integer> indices = IntStream.range(0, songList.size()).boxed().collect(Collectors.toList());
        Collections.shuffle(indices);

        List<String> shuffledSongIdList = new ArrayList<>();
        List<String> shuffledSongTitleList = new ArrayList<>();
        List<Song> shuffledSongList = new ArrayList<>();

        for(Integer index : indices){
            shuffledSongIdList.add(songIdList.get(index));
            shuffledSongTitleList.add(songTitleList.get(index));
            shuffledSongList.add(songList.get(index));
        }

        songIdList = new ArrayList<>(shuffledSongIdList);
        songTitleList = new ArrayList<>(shuffledSongTitleList);
        songList = new ArrayList<>(shuffledSongList);

        System.out.println("songList Shuffle");
        for(int i = 0; i < songList.size(); i++) {
            System.out.println(songList.get(i).getTitle());
        }

        if (!songList.isEmpty()) {
            String thumbnailUrl = "https://img.youtube.com/vi/" + songList.get(0).getYoutubeSongId() + "/0.jpg";
            Picasso.get().load(thumbnailUrl).into(imgThumbnail);
        }

        songAdapter = new SongsArrayAdapter(songList, onSongListener);
        rvSongs.setAdapter(songAdapter);

        songAdapter.notifyDataSetChanged();
    }


    private void showSortOptions() {
        new AlertDialog.Builder(this)
                .setTitle("Sort options")
                .setItems(new String[]{"Alphabetically", "By adding time", "Shuffle"}, (dialog, which) -> {
                    if (which == 0) {
                        sortSongsAlphabetically();
                    } else if (which == 1) {
                        loadSongsFromPlaylist();
                    }
                    else {
                        sortSongsRandomly();
                    }
                })
                .show();
    }

    private void showMoreOptions() {
        new AlertDialog.Builder(this)
                .setTitle("More options")
                .setItems(new String[]{"Rename playlist", "Delete playlist"}, (dialog, which) -> {
                    if (which == 0) {
                        showRenameDialog();
                    } else {
                        showDeleteDialog();
                    }
                })
                .show();
    }

    private void showRenameDialog() {
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.text_input_dialog, (ViewGroup) findViewById(android.R.id.content), false);
        EditText input = viewInflated.findViewById(R.id.input);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(playlistName);

        new AlertDialog.Builder(this)
                .setTitle("Rename playlist")
                .setView(viewInflated)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> renamePlaylist(input.getText().toString()))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete playlist")
                .setMessage("Are you sure you want to delete this playlist?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deletePlaylist())
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void renamePlaylist(String newPlaylistName) {
        String url = UrlProvider.PLAYLIST_URL + "/" + playlistId + "/rename";

        JSONObject playlistData = new JSONObject();
        try {
            playlistData.put("name", newPlaylistName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, playlistData,
                response -> {
                    playlistName = newPlaylistName;
                    TextView tvPlaylistName = findViewById(R.id.playlist_name);
                    tvPlaylistName.setText(playlistName);
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
        requestQueue.add(jsonObjectRequest);
    }

    private void deletePlaylist() {
        String url = UrlProvider.PLAYLIST_URL + "/" + playlistId + "/delete";

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url,
                response -> finish(),
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
        requestQueue.add(stringRequest);
    }

}
