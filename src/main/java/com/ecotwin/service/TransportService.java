package com.ecotwin.service;

import com.ecotwin.dao.ActivityLogDao;
import com.ecotwin.dao.TransportEntryDao;
import com.ecotwin.dao.VehicleDao;
import com.ecotwin.model.Household;
import com.ecotwin.model.TransportEntry;
import com.ecotwin.model.User;
import com.ecotwin.model.Vehicle;

import java.time.YearMonth;
import java.util.List;

public class TransportService {

    private final VehicleDao vehicleDao;
    private final TransportEntryDao transportEntryDao;
    private final ActivityLogDao activityLogDao;
    private final ScoreService scoreService;

    public TransportService(VehicleDao vehicleDao, TransportEntryDao transportEntryDao,
                             ActivityLogDao activityLogDao, ScoreService scoreService) {
        this.vehicleDao = vehicleDao;
        this.transportEntryDao = transportEntryDao;
        this.activityLogDao = activityLogDao;
        this.scoreService = scoreService;
    }

    /** US-18: record the household's transport information for the current period. */
    public TransportEntry recordEntry(User actor, Household household, double publicTransportTripsPerWeek,
                                       double flightsPerYear) {
        if (publicTransportTripsPerWeek < 0) {
            throw new IllegalArgumentException("Public transport trips cannot be negative");
        }
        if (flightsPerYear < 0) {
            throw new IllegalArgumentException("Flights per year cannot be negative");
        }

        String period = YearMonth.now().toString();
        TransportEntry entry = transportEntryDao.upsertForPeriod(household.getId(), period,
            publicTransportTripsPerWeek, flightsPerYear, actor.getId());
        activityLogDao.log(household.getId(), actor.getId(),
            displayName(actor) + " updated transport information for " + period);
        scoreService.recalculate(household.getId());
        return entry;
    }

    /** US-18: a household's vehicle list backs its transport score. */
    public Vehicle addVehicle(User actor, Household household, String label, String fuelType, double kmPerWeek) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Vehicle label is required");
        }
        if (fuelType == null || fuelType.isBlank()) {
            throw new IllegalArgumentException("Fuel type is required");
        }
        if (kmPerWeek < 0) {
            throw new IllegalArgumentException("Kilometres per week cannot be negative");
        }

        Vehicle vehicle = vehicleDao.create(household.getId(), label, fuelType, kmPerWeek);
        activityLogDao.log(household.getId(), actor.getId(), displayName(actor) + " added vehicle " + label);
        scoreService.recalculate(household.getId());
        return vehicle;
    }

    public List<Vehicle> findVehiclesForHousehold(Household household) {
        return vehicleDao.findByHousehold(household.getId());
    }

    public List<TransportEntry> findEntriesForHousehold(Household household) {
        return transportEntryDao.findByHousehold(household.getId());
    }

    private String displayName(User user) {
        return user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
    }
}
