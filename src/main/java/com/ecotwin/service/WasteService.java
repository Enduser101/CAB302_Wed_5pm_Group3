package com.ecotwin.service;

import com.ecotwin.dao.ActivityLogDao;
import com.ecotwin.dao.WasteEntryDao;
import com.ecotwin.model.Household;
import com.ecotwin.model.User;
import com.ecotwin.model.WasteEntry;

import java.time.YearMonth;
import java.util.List;

public class WasteService {

    private final WasteEntryDao wasteEntryDao;
    private final ActivityLogDao activityLogDao;
    private final ScoreService scoreService;

    public WasteService(WasteEntryDao wasteEntryDao, ActivityLogDao activityLogDao, ScoreService scoreService) {
        this.wasteEntryDao = wasteEntryDao;
        this.activityLogDao = activityLogDao;
        this.scoreService = scoreService;
    }

    /** US-17: record the household's waste and recycling for the current period. */
    public WasteEntry recordEntry(User actor, Household household, double generalKg, double recycledKg, double compostKg) {
        if (generalKg < 0 || recycledKg < 0 || compostKg < 0) {
            throw new IllegalArgumentException("Waste amounts cannot be negative");
        }

        String period = YearMonth.now().toString();
        WasteEntry entry = wasteEntryDao.create(household.getId(), period, generalKg, recycledKg, compostKg, actor.getId());
        activityLogDao.log(household.getId(), actor.getId(), displayName(actor) + " recorded waste for " + period);
        scoreService.recalculate(household.getId());
        return entry;
    }
    /** US-19: update an existing waste entry. */
    public WasteEntry updateEntry(User actor, Household household, long entryId,
                                  double generalKg, double recycledKg, double compostKg) {

        if (generalKg < 0 || recycledKg < 0 || compostKg < 0) {
            throw new IllegalArgumentException("Waste amounts cannot be negative");
        }

        WasteEntry oldEntry = null;

        for (WasteEntry entry : wasteEntryDao.findByHousehold(household.getId())) {
            if (entry.getId() == entryId) {
                oldEntry = entry;
                break;
            }
        }

        WasteEntry updatedEntry = wasteEntryDao.update(
                entryId,
                generalKg,
                recycledKg,
                compostKg,
                actor.getId()
        );

        if (oldEntry != null) {
            activityLogDao.log(
                    household.getId(),
                    actor.getId(),
                    displayName(actor) + " changed waste from general "
                            + oldEntry.getGeneralKg() + "kg, recycled "
                            + oldEntry.getRecycledKg() + "kg, compost "
                            + oldEntry.getCompostKg() + "kg to general "
                            + generalKg + "kg, recycled "
                            + recycledKg + "kg, compost "
                            + compostKg + "kg"
            );
        } else {
            activityLogDao.log(
                    household.getId(),
                    actor.getId(),
                    displayName(actor) + " updated waste for "
                            + updatedEntry.getPeriod()
            );
        }

        scoreService.recalculate(household.getId());

        return updatedEntry;
    }

    public List<WasteEntry> findEntriesForHousehold(Household household) {
        return wasteEntryDao.findByHousehold(household.getId());
    }

    private String displayName(User user) {
        return user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
    }
}
