package com.iteso.trace.beans;

import java.util.HashMap;

public class Channel {
    private String name;
    private String description;
    private HashMap<String, Boolean> tags;

    public Channel() {
        // Initialize collection to avoid NullPointerException
        setTags(new HashMap<String, Boolean>());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashMap<String, Boolean> getTags() {
        return tags;
    }

    public void setTags(HashMap<String, Boolean> tags) {
        this.tags = tags;
    }
}
