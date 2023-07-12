package com.example.songsurf.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.songsurf.R;
import com.example.songsurf.config.UrlProvider;
import com.example.songsurf.models.Song;
import com.example.songsurf.ui.playlist.AddToPlaylistActivity;
import com.example.songsurf.ui.playlist.MyPlaylistsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerUtils;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private final int numberOfSeconds = 50;
    private final int BUFFER_SIZE = 3;
    private ArrayList<String> selectedGenres = null;
    private ArrayList<String> selectedLanguages = null;
    private ArrayList<String> selectedYears = null;
    private final Queue<Song> songsQueue = new LinkedList<>();
    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayer youTubePlayer;
    private GestureDetector gestureDetector;
    private final SwipeGestureDetector swipeGestureDetector = new SwipeGestureDetector();
    private ExecutorService songFetchExecutor;
    private final List<Song> songs = new ArrayList<>();
    private int currentSongIndex = 0;
    private TextView personalisedButton;
    private TextView recommendedButton;
    private static boolean isRecommendedButtonClicked = false;
    private ImageView likeButton;
    private ImageView dislikeButton;
    private ImageButton filtersButton;
    private TextView filtersLabel;
    private TextView likeButtonLabel;
    private TextView dislikeButtonLabel;
    private ImageButton addToPlaylistButton;
    private TextView songTitleTextView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        youTubePlayerView = findViewById(R.id.youtube_player_view);
        gestureDetector = new GestureDetector(this, swipeGestureDetector);

        personalisedButton = findViewById(R.id.personalised_button);
        recommendedButton = findViewById(R.id.recommended_button);

        likeButton = findViewById(R.id.like_button);
        dislikeButton = findViewById(R.id.dislike_button);

        filtersButton = findViewById(R.id.filters_button);
        filtersLabel = findViewById(R.id.filters_button_label);

        likeButtonLabel = findViewById(R.id.like_button_label);
        dislikeButtonLabel = findViewById(R.id.dislike_button_label);

        addToPlaylistButton = findViewById(R.id.add_to_playlist_button);

        songTitleTextView = findViewById(R.id.song_title);

        showBottomNavigationMenu();
        initButtons();
        initYouTubePlayerView();
    }

    private void initButtons() {
        filtersButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FilterActivity.class);
            filterActivityResultLauncher.launch(intent);
        });

        addToPlaylistButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddToPlaylistActivity.class);
            intent.putExtra("song_id", songs.get(currentSongIndex).getSongId());
            addToPlaylistActivityResultLauncher.launch(intent);
        });

        personalisedButton.setOnClickListener(v -> {
            resetSongFetchingState();
            personalisedButton.setTextColor(Color.WHITE);
            recommendedButton.setTextColor(Color.GRAY);
            filtersButton.setVisibility(View.VISIBLE);
            filtersLabel.setVisibility(View.VISIBLE);

            likeButton.setVisibility(View.GONE);
            dislikeButton.setVisibility(View.GONE);
            likeButtonLabel.setVisibility(View.GONE);
            dislikeButtonLabel.setVisibility(View.GONE);

            isRecommendedButtonClicked = false;
            for (int i = 0; i <= BUFFER_SIZE; i++) {
                fetchAndQueueSong();
            }
        });

        recommendedButton.setOnClickListener(v -> {
            resetSongFetchingState();
            recommendedButton.setTextColor(Color.WHITE);
            personalisedButton.setTextColor(Color.GRAY);
            filtersButton.setVisibility(View.GONE);
            filtersLabel.setVisibility(View.GONE);

            likeButton.setVisibility(View.VISIBLE);
            dislikeButton.setVisibility(View.VISIBLE);
            likeButtonLabel.setVisibility(View.VISIBLE);
            dislikeButtonLabel.setVisibility(View.VISIBLE);

            isRecommendedButtonClicked = true;
            fetchAndQueueSong();
        });

        likeButton.setOnClickListener(v -> {
            swipeGestureDetector.animateButton(likeButton, dislikeButton);
            swipeGestureDetector.onSwipeRight();
        });

        dislikeButton.setOnClickListener(v -> {
            swipeGestureDetector.animateButton(dislikeButton, likeButton);
            swipeGestureDetector.onSwipeLeft();
        });
    }

    private void resetSongFetchingState() {
        songsQueue.clear();
        songs.clear();
        currentSongIndex = 0;
    }

    @SuppressLint("NonConstantResourceId")
    private void showBottomNavigationMenu() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.navigation_songsurf);

        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.navigation_songsurf:
                    return true;
                case R.id.navigation_playlists:
                    startActivity(new Intent(getApplicationContext(), MyPlaylistsActivity.class));

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
        bottomNavigationView.setSelectedItemId(R.id.navigation_songsurf);
    }

    private void initYouTubePlayerView() {
        IFramePlayerOptions options = new IFramePlayerOptions.Builder().controls(0).rel(0).build();
        getLifecycle().addObserver(youTubePlayerView);

        youTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                MainActivity.this.youTubePlayer = youTubePlayer;

                songFetchExecutor = Executors.newSingleThreadExecutor();

                for (int i = 0; i <= BUFFER_SIZE; i++) {
                    fetchAndQueueSong();
                }
            }
        }, true, options);
    }

    private void fetchAndQueueSong() {
        if (isRecommendedButtonClicked) {
            songFetchExecutor.execute(() -> getRecommendedSong(this::addSongToQueues));
        } else {
            songFetchExecutor.execute(() -> fetchSong(createFilters()));
        }
    }
    private void fetchSong(Map<String, String> filters) {
        getSong(filters, this::addSongToQueues);
    }

    private void addSongToQueues(Song song) {
        songsQueue.add(song);
        songs.add(song);
        
        if (songs.size() == 1) {
            playNextSong();
        }
    }

    private void playNextSong() {
        if (!songsQueue.isEmpty()) {
            Song song = songsQueue.poll();

            assert song != null;
            String youtube_song_id = song.getYoutubeSongId();
            String song_title = song.getTitle();

            runOnUiThread(() -> {
                YouTubePlayerUtils.loadOrCueVideo(
                        youTubePlayer, getLifecycle(),
                        youtube_song_id, numberOfSeconds
                );
                songTitleTextView.setText(song_title);
            });
        }
    }

    private Map<String, String> createFilters() {
        Map<String, String> filters = new HashMap<>();

        if (selectedGenres != null && !selectedGenres.isEmpty()) {
            filters.put("genre", String.join(",", selectedGenres));
        }
        if (selectedLanguages != null && !selectedLanguages.isEmpty()) {
            filters.put("language", String.join(",", selectedLanguages));
        }
        if (selectedYears != null && !selectedYears.isEmpty()) {
            filters.put("year", String.join(",", selectedYears));
        }

        return filters;
    }

    private class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private static final int MIN_SWIPE_DISTANCE = 100;
        private static final int MAX_OFF_PATH = 250;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > MIN_SWIPE_DISTANCE && Math.abs(velocityX) > MAX_OFF_PATH) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                }
            }
            return true;
        }

        public void animateButton(ImageView buttonToAnimate, ImageView buttonToReset) {
            buttonToAnimate.animate()
                    .scaleX(1.5f)
                    .scaleY(1.5f)
                    .setDuration(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> {
                        buttonToAnimate.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(200)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .start();
                    })
                    .start();

            buttonToReset.setScaleX(1f);
            buttonToReset.setScaleY(1f);
        }

        public void onSwipeRight() {
            if (!songs.isEmpty()) {
                animateButton(likeButton, dislikeButton);
                if (isRecommendedButtonClicked) {
                    sendSongPreferenceToServer(songs.get(currentSongIndex).getSongId(), true);
                }

                playNextSong(1000);
            }
        }

        public void onSwipeLeft() {
            if (!songs.isEmpty()) {
                animateButton(dislikeButton, likeButton);
                if (isRecommendedButtonClicked) {
                    sendSongPreferenceToServer(songs.get(currentSongIndex).getSongId(), false);
                }

                playNextSong(-1000);
            }
        }

        public void playNextSong(int translateX) {
            currentSongIndex = (currentSongIndex + 1) % songs.size();
            Song song = songs.get(currentSongIndex);
            String youtube_song_id = song.getYoutubeSongId();
            String song_title = song.getTitle();

            youTubePlayerView.animate().translationX(translateX).setDuration(300).withEndAction(() -> {
                youTubePlayerView.setTranslationX(0);
                songTitleTextView.setText(song_title);

                fetchAndQueueSong();
            }).start();

            YouTubePlayerUtils.loadOrCueVideo(
                    youTubePlayer, getLifecycle(),
                    youtube_song_id, numberOfSeconds
            );
        }
    }

    public interface SongCallback {
        void onSongReceived(Song song);
    }

    private void getSong(Map<String, String> filters, SongCallback callback) {
        String url;

        if (filters.isEmpty()) {
            url = UrlProvider.SONG_URL;
        } else {
            url = UrlProvider.SONG_URL + "/filter";
            List<String> params = new ArrayList<>();
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                params.add(entry.getKey() + "=" + entry.getValue());
            }
            url += "?" + String.join("&", params);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Integer song_id = response.getInt("id");
                        String youtube_song_id = response.getString("youtube_song_id");
                        String title = response.getString("title");
                        String artist_name = response.getString("artist_name");
                        String genre = response.getString("genre");
                        String language = response.getString("language");
                        String year = response.getString("year");
                        String rating = response.getString("rating");
                        String playlist_id = response.getString("playlist_id");

                        Song song = new Song(song_id, youtube_song_id, title, artist_name, genre, language, year, rating, playlist_id);

                        callback.onSongReceived(song);
                    } catch (JSONException e) {
                        e.printStackTrace();
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
        requestQueue.add(jsonObjectRequest);
    }

    private void getRecommendedSong(SongCallback callback) {
        String url = UrlProvider.SONG_URL + "/recommend";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Integer song_id = response.getInt("id");
                        String youtube_song_id = response.getString("youtube_song_id");
                        String title = response.getString("title");
                        String artist_name = response.getString("artist_name");
                        String genre = response.getString("genre");
                        String language = response.getString("language");
                        String year = response.getString("year");
                        String rating = response.getString("rating");
                        String playlist_id = response.getString("playlist_id");
                        Song song = new Song(song_id, youtube_song_id, title, artist_name, genre, language, year, rating, playlist_id);
                        callback.onSongReceived(song);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(error.networkResponse.data));
                            if (jsonObject.has("error")) {
                                String errorMessage = jsonObject.getString("error");
                                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("Error", error.toString());
                        }
                    } else {
                        Log.e("Error", error.toString());
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

    private void sendSongPreferenceToServer(Integer songId, boolean liked) {
        String url = UrlProvider.SONG_URL + "/preference";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("song_id", songId);
            jsonBody.put("liked", liked);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        String message = response.getString("message");
                        Log.d("SongPreference", message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                try {
                    String errorMessage = new JSONObject(new String(error.networkResponse.data)).getString("error");
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }) {
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

    ActivityResultLauncher<Intent> filterActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        selectedGenres = data.getStringArrayListExtra("selectedGenres");
                        selectedLanguages = data.getStringArrayListExtra("selectedLanguages");
                        selectedYears = data.getStringArrayListExtra("selectedYears");

                        resetSongFetchingState();
                        fetchAndQueueSong();

                        for (int i = 0; i < BUFFER_SIZE; i++) {
                            fetchAndQueueSong();
                        }
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> addToPlaylistActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(MainActivity.this, "Song added to playlist successfully", Toast.LENGTH_LONG).show();
                }
            }
    );

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
