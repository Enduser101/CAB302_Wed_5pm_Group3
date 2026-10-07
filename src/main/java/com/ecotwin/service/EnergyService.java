package com.ecotwin.service;

import com.ecotwin.dao.ActivityLogDao;
import com.ecotwin.dao.EnergyEntryDao;
import com.ecotwin.model.EnergyEntry;
import com.ecotwin.model.Household;
import com.ecotwin.model.User;

import java.time.YearMonth;
import java.util.List;

public class EnergyService {

    private final EnergyEntryDao energyEntryDao;
    private final ActivityLogDao activityLogDao;
    private final ScoreService scoreService;

    public EnergyService(EnergyEntryDao energyEntryDao, ActivityLogDao activityLogDao, ScoreService scoreService) {
        this.energyEntryDao = energyEntryDao;
        this.activityLogDao = activityLogDao;
        this.scoreService = scoreService;
    }

    /** US-15: record the household's energy usage for the current period. */
    public EnergyEntry recordEntry(User actor, Household household, double electricityKwh, Double solarGenerationKwh,
                                    String notes) {
        if (electricityKwh < 0) {
            throw new IllegalArgumentException("Electricity usage cannot be negative");
        }
        if (solarGenerationKwh != null && solarGenerationKwh < 0) {
            throw new IllegalArgumentException("Solar generation cannot be negative");
        }

        String period = YearMonth.now().toString();
        EnergyEntry entry = energyEntryDao.create(household.getId(), period, electricityKwh, solarGenerationKwh,
            notes, actor.getId());
        activityLogDao.log(household.getId(), actor.getId(), displayName(actor) + " recorded energy usage for " + period);
        scoreService.recalculate(household.getId());
        return entry;
    }

    public List<EnergyEntry> findEntriesForHousehold(Household household) {
        return energyEntryDao.findByHousehold(household.getId());
    }

    private String displayName(User user) {
        return user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
    }
}
