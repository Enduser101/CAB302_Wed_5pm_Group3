
package com.ecotwin.dao;

import com.ecotwin.model.ActivityLogEntry;

import java.util.List;

public interface ActivityLogDao {
    void log(long householdId, Long actorUserId, String message);

    // US-36: delete household activity history
    void deleteByHousehold(long householdId);

    List<ActivityLogEntry> findByHousehold(long householdId);
}

