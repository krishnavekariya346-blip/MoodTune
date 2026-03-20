package com.moodtune.app.models;

public class Song {
    public String videoId;
    public String title;
    public String artist;
    public String thumbnailUrl;
    public int duration; // in seconds
    public String audioUrl;

    public Song() {} // Empty constructor required for Firebase

    public Song(String videoId, String title, String artist, String thumbnailUrl, int duration) {
        this(videoId, title, artist, thumbnailUrl, duration, null);
    }

    public Song(String videoId, String title, String artist, String thumbnailUrl, int duration, String audioUrl) {
        this.videoId = videoId;
        this.title = title;
        this.artist = artist;
        this.thumbnailUrl = thumbnailUrl;
        this.duration = duration;
        this.audioUrl = audioUrl;
    }
}

