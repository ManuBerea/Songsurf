package com.example.songsurf.ui.playlist;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.songsurf.R;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;

public class VideoPlayerActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayer youTubePlayer;
    private TextView songTitle;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private SeekBar seekBar;
    private ImageButton btnPlay;
    private ImageButton btnPause;
    private String songId;
    private ArrayList<String> songIdList;
    private ArrayList<String> songTitleList;
    private int currentSongPosition;
    private boolean isPlaying = false;
    private float videoDuration = 0f;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        youTubePlayerView = findViewById(R.id.youtube_player_view);
        songTitle = findViewById(R.id.tvSongName);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        seekBar = findViewById(R.id.seekBar);
        ImageButton btnPrev = findViewById(R.id.btnPrev);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        ImageButton btnNext = findViewById(R.id.btnNext);

        songId = getIntent().getStringExtra("youtubeSongId");
        songIdList = getIntent().getStringArrayListExtra("songIdList");
        currentSongPosition = getIntent().getIntExtra("currentSongPosition", 0);
        songTitleList = getIntent().getStringArrayListExtra("songTitleList");

        songTitle.setText(songTitleList.get(currentSongPosition));
        btnPrev.setOnClickListener(v -> selectSong("previous"));
        btnNext.setOnClickListener(v -> selectSong("next"));

        btnPlay.setVisibility(View.GONE);
        btnPlay.setOnClickListener(v -> togglePlayPause());
        btnPause.setOnClickListener(v -> togglePlayPause());

        seekBar.setOnSeekBarChangeListener(this);
        initYouTubePlayerView();
    }

    private void initYouTubePlayerView() {
        IFramePlayerOptions options = new IFramePlayerOptions.Builder().controls(0).rel(0).build();
        getLifecycle().addObserver(youTubePlayerView);
        youTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                VideoPlayerActivity.this.youTubePlayer = youTubePlayer;
                youTubePlayer.loadVideo(songId, 0);
            }

            @Override
            public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
                switch (state) {
                    case PLAYING:
                        isPlaying = true;
                        break;
                    case PAUSED:
                        isPlaying = false;
                        break;
                    case ENDED:
                        isPlaying = false;
                        selectSong("next");
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onVideoDuration(@NonNull YouTubePlayer player, float duration) {
                videoDuration = duration;
                seekBar.setMax((int) duration);
                String totalTime = formatTime(duration);
                tvTotalTime.setText(totalTime);
            }

            @Override
            public void onCurrentSecond(@NonNull YouTubePlayer player, float second) {
                seekBar.setProgress((int) second);
                String currentTime = formatTime(second);
                tvCurrentTime.setText(currentTime);
            }

            @Override
            public void onVideoLoadedFraction(@NonNull YouTubePlayer player, float loadedFraction) {
                seekBar.setSecondaryProgress((int) (loadedFraction * videoDuration));
            }
        }, true, options);
    }

    private void togglePlayPause() {
        if (youTubePlayer != null) {
            if (isPlaying) {
                youTubePlayer.pause();
                btnPlay.setVisibility(View.VISIBLE);
                btnPause.setVisibility(View.GONE);
            } else {
                youTubePlayer.play();
                btnPlay.setVisibility(View.GONE);
                btnPause.setVisibility(View.VISIBLE);
            }
        }
    }

    private void selectSong(String direction) {
        if (songIdList.isEmpty()) {
            Toast.makeText(this, "No songs in the playlist.", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("next".equals(direction)) {
            currentSongPosition = (currentSongPosition + 1) % songIdList.size();
        } else if ("previous".equals(direction)) {
            currentSongPosition = (currentSongPosition - 1 + songIdList.size()) % songIdList.size();
        }

        songId = songIdList.get(currentSongPosition);
        songTitle.setText(songTitleList.get(currentSongPosition));

        if (youTubePlayer != null) {
            youTubePlayer.loadVideo(songId, 0);
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            String currentTime = formatTime(progress);
            tvCurrentTime.setText(currentTime);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (youTubePlayer != null) {
            youTubePlayer.seekTo(0);
            seekBar.setProgress(0);
            String currentTime = formatTime(0);
            tvCurrentTime.setText(currentTime);
        }
    }


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (youTubePlayer != null) {
            youTubePlayer.seekTo(seekBar.getProgress());
        }
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(float timeInSeconds) {
        int minutes = (int) (timeInSeconds / 60);
        int seconds = (int) (timeInSeconds % 60);
        return String.format("%d:%02d", minutes, seconds);
    }
}
