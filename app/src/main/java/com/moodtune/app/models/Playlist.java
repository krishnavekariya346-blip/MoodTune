package com.moodtune.app.models;

import java.util.List;

public class Playlist {
    public String playlistId;
    public String userId;
    public String mood;
    public List<Song> songs;
    public long createdAt;
    public long updatedAt;

    public Playlist() {} // Empty constructor required for Firebase

    public Playlist(String playlistId, String userId, String mood, List<Song> songs) {
        this.playlistId = playlistId;
        this.userId = userId;
        this.mood = mood;
        this.songs = songs;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}

