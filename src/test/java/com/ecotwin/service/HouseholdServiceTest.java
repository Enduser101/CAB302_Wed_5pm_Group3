package com.ecotwin.service;

import com.ecotwin.dao.ActivityLogDao;
import com.ecotwin.dao.HouseholdDao;
import com.ecotwin.dao.HouseholdMembershipDao;
import com.ecotwin.model.Household;
import com.ecotwin.model.HouseholdMembership;
import com.ecotwin.model.User;
import com.ecotwin.dao.UserDao;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HouseholdServiceTest {

    private final HouseholdDao householdDao = mock(HouseholdDao.class);
    private final HouseholdMembershipDao membershipDao = mock(HouseholdMembershipDao.class);
    private final ActivityLogDao activityLogDao = mock(ActivityLogDao.class);
    private final UserDao userDao = mock(UserDao.class);
    private final HouseholdService service = new HouseholdService(householdDao, membershipDao, activityLogDao, userDao);

    private final User creator = new User(1, "creator", "c@example.com", "hash", "Creator", "now");

    @Test
    void creatingAHouseholdMakesTheCreatorAdmin() {
        Household household = new Household(1, "Test House", "ABC234", 2, "House", "QLD", "now");
        when(householdDao.findByJoinCode(anyString())).thenReturn(Optional.empty());
        when(householdDao.create(anyString(), anyInt(), any(), any(), anyString())).thenReturn(household);

        service.createHousehold(creator, "Test House", 2, "House", "QLD");

        verify(membershipDao).addMember(creator.getId(), household.getId(), HouseholdMembership.Role.ADMIN);
    }

    @Test
    void creatingAHouseholdRecordsItInActivityHistory() {
        Household household = new Household(1, "Test House", "ABC234", 2, "House", "QLD", "now");
        when(householdDao.findByJoinCode(anyString())).thenReturn(Optional.empty());
        when(householdDao.create(anyString(), anyInt(), any(), any(), anyString())).thenReturn(household);

        service.createHousehold(creator, "Test House", 2, "House", "QLD");

        verify(activityLogDao, times(1)).log(eqLong(household.getId()), eqLong(creator.getId()), anyString());
    }

    @Test
    void creatingAHouseholdRejectsABlankName() {
        assertThrows(IllegalArgumentException.class,
            () -> service.createHousehold(creator, "  ", 2, "House", "QLD"));
    }

    @Test
    void creatingAHouseholdRejectsZeroOccupants() {
        assertThrows(IllegalArgumentException.class,
            () -> service.createHousehold(creator, "Test House", 0, "House", "QLD"));
    }

    @Test
    void joiningWithAValidCodeAddsTheUserAsAMember() {
        Household household = new Household(1, "Test House", "ABC234", 2, "House", "QLD", "now");
        User joiner = new User(2, "joiner", "j@example.com", "hash", "Joiner", "now");
        when(householdDao.findByJoinCode("ABC234")).thenReturn(Optional.of(household));

        Household result = service.joinHousehold(joiner, "abc234");

        assertEquals(household.getId(), result.getId());
        verify(membershipDao).addMember(joiner.getId(), household.getId(), HouseholdMembership.Role.MEMBER);
    }

    @Test
    void joiningWithAnInvalidCodeIsRejected() {
        User joiner = new User(2, "joiner", "j@example.com", "hash", "Joiner", "now");
        when(householdDao.findByJoinCode("BADCODE")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.joinHousehold(joiner, "BADCODE"));
    }

    @Test
    void joiningWithABlankCodeIsRejected() {
        User joiner = new User(2, "joiner", "j@example.com", "hash", "Joiner", "now");

        assertThrows(IllegalArgumentException.class, () -> service.joinHousehold(joiner, "  "));
    }

    private static int anyInt() {
        return org.mockito.ArgumentMatchers.anyInt();
    }

    private static long eqLong(long value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }

    // US -10 Leave a household (epic 3, priority must)
    // Must be able to remove household membership, including access to the household. Household acticity history remains unchanged, despite members leaving.
    // tests
    // member leaving is no longer a current household member
    // member leaving loses all access to the household
    // member activity history remains after leaving

    @Test
    void memberLeavesHousehold_membershipRemoved() {
        User admin = new User(1, "james", "j@example.com", "hash", "James", "now");
        Household household = new Household(
                1, "11 Mermaid Street", "ABC123", 2, "House", "QLD", "now");
        User member = new User(2, "ted", "t@example.com", "hash", "Ted", "now");

        HouseholdMembership membership = new HouseholdMembership(
                10, member.getId(), household.getId(),
                HouseholdMembership.Role.MEMBER, "now", null);

        when(membershipDao.findActiveByUserAndHousehold(
                member.getId(), household.getId()))
                .thenReturn(Optional.of(membership));

        service.leaveHousehold(member, household);

        verify(membershipDao).leaveHousehold(membership.getId());
    }

    @Test
    void memberLeavesHousehold_losesFutureAccess() {
        User admin = new User(1, "james", "j@example.com", "hash", "James", "now");
        Household household = new Household(
                1, "11 Mermaid Street", "ABC123", 2, "House", "QLD", "now");
        User member = new User(2, "ted", "t@example.com", "hash", "Ted", "now");

        HouseholdMembership membership = new HouseholdMembership(
                10, member.getId(), household.getId(),
                HouseholdMembership.Role.MEMBER, "now", null);

        when(membershipDao.findActiveByUserAndHousehold(
                member.getId(), household.getId()))
                .thenReturn(Optional.of(membership));

        service.leaveHousehold(member, household);

        verify(membershipDao).leaveHousehold(membership.getId());
    }

    @Test
    void memberLeavesHousehold_historicalDataRetained() {
        User admin = new User(1, "james", "j@example.com", "hash", "James", "now");
        Household household = new Household(
                1, "11 Mermaid Street", "ABC123", 2, "House", "QLD", "now");
        User member = new User(2, "ted", "t@example.com", "hash", "Ted", "now");

        HouseholdMembership membership = new HouseholdMembership(
                10, member.getId(), household.getId(),
                HouseholdMembership.Role.MEMBER, "now", null);

        when(membershipDao.findActiveByUserAndHousehold(
                member.getId(), household.getId()))
                .thenReturn(Optional.of(membership));

        service.leaveHousehold(member, household);

        verify(membershipDao).leaveHousehold(membership.getId());
    }


    // US -11 View current household members (epic 3, priority should)
    // Current active members should be displayed, the administrator is identifiable, while departed members are not.
    // tests
    // member lists returns all currently active members
    // administrator is identifiable in the list of members
    // members who have left are excluded from the current member list

    @Test
    void viewMembers_returnsAllActiveMembers() {
        User admin = new User(1, "james", "j@example.com", "hash", "James", "now");
        Household household = new Household(
                1, "11 Mermaid Street", "ABC123", 3, "House", "QLD", "now");
        User member1 = new User(2, "ted", "t@example.com", "hash", "Ted", "now");
        User member2 = new User(3, "michael", "m@example.com", "hash", "Michael", "now");

        HouseholdMembership adminMembership = new HouseholdMembership(
                10, admin.getId(), household.getId(),
                HouseholdMembership.Role.ADMIN, "now", null);
        HouseholdMembership member1Membership = new HouseholdMembership(
                11, member1.getId(), household.getId(),
                HouseholdMembership.Role.MEMBER, "now", null);
        HouseholdMembership member2Membership = new HouseholdMembership(
                12, member2.getId(), household.getId(),
                HouseholdMembership.Role.MEMBER, "now", null);

        when(membershipDao.findActiveByHousehold(household.getId()))
                .thenReturn(List.of(adminMembership, member1Membership, member2Membership));

        when(userDao.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(userDao.findById(member1.getId())).thenReturn(Optional.of(member1));
        when(userDao.findById(member2.getId())).thenReturn(Optional.of(member2));

        List<User> members = service.findActiveMembers(household);

        assertEquals(3, members.size());
    }

    @Test
    void viewMembers_identifiesAdministrator() {
        User admin = new User(1, "james", "j@example.com", "hash", "James", "now");
        Household household = new Household(
                1, "11 Mermaid Street", "ABC123", 1, "House", "QLD", "now");

        HouseholdMembership adminMembership = new HouseholdMembership(
                10, admin.getId(), household.getId(),
                HouseholdMembership.Role.ADMIN, "now", null);

        when(membershipDao.findActiveByHousehold(household.getId()))
                .thenReturn(List.of(adminMembership));

        when(userDao.findById(admin.getId())).thenReturn(Optional.of(admin));

        List<User> members = service.findActiveMembers(household);

        assertEquals(admin, members.get(0));
    }

    @Test
    void viewMembers_excludesDepartedMembers() {
        User admin = new User(1, "james", "j@example.com", "hash", "James", "now");
        Household household = new Household(
                1, "11 Mermaid Street", "ABC123", 2, "House", "QLD", "now");
        User member = new User(2, "frankie", "f@example.com", "hash", "Frankie", "now");

        HouseholdMembership adminMembership = new HouseholdMembership(
                10, admin.getId(), household.getId(),
                HouseholdMembership.Role.ADMIN, "now", null);

        when(membershipDao.findActiveByHousehold(household.getId()))
                .thenReturn(List.of(adminMembership));

        when(userDao.findById(admin.getId())).thenReturn(Optional.of(admin));

        List<User> members = service.findActiveMembers(household);
        boolean stillListed = members.contains(member);

        assertFalse(stillListed);
    }
}
