package com.ecotwin.model;

public class ActivityLogEntry {

    private final long id;
    private final long householdId;
    private final Long actorUserId; // null = system-generated
    private final String message;
    private final String createdAt;

    public ActivityLogEntry(long id, long householdId, Long actorUserId, String message, String createdAt) {
        this.id = id;
        this.householdId = householdId;
        this.actorUserId = actorUserId;
        this.message = message;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getHouseholdId() {
        return householdId;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public String getMessage() {
        return message;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
