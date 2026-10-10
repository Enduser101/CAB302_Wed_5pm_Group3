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

    /** US-19: update an existing transport entry. */
    public TransportEntry updateEntry(User actor,
                                      Household household,
                                      long entryId,
                                      double publicTransportTripsPerWeek,
                                      double flightsPerYear) {

        if (publicTransportTripsPerWeek < 0) {
            throw new IllegalArgumentException(
                    "Public transport trips cannot be negative"
            );
        }

        if (flightsPerYear < 0) {
            throw new IllegalArgumentException(
                    "Flights per year cannot be negative"
            );
        }

        TransportEntry oldEntry = null;

        for (TransportEntry entry :
                transportEntryDao.findByHousehold(household.getId())) {

            if (entry.getId() == entryId) {
                oldEntry = entry;
                break;
            }
        }

        TransportEntry updatedEntry = transportEntryDao.update(
                entryId,
                publicTransportTripsPerWeek,
                flightsPerYear,
                actor.getId()
        );

        if (oldEntry != null) {
            activityLogDao.log(
                    household.getId(),
                    actor.getId(),
                    displayName(actor)
                            + " changed transport from "
                            + oldEntry.getPublicTransportTripsPerWeek()
                            + " PT trips/week and "
                            + oldEntry.getFlightsPerYear()
                            + " flights/year to "
                            + publicTransportTripsPerWeek
                            + " PT trips/week and "
                            + flightsPerYear
                            + " flights/year"
            );
        } else {
            activityLogDao.log(
                    household.getId(),
                    actor.getId(),
                    displayName(actor)
                            + " updated transport information for "
                            + updatedEntry.getPeriod()
            );
        }

        scoreService.recalculate(household.getId());

        return updatedEntry;
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

    /** US-19: update an existing household vehicle. */
    public Vehicle updateVehicle(User actor,
                                 Household household,
                                 long vehicleId,
                                 String label,
                                 String fuelType,
                                 double kmPerWeek) {

        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Vehicle label is required");
        }

        if (fuelType == null || fuelType.isBlank()) {
            throw new IllegalArgumentException("Fuel type is required");
        }

        if (kmPerWeek < 0) {
            throw new IllegalArgumentException(
                    "Kilometres per week cannot be negative"
            );
        }

        Vehicle oldVehicle = null;

        for (Vehicle vehicle : vehicleDao.findByHousehold(household.getId())) {
            if (vehicle.getId() == vehicleId) {
                oldVehicle = vehicle;
                break;
            }
        }

        Vehicle updatedVehicle = vehicleDao.update(
                vehicleId,
                label,
                fuelType,
                kmPerWeek
        );

        if (oldVehicle != null) {
            activityLogDao.log(
                    household.getId(),
                    actor.getId(),
                    displayName(actor)
                            + " changed vehicle "
                            + oldVehicle.getLabel()
                            + " (" + oldVehicle.getFuelType()
                            + ", " + oldVehicle.getKmPerWeek()
                            + " km/week) to "
                            + label
                            + " (" + fuelType
                            + ", " + kmPerWeek
                            + " km/week)"
            );
        } else {
            activityLogDao.log(
                    household.getId(),
                    actor.getId(),
                    displayName(actor) + " updated vehicle " + label
            );
        }

        scoreService.recalculate(household.getId());

        return updatedVehicle;
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
