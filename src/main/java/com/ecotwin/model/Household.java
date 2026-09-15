package com.ecotwin.model;

public class Household {

    private final long id;
    private final String name;
    private final String joinCode;
    private final int occupants;
    private final String dwellingType;
    private final String state;
    private final String createdAt;

    public Household(long id, String name, String joinCode, int occupants,
                      String dwellingType, String state, String createdAt) {
        this.id = id;
        this.name = name;
        this.joinCode = joinCode;
        this.occupants = occupants;
        this.dwellingType = dwellingType;
        this.state = state;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public int getOccupants() {
        return occupants;
    }

    public String getDwellingType() {
        return dwellingType;
    }

    public String getState() {
        return state;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
