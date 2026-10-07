package com.ecotwin.dao;

import com.ecotwin.model.Vehicle;

import java.util.List;

public interface VehicleDao {
    // US-18: a household's vehicle list backing the transport domain.
    Vehicle create(long householdId, String label, String fuelType, double kmPerWeek);

    // US-19: update an existing vehicle.
    Vehicle update(long vehicleId, String label, String fuelType, double kmPerWeek);

    List<Vehicle> findByHousehold(long householdId);
}
