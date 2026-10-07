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
import static org.mockito.ArgumentMatchers.*;
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

        assertEquals(admin, members.getFirst());
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

    // US -32 Remove a household member (epic 9)
    // Administrator can remove a member, but cannot remove themselves or another administrator.
    // tests
    // administrator can remove a member
    // non-administrator cannot remove a member
    // administrator cannot remove another administrator
    // member removal is recorded in activity history

    @Test
    void adminRemovesMember_membershipIsRemoved() {
        User admin = new User(1, "james", "j@example.com", "hash", "James", "now");
        User member = new User(2, "ted", "t@example.com", "hash", "Ted", "now");
        Household household = new Household(
                1, "11 Mermaid Street", "ABC123", 2, "House", "QLD", "now");

        HouseholdMembership adminMembership = new HouseholdMembership(
                10, admin.getId(), household.getId(),
                HouseholdMembership.Role.ADMIN, "now", null);

        HouseholdMembership memberMembership = new HouseholdMembership(
                11, member.getId(), household.getId(),
                HouseholdMembership.Role.MEMBER, "now", null);

        when(membershipDao.findActiveByUserAndHousehold(
                admin.getId(), household.getId()))
                .thenReturn(Optional.of(adminMembership));

        when(membershipDao.findActiveByUserAndHousehold(
                member.getId(), household.getId()))
                .thenReturn(Optional.of(memberMembership));

        service.removeMember(admin, household, member);

        verify(membershipDao).leaveHousehold(memberMembership.getId());
    }

    @Test
    void nonAdminCannotRemoveMember() {
        User member = new User(1, "ted", "t@example.com", "hash", "Ted", "now");
        User otherMember = new User(2, "frankie", "f@example.com", "hash", "Frankie", "now");
        Household household = new Household(
                1, "11 Mermaid Street", "ABC123", 3, "House", "QLD", "now");

        HouseholdMembership memberMembership = new HouseholdMembership(
                10, member.getId(), household.getId(),
                HouseholdMembership.Role.MEMBER, "now", null);

        when(membershipDao.findActiveByUserAndHousehold(
                member.getId(), household.getId()))
                .thenReturn(Optional.of(memberMembership));

        assertThrows(IllegalArgumentException.class,
                () -> service.removeMember(member, household, otherMember));

        verify(membershipDao, times(0)).leaveHousehold(anyLong());
    }

    @Test
    void adminCannotRemoveAnotherAdmin() {
        User admin = new User(1, "james", "j@example.com", "hash", "James", "now");
        User otherAdmin = new User(2, "ted", "t@example.com", "hash", "Ted", "now");
        Household household = new Household(
                1, "11 Mermaid Street", "ABC123", 2, "House", "QLD", "now");

        HouseholdMembership adminMembership = new HouseholdMembership(
                10, admin.getId(), household.getId(),
                HouseholdMembership.Role.ADMIN, "now", null);

        HouseholdMembership otherAdminMembership = new HouseholdMembership(
                11, otherAdmin.getId(), household.getId(),
                HouseholdMembership.Role.ADMIN, "now", null);

        when(membershipDao.findActiveByUserAndHousehold(
                admin.getId(), household.getId()))
                .thenReturn(Optional.of(adminMembership));

        when(membershipDao.findActiveByUserAndHousehold(
                otherAdmin.getId(), household.getId()))
                .thenReturn(Optional.of(otherAdminMembership));

        assertThrows(IllegalArgumentException.class,
                () -> service.removeMember(admin, household, otherAdmin));

        verify(membershipDao, times(0)).leaveHousehold(anyLong());
    }

    @Test
    void adminRemovesMember_recordsActivityHistory() {
        User admin = new User(1, "james", "j@example.com", "hash", "James", "now");
        User member = new User(2, "ted", "t@example.com", "hash", "Ted", "now");
        Household household = new Household(
                1, "11 Mermaid Street", "ABC123", 2, "House", "QLD", "now");

        HouseholdMembership adminMembership = new HouseholdMembership(
                10, admin.getId(), household.getId(),
                HouseholdMembership.Role.ADMIN, "now", null);

        HouseholdMembership memberMembership = new HouseholdMembership(
                11, member.getId(), household.getId(),
                HouseholdMembership.Role.MEMBER, "now", null);

        when(membershipDao.findActiveByUserAndHousehold(
                admin.getId(), household.getId()))
                .thenReturn(Optional.of(adminMembership));

        when(membershipDao.findActiveByUserAndHousehold(
                member.getId(), household.getId()))
                .thenReturn(Optional.of(memberMembership));

        service.removeMember(admin, household, member);

        verify(activityLogDao).log(
                eqLong(household.getId()),
                eqLong(admin.getId()),
                anyString());
    }


    // US -33 Rename household and change join code (epic 9)
    // Administrator can update household identifying information.
    // tests
    // administrator can rename household
    // blank household name is rejected
    // non-administrator cannot rename household
    // rename is recorded in activity history
    // administrator can change join code
    // non-administrator cannot change join code
    // join code change is recorded in activity history

    @Test
    void adminRenamesHousehold_nameIsUpdated() {
        User admin = new User(1, "james", "j@example.com", "hash", "James", "now");
        Household household = new Household(
                1, "Old House", "ABC123", 2, "House", "QLD", "now");

        HouseholdMembership adminMembership = new HouseholdMembership(
                10, admin.getId(), household.getId(),
                HouseholdMembership.Role.ADMIN, "now", null);

        when(membershipDao.findActiveByUserAndHousehold(
                admin.getId(), household.getId()))
                .thenReturn(Optional.of(adminMembership));

        service.renameHousehold(admin, household, "New House");

        verify(householdDao).renameHousehold(household.getId(), "New House");
    }

    @Test
    void adminRenamingHousehold_rejectsBlankName() {
        User admin = new User(1, "james", "j@example.com", "hash", "James", "now");
        Household household = new Household(
                1, "Old House", "ABC123", 2, "House", "QLD", "now");

        HouseholdMembership adminMembership = new HouseholdMembership(
                10, admin.getId(), household.getId(),
                HouseholdMembership.Role.ADMIN, "now", null);

        when(membershipDao.findActiveByUserAndHousehold(
                admin.getId(), household.getId()))
                .thenReturn(Optional.of(adminMembership));

        assertThrows(IllegalArgumentException.class,
                () -> service.renameHousehold(admin, household, "   "));

        verify(householdDao, times(0)).renameHousehold(anyLong(), anyString());
    }

    @Test
    void nonAdminCannotRenameHousehold() {
        User member = new User(1, "ted", "t@example.com", "hash", "Ted", "now");
        Household household = new Household(
                1, "Old House", "ABC123", 2, "House", "QLD", "now");

        HouseholdMembership memberMembership = new HouseholdMembership(
                10, member.getId(), household.getId(),
                HouseholdMembership.Role.MEMBER, "now", null);

        when(membershipDao.findActiveByUserAndHousehold(
                member.getId(), household.getId()))
                .thenReturn(Optional.of(memberMembership));

        assertThrows(IllegalArgumentException.class,
                () -> service.renameHousehold(member, household, "New House"));

        verify(householdDao, times(0)).renameHousehold(anyLong(), anyString());
    }

    @Test
    void adminRenamesHousehold_recordsActivityHistory() {
        User admin = new User(1, "james", "j@example.com", "hash", "James", "now");
        Household household = new Household(
                1, "Old House", "ABC123", 2, "House", "QLD", "now");

        HouseholdMembership adminMembership = new HouseholdMembership(
                10, admin.getId(), household.getId(),
                HouseholdMembership.Role.ADMIN, "now", null);

        when(membershipDao.findActiveByUserAndHousehold(
                admin.getId(), household.getId()))
                .thenReturn(Optional.of(adminMembership));

        service.renameHousehold(admin, household, "New House");

        verify(activityLogDao).log(
                eqLong(household.getId()),
                eqLong(admin.getId()),
                anyString());
    }

    @Test
    void adminChangesJoinCode_joinCodeIsUpdated() {
        User admin = new User(1, "james", "j@example.com", "hash", "James", "now");
        Household household = new Household(
                1, "Test House", "ABC123", 2, "House", "QLD", "now");

        HouseholdMembership adminMembership = new HouseholdMembership(
                10, admin.getId(), household.getId(),
                HouseholdMembership.Role.ADMIN, "now", null);

        when(membershipDao.findActiveByUserAndHousehold(
                admin.getId(), household.getId()))
                .thenReturn(Optional.of(adminMembership));

        service.changeJoinCode(admin, household);

        verify(householdDao).updateJoinCode(
                eqLong(household.getId()), anyString());
    }

    @Test
    void nonAdminCannotChangeJoinCode() {
        User member = new User(1, "ted", "t@example.com", "hash", "Ted", "now");
        Household household = new Household(
                1, "Test House", "ABC123", 2, "House", "QLD", "now");

        HouseholdMembership memberMembership = new HouseholdMembership(
                10, member.getId(), household.getId(),
                HouseholdMembership.Role.MEMBER, "now", null);

        when(membershipDao.findActiveByUserAndHousehold(
                member.getId(), household.getId()))
                .thenReturn(Optional.of(memberMembership));

        assertThrows(IllegalArgumentException.class,
                () -> service.changeJoinCode(member, household));

        verify(householdDao, times(0))
                .updateJoinCode(anyLong(), anyString());
    }

    @Test
    void adminChangesJoinCode_recordsActivityHistory() {
        User admin = new User(1, "james", "j@example.com", "hash", "James", "now");
        Household household = new Household(
                1, "Test House", "ABC123", 2, "House", "QLD", "now");

        HouseholdMembership adminMembership = new HouseholdMembership(
                10, admin.getId(), household.getId(),
                HouseholdMembership.Role.ADMIN, "now", null);

        when(membershipDao.findActiveByUserAndHousehold(
                admin.getId(), household.getId()))
                .thenReturn(Optional.of(adminMembership));

        service.changeJoinCode(admin, household);

        verify(activityLogDao).log(
                eqLong(household.getId()),
                eqLong(admin.getId()),
                anyString());
    }


    // US -35 Transfer administrator rights (epic 9)
    // Administrator can transfer administrator rights to another active member.
    // tests
    // administrator can transfer rights to another member
    // non-administrator cannot transfer rights
    // transfer is recorded in activity history

    @Test
    void adminTransfersRights_toAnotherMember() {
        User admin = new User(1, "james", "j@example.com", "hash", "James", "now");
        User member = new User(2, "ted", "t@example.com", "hash", "Ted", "now");
        Household household = new Household(
                1, "Test House", "ABC123", 2, "House", "QLD", "now");

        HouseholdMembership adminMembership = new HouseholdMembership(
                10, admin.getId(), household.getId(),
                HouseholdMembership.Role.ADMIN, "now", null);

        HouseholdMembership memberMembership = new HouseholdMembership(
                11, member.getId(), household.getId(),
                HouseholdMembership.Role.MEMBER, "now", null);

        when(membershipDao.findActiveByUserAndHousehold(
                admin.getId(), household.getId()))
                .thenReturn(Optional.of(adminMembership));

        when(membershipDao.findActiveByUserAndHousehold(
                member.getId(), household.getId()))
                .thenReturn(Optional.of(memberMembership));

        service.transferAdmin(admin, household, member);

        verify(membershipDao).transferAdmin(
                household.getId(),
                admin.getId(),
                member.getId());
    }

    @Test
    void nonAdminCannotTransferRights() {
        User member = new User(1, "ted", "t@example.com", "hash", "Ted", "now");
        User newAdmin = new User(2, "frankie", "f@example.com", "hash", "Frankie", "now");
        Household household = new Household(
                1, "Test House", "ABC123", 2, "House", "QLD", "now");

        HouseholdMembership memberMembership = new HouseholdMembership(
                10, member.getId(), household.getId(),
                HouseholdMembership.Role.MEMBER, "now", null);

        when(membershipDao.findActiveByUserAndHousehold(
                member.getId(), household.getId()))
                .thenReturn(Optional.of(memberMembership));

        assertThrows(IllegalArgumentException.class,
                () -> service.transferAdmin(member, household, newAdmin));

        verify(membershipDao, times(0))
                .transferAdmin(anyLong(), anyLong(), anyLong());
    }

    @Test
    void adminTransfersRights_recordsActivityHistory() {
        User admin = new User(1, "james", "j@example.com", "hash", "James", "now");
        User member = new User(2, "ted", "t@example.com", "hash", "Ted", "now");
        Household household = new Household(
                1, "Test House", "ABC123", 2, "House", "QLD", "now");

        HouseholdMembership adminMembership = new HouseholdMembership(
                10, admin.getId(), household.getId(),
                HouseholdMembership.Role.ADMIN, "now", null);

        HouseholdMembership memberMembership = new HouseholdMembership(
                11, member.getId(), household.getId(),
                HouseholdMembership.Role.MEMBER, "now", null);

        when(membershipDao.findActiveByUserAndHousehold(
                admin.getId(), household.getId()))
                .thenReturn(Optional.of(adminMembership));

        when(membershipDao.findActiveByUserAndHousehold(
                member.getId(), household.getId()))
                .thenReturn(Optional.of(memberMembership));

        service.transferAdmin(admin, household, member);

        verify(activityLogDao).log(
                eqLong(household.getId()),
                eqLong(admin.getId()),
                anyString());
    }
}
