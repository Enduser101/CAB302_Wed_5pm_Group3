package com.ecotwin.dao;

public interface ActivityLogDao {
    void log(long householdId, Long actorUserId, String message);
}
