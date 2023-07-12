package com.example.songsurf.models;

public class Song {
    private Integer song_id;
    private String youtube_song_id;
    private String title;
    private String artist_name;
    private String genre;
    private String language;
    private String year;
    private String rating;
    private String playlist_id;

    public Song(Integer song_id, String youtube_song_id, String title, String artist_name, String genre, String language, String year, String rating, String playlist_id) {
        this.song_id = song_id;
        this.youtube_song_id = youtube_song_id;
        this.title = title;
        this.artist_name = artist_name;
        this.genre = genre;
        this.language = language;
        this.year = year;
        this.rating = rating;
        this.playlist_id = playlist_id;
    }

    public String getYoutubeSongId() {
        return youtube_song_id;
    }

    public void setYoutubeSongId(String youtube_song_id) {
        this.youtube_song_id = youtube_song_id;
    }

    public Integer getSongId() {
        return song_id;
    }

    public void setSongId(Integer song_id) {
        this.song_id = song_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtistName() {
        return artist_name;
    }

    public void setArtistName(String artist_name) {
        this.artist_name = artist_name;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getPlaylistId() {
        return playlist_id;
    }

    public void setPlaylistId(String playlist_id) {
        this.playlist_id = playlist_id;
    }
}
