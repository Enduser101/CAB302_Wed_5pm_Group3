package com.ecotwin.dao;

public interface ActivityLogDao {
    void log(long householdId, Long actorUserId, String message);

    // US-36: delete household activity history
    void deleteByHousehold(long householdId);
}
