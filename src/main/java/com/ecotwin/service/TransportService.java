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

/**
 * Household transport: monthly public transport and flight figures, plus the household's
 * vehicles (US-18, US-19, US-14).
 *
 * <p>Every change is written to the household's activity history and triggers a score
 * recalculation. Edits record the previous and new values.
 */
public class TransportService {

    private final VehicleDao vehicleDao;
    private final TransportEntryDao transportEntryDao;
    private final ActivityLogDao activityLogDao;
    private final ScoreService scoreService;

    /**
     * @param vehicleDao        where the household's vehicles are stored
     * @param transportEntryDao where monthly transport readings are stored
     * @param activityLogDao    where history messages are written
     * @param scoreService      told to recalculate after every change
     */
    public TransportService(VehicleDao vehicleDao, TransportEntryDao transportEntryDao,
                             ActivityLogDao activityLogDao, ScoreService scoreService) {
        this.vehicleDao = vehicleDao;
        this.transportEntryDao = transportEntryDao;
        this.activityLogDao = activityLogDao;
        this.scoreService = scoreService;
    }

    /**
     * US-18: record the household's transport information for the current period.
     * Transport has one reading per month, so recording again in the same month replaces
     * that month's figures rather than adding a second entry.
     *
     * @param actor                       the user recording the figures
     * @param household                   the household they belong to
     * @param publicTransportTripsPerWeek average public transport trips per week
     * @param flightsPerYear              flights taken per year
     * @return the saved entry for the current month ({@code YYYY-MM})
     * @throws IllegalArgumentException if either figure is negative
     */
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

    /**
     * US-19: update an existing transport entry.
     *
     * <p>US-14: the history message shows the old and new trips and flights.
     *
     * @param actor                       the user making the edit
     * @param household                   the household the entry belongs to
     * @param entryId                     the entry to change
     * @param publicTransportTripsPerWeek the corrected trips per week
     * @param flightsPerYear              the corrected flights per year
     * @return the updated entry
     * @throws IllegalArgumentException if either figure is negative
     */
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

    /**
     * US-18: add a vehicle to the household. Vehicles are listed on the Transport tab; the
     * transport score itself is currently based on trips and flights only.
     *
     * @param actor     the user adding the vehicle
     * @param household the household it belongs to
     * @param label     a name for the vehicle, e.g. "Family car"
     * @param fuelType  e.g. "Petrol", "Diesel", "Electric"
     * @param kmPerWeek average distance driven per week, in km
     * @return the saved vehicle
     * @throws IllegalArgumentException if the label or fuel type is blank, or the distance is negative
     */
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

    /**
     * US-19: update an existing household vehicle. The history message shows the old and
     * new label, fuel type and distance.
     *
     * @param actor     the user making the edit
     * @param household the household the vehicle belongs to
     * @param vehicleId the vehicle to change
     * @param label     the new name
     * @param fuelType  the new fuel type
     * @param kmPerWeek the new distance per week, in km
     * @return the updated vehicle
     * @throws IllegalArgumentException if the label or fuel type is blank, or the distance is negative
     */
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

    /**
     * @param household the household to look up
     * @return the household's vehicles
     */
    public List<Vehicle> findVehiclesForHousehold(Household household) {
        return vehicleDao.findByHousehold(household.getId());
    }

    /**
     * @param household the household to look up
     * @return all of the household's monthly transport readings
     */
    public List<TransportEntry> findEntriesForHousehold(Household household) {
        return transportEntryDao.findByHousehold(household.getId());
    }

    /**
     * @param user the user to name
     * @return their display name, or their username if they have not set one
     */
    private String displayName(User user) {
        return user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
    }
}
