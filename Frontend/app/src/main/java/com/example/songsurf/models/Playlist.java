package com.example.songsurf.models;

public class Playlist {
    private final int id;
    private final String name;
    private final int songCount;
    private final String youtubeSongId;

    public Playlist(int id, String name, int songCount, String youtubeSongId) {
        this.id = id;
        this.name = name;
        this.songCount = songCount;
        this.youtubeSongId = youtubeSongId;
    }

    public int getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public int getSongCount() {
        return songCount;
    }

    public String getYoutubeSongId() {
        return youtubeSongId;
    }
}
