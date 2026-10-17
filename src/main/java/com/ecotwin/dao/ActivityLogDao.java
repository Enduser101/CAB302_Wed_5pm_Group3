
package com.ecotwin.dao;

import com.ecotwin.model.ActivityLogEntry;

import java.util.List;

public interface ActivityLogDao {
    void log(long householdId, Long actorUserId, String message);

    // US-36: delete household activity history
    void deleteByHousehold(long householdId);

    // US-34: update an activity history message
    void updateMessage(long entryId, long householdId, String message);

    // US-34: delete an individual activity history entry
    void deleteEntry(long entryId, long householdId);


    List<ActivityLogEntry> findByHousehold(long householdId);
}

