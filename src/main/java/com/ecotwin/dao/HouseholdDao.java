package com.ecotwin.dao;

import com.ecotwin.model.Household;

import java.util.Optional;

public interface HouseholdDao {
    // US-08: creator becomes admin - HouseholdService creates the household then the admin membership.
    Household create(String name, int occupants, String dwellingType, String state, String joinCode);
    Optional<Household> findById(long id);
    Optional<Household> findByJoinCode(String joinCode); // US-09: join by code, invalid code -> empty

    // US-33: rename a household
    void renameHousehold(long householdId, String newName);
    
    // US-33: change the household join code
    void updateJoinCode(long householdId, String newJoinCode);
}
