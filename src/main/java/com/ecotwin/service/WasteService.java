package com.ecotwin.service;

import com.ecotwin.dao.ActivityLogDao;
import com.ecotwin.dao.WasteEntryDao;
import com.ecotwin.model.Household;
import com.ecotwin.model.User;
import com.ecotwin.model.WasteEntry;

import java.time.YearMonth;
import java.util.List;

/**
 * Monthly household waste readings, split into general waste, recycling and compost, in
 * kilograms (US-17, US-19, US-14).
 *
 * <p>Every change is written to the household's activity history and triggers a score
 * recalculation. Edits record all three previous and new amounts.
 */
public class WasteService {

    private final WasteEntryDao wasteEntryDao;
    private final ActivityLogDao activityLogDao;
    private final ScoreService scoreService;

    /**
     * @param wasteEntryDao  where waste readings are stored
     * @param activityLogDao where history messages are written
     * @param scoreService   told to recalculate after every change
     */
    public WasteService(WasteEntryDao wasteEntryDao, ActivityLogDao activityLogDao, ScoreService scoreService) {
        this.wasteEntryDao = wasteEntryDao;
        this.activityLogDao = activityLogDao;
        this.scoreService = scoreService;
    }

    /**
     * US-17: record the household's waste and recycling for the current period.
     *
     * @param actor      the user recording the reading
     * @param household  the household it belongs to
     * @param generalKg  general (landfill) waste this month, in kg
     * @param recycledKg recycling this month, in kg
     * @param compostKg  compost this month, in kg
     * @return the saved entry, dated to the current month ({@code YYYY-MM})
     * @throws IllegalArgumentException if any amount is negative
     */
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

    /**
     * US-19: update an existing waste entry.
     *
     * <p>US-14: the history message lists all three old and new amounts, e.g. "changed waste
     * from general 27.0kg, recycled 12.0kg, compost 4.0kg to general 24.0kg, ...".
     *
     * @param actor      the user making the edit
     * @param household  the household the entry belongs to
     * @param entryId    the entry to change
     * @param generalKg  the corrected general waste, in kg
     * @param recycledKg the corrected recycling, in kg
     * @param compostKg  the corrected compost, in kg
     * @return the updated entry
     * @throws IllegalArgumentException if any amount is negative
     */
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

    /**
     * @param household the household to look up
     * @return all of the household's waste readings
     */
    public List<WasteEntry> findEntriesForHousehold(Household household) {
        return wasteEntryDao.findByHousehold(household.getId());
    }

    /**
     * @param user the user to name
     * @return their display name, or their username if they have not set one
     */
    private String displayName(User user) {
        return user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
    }
}
