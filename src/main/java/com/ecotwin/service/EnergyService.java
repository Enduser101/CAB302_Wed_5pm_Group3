package com.ecotwin.service;

import com.ecotwin.dao.ActivityLogDao;
import com.ecotwin.dao.EnergyEntryDao;
import com.ecotwin.model.EnergyEntry;
import com.ecotwin.model.Household;
import com.ecotwin.model.User;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    /** US-19: update an existing energy entry. */
    public EnergyEntry updateEntry(User actor, Household household, long entryId,
                                   double electricityKwh, Double solarGenerationKwh, String notes) {
        if (electricityKwh < 0) {
            throw new IllegalArgumentException("Electricity usage cannot be negative");
        }

        if (solarGenerationKwh != null && solarGenerationKwh < 0) {
            throw new IllegalArgumentException("Solar generation cannot be negative");
        }

        EnergyEntry oldEntry = null;

        for (EnergyEntry entry : energyEntryDao.findByHousehold(household.getId())) {
            if (entry.getId() == entryId) {
                oldEntry = entry;
                break;
            }
        }

        EnergyEntry updatedEntry = energyEntryDao.update(
                entryId,
                electricityKwh,
                solarGenerationKwh,
                notes,
                actor.getId()
        );

        List<String> changes = new ArrayList<>();
        if (oldEntry != null && oldEntry.getElectricityKwh() != electricityKwh) {
            changes.add("energy usage from " + oldEntry.getElectricityKwh() + " kWh to " + electricityKwh + " kWh");
        }
        if (oldEntry != null && !Objects.equals(oldEntry.getSolarGenerationKwh(), solarGenerationKwh)) {
            changes.add("solar generation from " + solarText(oldEntry.getSolarGenerationKwh())
                    + " to " + solarText(solarGenerationKwh));
        }

        if (!changes.isEmpty()) {
            activityLogDao.log(
                    household.getId(),
                    actor.getId(),
                    displayName(actor) + " changed " + String.join(" and ", changes)
            );
        } else {
            activityLogDao.log(
                    household.getId(),
                    actor.getId(),
                    displayName(actor) + " updated energy usage for "
                            + updatedEntry.getPeriod()
            );
        }

        scoreService.recalculate(household.getId());

        return updatedEntry;
    }

    public List<EnergyEntry> findEntriesForHousehold(Household household) {
        return energyEntryDao.findByHousehold(household.getId());
    }

    private String solarText(Double kwh) {
        return kwh == null ? "none" : kwh + " kWh";
    }

    private String displayName(User user) {
        return user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
    }
}
