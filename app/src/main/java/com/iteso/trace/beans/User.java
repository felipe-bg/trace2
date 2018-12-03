package com.iteso.trace.beans;

import java.util.HashMap;

public class User {
    private String displayName;
    private String email;
    private String currentConversation;
    private String avatar;
    private HashMap<String, Boolean> channels;
    private HashMap<String, Boolean> chats;

    public User() {
        // Initialize collections to avoid NullPointerException
        setChannels(new HashMap<String, Boolean>());
        setChats(new HashMap<String, Boolean>());
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCurrentConversation() {
        return currentConversation;
    }

    public void setCurrentConversation(String currentConversation) {
        this.currentConversation = currentConversation;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public HashMap<String, Boolean> getChannels() {
        return channels;
    }

    public void setChannels(HashMap<String, Boolean> channels) {
        this.channels = channels;
    }

    public HashMap<String, Boolean> getChats() {
        return chats;
    }

    public void setChats(HashMap<String, Boolean> chats) {
        this.chats = chats;
    }
}
