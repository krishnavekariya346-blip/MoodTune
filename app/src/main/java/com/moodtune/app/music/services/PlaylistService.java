package com.moodtune.app.music.services;

import android.content.Context;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.moodtune.app.models.Playlist;
import com.moodtune.app.models.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistService {
    private static final String TAG = "PlaylistService";
    private final FirebaseFirestore db;
    private final Context context;

    public PlaylistService() {
        this.db = FirebaseFirestore.getInstance();
        this.context = null;
    }

    public PlaylistService(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.context = context;
    }

    public interface PlaylistWithSongsCallback {
        void onComplete(List<Song> songs);
        void onError(String error);
    }

    public void createGeneralPlaylistForMood(String userId, String mood, PlaylistWithSongsCallback callback) {
        if (context == null) {
            Log.w(TAG, "Context is null, cannot fetch Audius playlist");
            callback.onError("Context unavailable");
            return;
        }

        AudiusApiService audiusApiService = new AudiusApiService(context);
        audiusApiService.fetchTracksForMood(mood, new AudiusApiService.TracksCallback() {
            @Override
            public void onSuccess(List<Song> songs) {
                Log.d(TAG, "✅ Audius playlist fetched for mood: " + mood);
                List<Song> filtered = filterPlayableAudio(songs);
                Collections.shuffle(filtered);
                if (filtered.isEmpty()) {
                    callback.onError("Audius returned no streamable tracks");
                    return;
                }
                callback.onComplete(filtered);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to fetch Audius playlist: " + error);
                callback.onError(error);
            }
        });
    }

    private List<Song> filterPlayableAudio(List<Song> songs) {
        if (songs == null) return new ArrayList<>();
        List<Song> playable = new ArrayList<>();
        for (Song song : songs) {
            if (song != null && song.audioUrl != null && !song.audioUrl.trim().isEmpty()) {
                playable.add(song);
            }
        }
        return playable;
    }

    public void savePlayedSong(String userId, String mood, Song song) {
        db.collection("playlists")
                .whereEqualTo("userId", userId)
                .whereEqualTo("mood", mood)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Playlist existingPlaylist = doc.toObject(Playlist.class);
                            if (existingPlaylist != null && existingPlaylist.songs != null) {
                                boolean songExists = false;
                                for (Song existingSong : existingPlaylist.songs) {
                                    if (existingSong != null && song != null
                                            && existingSong.videoId != null
                                            && existingSong.videoId.equals(song.videoId)) {
                                        songExists = true;
                                        break;
                                    }
                                }

                                if (!songExists) {
                                    existingPlaylist.songs.add(song);
                                    existingPlaylist.updatedAt = System.currentTimeMillis();
                                    db.collection("playlists")
                                            .document(doc.getId())
                                            .set(existingPlaylist)
                                            .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Song added: " + song.title))
                                            .addOnFailureListener(e -> Log.e(TAG, "Failed to update playlist", e));
                                }
                            }
                            break;
                        }
                    } else {
                        List<Song> songs = new ArrayList<>();
                        songs.add(song);
                        Playlist newPlaylist = new Playlist(
                                mood + "_" + userId + "_" + System.currentTimeMillis(),
                                userId,
                                mood,
                                songs
                        );
                        db.collection("playlists").document(newPlaylist.playlistId)
                                .set(newPlaylist)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ New playlist created"))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to create new playlist", e));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to check existing playlists", e));
    }
}

