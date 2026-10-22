package com.ecotwin.service;

import com.ecotwin.dao.ActivityLogDao;
import com.ecotwin.dao.WaterEntryDao;
import com.ecotwin.model.Household;
import com.ecotwin.model.User;
import com.ecotwin.model.WaterEntry;

import java.time.YearMonth;
import java.util.List;

/**
 * Monthly household water readings, in litres (US-16, US-19, US-14).
 *
 * <p>Every change is written to the household's activity history and triggers a score
 * recalculation. Edits record the previous and new amount.
 */
public class WaterService {

    private final WaterEntryDao waterEntryDao;
    private final ActivityLogDao activityLogDao;
    private final ScoreService scoreService;

    /**
     * @param waterEntryDao  where water readings are stored
     * @param activityLogDao where history messages are written
     * @param scoreService   told to recalculate after every change
     */
    public WaterService(WaterEntryDao waterEntryDao, ActivityLogDao activityLogDao, ScoreService scoreService) {
        this.waterEntryDao = waterEntryDao;
        this.activityLogDao = activityLogDao;
        this.scoreService = scoreService;
    }

    /**
     * US-16: record the household's water usage for the current period.
     *
     * @param actor     the user recording the reading
     * @param household the household it belongs to
     * @param litres    water used this month, in litres; zero is a valid reading
     * @param notes     optional free-text notes
     * @return the saved entry, dated to the current month ({@code YYYY-MM})
     * @throws IllegalArgumentException if litres is negative
     */
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

    /**
     * US-19: update an existing water entry.
     *
     * <p>US-14: the history message shows the old and new amount, e.g. "changed water
     * usage from 10400.0 L to 9500.0 L".
     *
     * @param actor     the user making the edit
     * @param household the household the entry belongs to
     * @param entryId   the entry to change
     * @param litres    the corrected amount, in litres
     * @param notes     optional free-text notes
     * @return the updated entry
     * @throws IllegalArgumentException if litres is negative
     */
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

    /**
     * @param household the household to look up
     * @return all of the household's water readings
     */
    public List<WaterEntry> findEntriesForHousehold(Household household) {
        return waterEntryDao.findByHousehold(household.getId());
    }

    /**
     * @param user the user to name
     * @return their display name, or their username if they have not set one
     */
    private String displayName(User user) {
        return user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
    }
}
