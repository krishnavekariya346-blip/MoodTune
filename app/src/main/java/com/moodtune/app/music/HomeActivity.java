package com.moodtune.app.music;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.moodtune.app.R;
import com.moodtune.app.models.Playlist;
import com.moodtune.app.models.Song;
import com.moodtune.app.music.services.PlaylistService;
import com.moodtune.app.music.adapters.SongAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private TextView txtWelcome, txtCurrentMood;
    private Button btnChangeMood;
    private PlayerView playerView;
    private RecyclerView recyclerViewPlaylist;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ExoPlayer exoPlayer;
    private List<Song> currentPlaylist;
    private int currentSongIndex = 0;
    private String userMood = "neutral";
    private final Set<String> failedTrackIds = new HashSet<>();
    private SongAdapter songAdapter;
    private int currentUserSongsCount = 0;
    private DefaultHttpDataSource.Factory httpDataSourceFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupFirebase();
        
        // If user is not signed in, redirect to sign in page
        if (currentUser == null) {
            finish();
            return;
        }

        // Get mood from intent
        String selectedMood = getIntent().getStringExtra("selected_mood");
        if (selectedMood != null) {
            userMood = selectedMood;
        }

        initializeViews();
        setupAudioPlayer();
        loadUserData();
    }

    private void initializeViews() {
        txtWelcome = findViewById(R.id.txtWelcome);
        txtCurrentMood = findViewById(R.id.txtCurrentMood);
        btnChangeMood = findViewById(R.id.btnChangeMood);
        playerView = findViewById(R.id.playerView);
        recyclerViewPlaylist = findViewById(R.id.recyclerViewPlaylist);

        recyclerViewPlaylist.setLayoutManager(new LinearLayoutManager(this));
        currentPlaylist = new ArrayList<>();

        btnChangeMood.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MoodSelectionActivity.class);
            startActivity(intent);
        });
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void setupAudioPlayer() {
        httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                .setUserAgent("MoodSound/1.0 (Android)")
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(10_000)
                .setReadTimeoutMs(15_000);

        exoPlayer = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(httpDataSourceFactory))
                .build();
        playerView.setPlayer(exoPlayer);
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    skipSong(null, false);
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                String details = null;
                if (error != null) {
                    StringBuilder builder = new StringBuilder(error.getErrorCodeName());
                    if (error.getMessage() != null && !error.getMessage().trim().isEmpty()) {
                        builder.append(" - ").append(error.getMessage().trim());
                    }
                    details = builder.toString();
                }
                handlePlaybackFailure(details);
            }
        });
    }

    private void loadUserData() {
        if (currentUser != null) {
            txtWelcome.setText("Welcome, " + currentUser.getDisplayName());
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String mood = documentSnapshot.getString("currentMood");
                            if (mood != null && !mood.isEmpty()) {
                                userMood = mood;
                            }
                        }
                        txtCurrentMood.setText("Current mood: " + userMood);
                        loadPlaylistForMood(userMood);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadPlaylistForMood(String mood) {
        loadMixedPlaylistForMood(mood);
    }

    private void loadMixedPlaylistForMood(String mood) {
        currentPlaylist.clear();
        failedTrackIds.clear();

        db.collection("playlists")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("mood", mood)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Song> userSongs = new ArrayList<>();

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Playlist playlist = doc.toObject(Playlist.class);
                            if (playlist != null && playlist.songs != null && !playlist.songs.isEmpty()) {
                                userSongs.addAll(playlist.songs);
                                break;
                            }
                        }
                    }

                    userSongs.removeIf(song -> song == null
                            || song.audioUrl == null
                            || song.audioUrl.trim().isEmpty());

                    Collections.shuffle(userSongs);
                    int userSongsToAdd = Math.min(5, userSongs.size());
                    List<Song> selectedUserSongs = userSongs.subList(0, userSongsToAdd);
                    currentPlaylist.addAll(selectedUserSongs);

                    loadGeneralSuggestionsForMood(mood, userSongsToAdd);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load user playlist", Toast.LENGTH_SHORT).show();
                    loadGeneralSuggestionsForMood(mood, 0);
                });
    }

    private void loadGeneralSuggestionsForMood(String mood, int userSongsCount) {
        PlaylistService playlistService = new PlaylistService(HomeActivity.this);
        playlistService.createGeneralPlaylistForMood(currentUser.getUid(), mood,
                new PlaylistService.PlaylistWithSongsCallback() {
                    @Override
                    public void onComplete(List<Song> suggestedSongs) {
                        List<Song> playableSongs = new ArrayList<>();
                        for (Song song : suggestedSongs) {
                            if (song.audioUrl != null && !song.audioUrl.isEmpty()) {
                                playableSongs.add(song);
                            }
                        }

                        Collections.shuffle(playableSongs);
                        int remainingSlots = 10 - currentPlaylist.size();
                        int addCount = Math.min(remainingSlots, playableSongs.size());

                        currentPlaylist.addAll(playableSongs.subList(0, addCount));

                        while (currentPlaylist.size() < 10 && !playableSongs.isEmpty()) {
                            currentPlaylist.add(playableSongs.get(
                                    (int) (Math.random() * playableSongs.size())
                            ));
                        }

                        setupPlaylistRecyclerViewWithUserCount(userSongsCount);
                        playCurrentSong();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(HomeActivity.this,
                                "Error loading general songs: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupPlaylistRecyclerViewWithUserCount(int userSongsCount) {
        currentUserSongsCount = userSongsCount;

        if (songAdapter == null) {
            songAdapter = new SongAdapter(currentPlaylist, this::playSelectedSong);
            recyclerViewPlaylist.setAdapter(songAdapter);
        }

        songAdapter.setUserSongsCount(userSongsCount);
        songAdapter.notifyDataSetChanged();
    }

    private void playSelectedSong(int position) {
        currentSongIndex = position;
        playCurrentSong();
    }

    private void playCurrentSong() {
        if (exoPlayer == null || currentPlaylist.isEmpty() || currentSongIndex >= currentPlaylist.size()) {
            return;
        }

        Song currentSong = currentPlaylist.get(currentSongIndex);
        if (currentSong == null) {
            skipSong("Missing track information", true);
            return;
        }

        if (currentSong.audioUrl == null || currentSong.audioUrl.trim().isEmpty()) {
            skipSong("This track has no audio stream", true);
            return;
        }

        String trackKey = !TextUtils.isEmpty(currentSong.audioUrl) ? currentSong.audioUrl : currentSong.videoId;
        if (!TextUtils.isEmpty(trackKey) && failedTrackIds.contains(trackKey)) {
            skipSong("Skipping previously failed track", true);
            return;
        }

        exoPlayer.stop();

        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(Uri.parse(currentSong.audioUrl))
                .setMimeType(MimeTypes.AUDIO_MPEG)
                .build();
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();

        PlaylistService playlistService = new PlaylistService();
        playlistService.savePlayedSong(currentUser.getUid(), userMood, currentSong);
    }

    private void handlePlaybackFailure(String rawMessage) {
        if (currentPlaylist != null && !currentPlaylist.isEmpty() && currentSongIndex < currentPlaylist.size()) {
            Song failedSong = currentPlaylist.get(currentSongIndex);
            if (failedSong != null) {
                String trackKey = !TextUtils.isEmpty(failedSong.audioUrl) ? failedSong.audioUrl : failedSong.videoId;
                if (!TextUtils.isEmpty(trackKey)) {
                    failedTrackIds.add(trackKey);
                }
            }
        }

        StringBuilder messageBuilder = new StringBuilder("Audio playback issue");
        if (rawMessage != null && !rawMessage.trim().isEmpty()) {
            messageBuilder.append(": ").append(rawMessage.trim());
        }

        skipSong(messageBuilder.toString(), true);
    }

    private void skipSong(String reason, boolean removeCurrent) {
        if (reason != null) {
            Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
        }

        if (exoPlayer != null) {
            exoPlayer.stop();
        }

        if (currentPlaylist == null || currentPlaylist.isEmpty()) {
            loadMixedPlaylistForMood(userMood);
            return;
        }

        if (removeCurrent && currentSongIndex < currentPlaylist.size()) {
            if (currentSongIndex < currentUserSongsCount) {
                currentUserSongsCount = Math.max(0, currentUserSongsCount - 1);
            }
            currentPlaylist.remove(currentSongIndex);
            if (songAdapter != null) {
                songAdapter.notifyItemRemoved(currentSongIndex);
                songAdapter.setUserSongsCount(currentUserSongsCount);
                songAdapter.notifyDataSetChanged();
            }
        }

        if (currentPlaylist.isEmpty()) {
            Toast.makeText(this, "Fetching new suggestions...", Toast.LENGTH_SHORT).show();
            loadMixedPlaylistForMood(userMood);
            return;
        }

        if (!removeCurrent) {
            currentSongIndex = (currentSongIndex + 1) % currentPlaylist.size();
        } else if (currentSongIndex >= currentPlaylist.size()) {
            currentSongIndex = 0;
        }

        int attempts = 0;
        int totalSongs = currentPlaylist.size();
        while (attempts < totalSongs) {
            Song candidate = currentPlaylist.get(currentSongIndex);
            if (candidate != null
                    && candidate.audioUrl != null
                    && !candidate.audioUrl.trim().isEmpty()
                    && !failedTrackIds.contains(candidate.audioUrl)) {
                playCurrentSong();
                return;
            }
            attempts++;
            currentSongIndex = (currentSongIndex + 1) % currentPlaylist.size();
        }

        Toast.makeText(this, "No playable tracks found. Refreshing playlist...", Toast.LENGTH_LONG).show();
        loadMixedPlaylistForMood(userMood);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playerView != null) {
            playerView.setPlayer(null);
        }
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}

