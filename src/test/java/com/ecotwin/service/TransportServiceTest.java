package com.ecotwin.service;

import com.ecotwin.dao.ActivityLogDao;
import com.ecotwin.dao.TransportEntryDao;
import com.ecotwin.dao.VehicleDao;
import com.ecotwin.model.Household;
import com.ecotwin.model.TransportEntry;
import com.ecotwin.model.User;
import com.ecotwin.model.Vehicle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TransportServiceTest {

    private final VehicleDao vehicleDao = mock(VehicleDao.class);
    private final TransportEntryDao transportEntryDao = mock(TransportEntryDao.class);
    private final ActivityLogDao activityLogDao = mock(ActivityLogDao.class);
    private final ScoreService scoreService = mock(ScoreService.class);
    private final TransportService service =
        new TransportService(vehicleDao, transportEntryDao, activityLogDao, scoreService);

    private final User user = new User(1, "resident", "r@example.com", "hash", "Resident", "now");
    private final Household household = new Household(1, "Test House", "ABC234", 2, "House", "QLD", "now");

    @Test
    void recordingAValidEntryPersistsIt() {
        TransportEntry saved = new TransportEntry(1, household.getId(), "2026-09", 4.0, 2.0, user.getId(), "now");
        when(transportEntryDao.upsertForPeriod(anyLong(), anyString(), anyDouble(), anyDouble(), any())).thenReturn(saved);

        TransportEntry result = service.recordEntry(user, household, 4.0, 2.0);

        assertEquals(saved.getId(), result.getId());
        assertEquals(4.0, result.getPublicTransportTripsPerWeek());
    }

    @Test
    void recordingAnEntryLogsTheChange() {
        when(transportEntryDao.upsertForPeriod(anyLong(), anyString(), anyDouble(), anyDouble(), any()))
            .thenReturn(new TransportEntry(1, household.getId(), "2026-09", 4.0, 2.0, user.getId(), "now"));

        service.recordEntry(user, household, 4.0, 2.0);

        verify(activityLogDao, times(1)).log(eqLong(household.getId()), eqLong(user.getId()), anyString());
    }

    @Test
    void recordingAnEntryTriggersScoreRecalculation() {
        when(transportEntryDao.upsertForPeriod(anyLong(), anyString(), anyDouble(), anyDouble(), any()))
            .thenReturn(new TransportEntry(1, household.getId(), "2026-09", 4.0, 2.0, user.getId(), "now"));

        service.recordEntry(user, household, 4.0, 2.0);

        verify(scoreService, times(1)).recalculate(household.getId());
    }

    @Test
    void negativeTransportValuesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.recordEntry(user, household, -1.0, 2.0));
        assertThrows(IllegalArgumentException.class, () -> service.recordEntry(user, household, 4.0, -2.0));
        verifyNoInteractions(transportEntryDao, activityLogDao, scoreService);
    }

    @Test
    void addingAValidVehiclePersistsItAndLogsTheChange() {
        Vehicle saved = new Vehicle(1, household.getId(), "Vehicle 1", "petrol", 150.0);
        when(vehicleDao.create(anyLong(), anyString(), anyString(), anyDouble())).thenReturn(saved);

        Vehicle result = service.addVehicle(user, household, "Vehicle 1", "petrol", 150.0);

        assertEquals(saved.getId(), result.getId());
        verify(activityLogDao, times(1)).log(eqLong(household.getId()), eqLong(user.getId()), anyString());
        verify(scoreService, times(1)).recalculate(household.getId());
    }

    @Test
    void addingAVehicleWithABlankLabelIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.addVehicle(user, household, "  ", "petrol", 150.0));
        verifyNoInteractions(vehicleDao, activityLogDao, scoreService);
    }

    @Test
    void addingAVehicleWithNegativeKmPerWeekIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.addVehicle(user, household, "Vehicle 1", "petrol", -10.0));
        verifyNoInteractions(vehicleDao, activityLogDao, scoreService);
    }

    private static long eqLong(long value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
