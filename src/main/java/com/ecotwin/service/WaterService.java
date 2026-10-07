package com.ecotwin.service;

import com.ecotwin.dao.ActivityLogDao;
import com.ecotwin.dao.WaterEntryDao;
import com.ecotwin.model.Household;
import com.ecotwin.model.User;
import com.ecotwin.model.WaterEntry;

import java.time.YearMonth;
import java.util.List;

public class WaterService {

    private final WaterEntryDao waterEntryDao;
    private final ActivityLogDao activityLogDao;
    private final ScoreService scoreService;

    public WaterService(WaterEntryDao waterEntryDao, ActivityLogDao activityLogDao, ScoreService scoreService) {
        this.waterEntryDao = waterEntryDao;
        this.activityLogDao = activityLogDao;
        this.scoreService = scoreService;
    }

    /** US-16: record the household's water usage for the current period. */
    public WaterEntry recordEntry(User actor, Household household, double litres, String notes) {
        if (litres < 0) {
            throw new IllegalArgumentException("Water usage cannot be negative");
        }

        String period = YearMonth.now().toString();
        WaterEntry entry = waterEntryDao.create(household.getId(), period, litres, notes, actor.getId());
        activityLogDao.log(household.getId(), actor.getId(), displayName(actor) + " recorded water usage for " + period);
        scoreService.recalculate(household.getId());
        return entry;
    }

    public List<WaterEntry> findEntriesForHousehold(Household household) {
        return waterEntryDao.findByHousehold(household.getId());
    }

    private String displayName(User user) {
        return user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
    }
}
