package com.moodtune.app.models;

import java.util.Date;

public class ChatMessage {
    public String text;
    public boolean isUser;
    public Date timestamp;

    public ChatMessage(String t, boolean u) {
        text = t;
        isUser = u;
        timestamp = new Date();
    }
    
    public ChatMessage(String t, boolean u, Date ts) {
        text = t;
        isUser = u;
        timestamp = ts;
    }
}

