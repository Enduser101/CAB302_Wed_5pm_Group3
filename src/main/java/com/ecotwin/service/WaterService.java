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
    /** US-19: update an existing water entry. */
    public WaterEntry updateEntry(User actor, Household household, long entryId,
                                  double litres, String notes) {
        if (litres < 0) {
            throw new IllegalArgumentException("Water usage cannot be negative");
        }

        WaterEntry oldEntry = null;

        for (WaterEntry entry : waterEntryDao.findByHousehold(household.getId())) {
            if (entry.getId() == entryId) {
                oldEntry = entry;
                break;
            }
        }

        WaterEntry updatedEntry = waterEntryDao.update(
                entryId,
                litres,
                notes,
                actor.getId()
        );

        if (oldEntry != null) {
            activityLogDao.log(
                    household.getId(),
                    actor.getId(),
                    displayName(actor) + " changed water usage from "
                            + oldEntry.getLitres() + " L to "
                            + litres + " L"
            );
        } else {
            activityLogDao.log(
                    household.getId(),
                    actor.getId(),
                    displayName(actor) + " updated water usage for "
                            + updatedEntry.getPeriod()
            );
        }

        scoreService.recalculate(household.getId());

        return updatedEntry;
    }
    public List<WaterEntry> findEntriesForHousehold(Household household) {
        return waterEntryDao.findByHousehold(household.getId());
    }

    private String displayName(User user) {
        return user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
    }
}
