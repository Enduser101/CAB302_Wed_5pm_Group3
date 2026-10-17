package com.ecotwin.dao;

import com.ecotwin.model.ActivityLogEntry;

import java.util.List;

public interface ActivityLogDao {
    void log(long householdId, Long actorUserId, String message);

    List<ActivityLogEntry> findByHousehold(long householdId);
}