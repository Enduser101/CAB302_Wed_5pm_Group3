package com.ecotwin.model;

public class User {

    private final long id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final String displayName;
    private final String createdAt;

    public User(long id, String username, String email, String passwordHash, String displayName, String createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
