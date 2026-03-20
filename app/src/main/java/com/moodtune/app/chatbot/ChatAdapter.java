package com.moodtune.app.chatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.moodtune.app.R;
import com.moodtune.app.models.ChatMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {
    List<ChatMessage> items;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
    
    public ChatAdapter(List<ChatMessage> items) { 
        this.items = items; 
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        ChatMessage m = items.get(position);
        
        // Show timestamp for first message or when time changes
        boolean showTimestamp = position == 0 || shouldShowTimestamp(position);
        holder.tvTimestamp.setVisibility(showTimestamp ? View.VISIBLE : View.GONE);
        
        if (showTimestamp) {
            Date messageDate = m.timestamp != null ? m.timestamp : new Date();
            if (isToday(messageDate)) {
                holder.tvTimestamp.setText(timeFormat.format(messageDate));
            } else {
                holder.tvTimestamp.setText(dateFormat.format(messageDate));
            }
        }
        
        if (m.isUser) {
            // User message
            holder.layoutBot.setVisibility(View.GONE);
            holder.layoutUser.setVisibility(View.VISIBLE);
            holder.tvUserMessage.setText(m.text);
            
            // Show time for user message
            Date messageDate = m.timestamp != null ? m.timestamp : new Date();
            holder.tvUserTime.setText(getRelativeTime(messageDate));
            holder.tvUserTime.setVisibility(View.VISIBLE);
            
        } else {
            // Bot message
            holder.layoutUser.setVisibility(View.GONE);
            holder.layoutBot.setVisibility(View.VISIBLE);
            holder.tvBotMessage.setText(m.text);
            
            // Show time for bot message
            Date messageDate = m.timestamp != null ? m.timestamp : new Date();
            holder.tvBotTime.setText(getRelativeTime(messageDate));
            holder.tvBotTime.setVisibility(View.VISIBLE);
        }
    }
    
    private boolean shouldShowTimestamp(int position) {
        if (position == 0) return true;
        ChatMessage current = items.get(position);
        ChatMessage previous = items.get(position - 1);
        
        if (current.timestamp == null || previous.timestamp == null) {
            return false;
        }
        
        // Show timestamp if more than 5 minutes difference
        long diff = current.timestamp.getTime() - previous.timestamp.getTime();
        return diff > 5 * 60 * 1000; // 5 minutes
    }
    
    private boolean isToday(Date date) {
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return sdf.format(date).equals(sdf.format(today));
    }
    
    private String getRelativeTime(Date date) {
        long diff = System.currentTimeMillis() - date.getTime();
        long minutes = diff / (60 * 1000);
        
        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + "m ago";
        } else if (isToday(date)) {
            return timeFormat.format(date);
        } else {
            return dateFormat.format(date);
        }
    }

    @Override
    public int getItemCount() { 
        return items.size(); 
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTimestamp;
        LinearLayout layoutBot, layoutUser;
        TextView tvBotName, tvBotMessage, tvBotTime;
        TextView tvUserMessage, tvUserTime;
        ImageView ivBotIcon, ivUserIcon;
        
        public VH(View v) {
            super(v);
            tvTimestamp = v.findViewById(R.id.tvTimestamp);
            layoutBot = v.findViewById(R.id.layoutBot);
            layoutUser = v.findViewById(R.id.layoutUser);
            tvBotName = v.findViewById(R.id.tvBotName);
            tvBotMessage = v.findViewById(R.id.tvBotMessage);
            tvBotTime = v.findViewById(R.id.tvBotTime);
            tvUserMessage = v.findViewById(R.id.tvUserMessage);
            tvUserTime = v.findViewById(R.id.tvUserTime);
            ivBotIcon = v.findViewById(R.id.ivBotIcon);
            ivUserIcon = v.findViewById(R.id.ivUserIcon);
        }
    }
}

