package com.ecotwin.model;

public class HouseholdMembership {

    public enum Role { ADMIN, MEMBER }

    private final long id;
    private final long userId;
    private final long householdId;
    private final Role role;
    private final String joinedAt;
    private final String leftAt; // null = currently active

    public HouseholdMembership(long id, long userId, long householdId, Role role,
                                String joinedAt, String leftAt) {
        this.id = id;
        this.userId = userId;
        this.householdId = householdId;
        this.role = role;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
    }

    public boolean isActive() {
        return leftAt == null;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public long getHouseholdId() {
        return householdId;
    }

    public Role getRole() {
        return role;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public String getLeftAt() {
        return leftAt;
    }
}
