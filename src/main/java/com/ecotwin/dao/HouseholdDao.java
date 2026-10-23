package com.ecotwin.dao;

import com.ecotwin.model.Household;

import java.util.Optional;

/**
 * Stores households (DAO / Repository pattern). Who belongs to a household is stored
 * separately, by {@link HouseholdMembershipDao}.
 */
public interface HouseholdDao {

    /**
     * US-08: creator becomes admin - HouseholdService creates the household then the admin membership.
     *
     * @param name         the household's name
     * @param occupants    how many people live there
     * @param dwellingType e.g. "House" or "Apartment"
     * @param state        the Australian state, e.g. "QLD"
     * @param joinCode     a code no other household has
     * @return the saved household, with its new id
     */
    Household create(String name, int occupants, String dwellingType, String state, String joinCode);

    /**
     * @param id the household's id
     * @return the matching household, or empty if there is none
     */
    Optional<Household> findById(long id);

    /**
     * US-09: join by code, invalid code -> empty.
     *
     * @param joinCode the exact code (callers upper-case it first)
     * @return the household with that code, or empty if none matches
     */
    Optional<Household> findByJoinCode(String joinCode);

    // US-33: rename a household
    void renameHousehold(long householdId, String newName);
    
    // US-33: change the household join code
    void updateJoinCode(long householdId, String newJoinCode);

    // US-36: delete a household
    void deleteHousehold(long householdId);
}
