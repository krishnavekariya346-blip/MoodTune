package com.moodtune.app.music.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.moodtune.app.R;
import com.moodtune.app.models.Song;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    
    private List<Song> songs;
    private OnSongClickListener listener;
    private int userSongsCount;

    public interface OnSongClickListener {
        void onSongClick(int position);
    }

    public SongAdapter(List<Song> songs, OnSongClickListener listener) {
        this.songs = songs;
        this.listener = listener;
        this.userSongsCount = 0;
    }

    public void setUserSongsCount(int count) {
        this.userSongsCount = count;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.txtTitle.setText(song.title);
        holder.txtArtist.setText(song.artist);
        
        int minutes = song.duration / 60;
        int seconds = song.duration % 60;
        holder.txtDuration.setText(String.format("%d:%02d", minutes, seconds));
        
        if (position < userSongsCount) {
            holder.imgIndicator.setVisibility(View.VISIBLE);
            holder.imgIndicator.setImageResource(android.R.drawable.star_on);
            holder.txtSource.setText("Your Playlist");
        } else {
            holder.imgIndicator.setVisibility(View.VISIBLE);
            holder.imgIndicator.setImageResource(android.R.drawable.ic_menu_compass);
            holder.txtSource.setText("Suggested");
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSongClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtArtist, txtDuration, txtSource;
        ImageView imgIndicator;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtArtist = itemView.findViewById(R.id.txtArtist);
            txtDuration = itemView.findViewById(R.id.txtDuration);
            txtSource = itemView.findViewById(R.id.txtSource);
            imgIndicator = itemView.findViewById(R.id.imgIndicator);
        }
    }
}

