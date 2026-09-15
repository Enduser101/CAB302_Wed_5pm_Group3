package com.ecotwin.dao;

import com.ecotwin.model.HouseholdMembership;

import java.util.Optional;

public interface HouseholdMembershipDao {
    HouseholdMembership addMember(long userId, long householdId, HouseholdMembership.Role role); // US-08, US-09
    Optional<HouseholdMembership> findActiveByUserAndHousehold(long userId, long householdId);
    Optional<HouseholdMembership> findAnyActiveByUser(long userId); // which household to land in after login
    void leaveHousehold(long membershipId); //US-10: leave a household
}
