package com.ecotwin.service;

import com.ecotwin.dao.ActivityLogDao;
import com.ecotwin.dao.HouseholdDao;
import com.ecotwin.dao.HouseholdMembershipDao;
import com.ecotwin.model.Household;
import com.ecotwin.model.HouseholdMembership;
import com.ecotwin.model.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private final HouseholdService service = new HouseholdService(householdDao, membershipDao, activityLogDao);

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
}
