package com.ecotwin.service;

import com.ecotwin.dao.ActivityLogDao;
import com.ecotwin.dao.TransportEntryDao;
import com.ecotwin.dao.VehicleDao;
import com.ecotwin.model.Household;
import com.ecotwin.model.TransportEntry;
import com.ecotwin.model.User;
import com.ecotwin.model.Vehicle;
import java.util.List;
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
    void canEditTransportEntry() {
        TransportEntry updated = new TransportEntry(
                1,
                household.getId(),
                "2026-09",
                6.0,
                1.0,
                user.getId(),
                "later"
        );

        when(transportEntryDao.update(
                anyLong(),
                anyDouble(),
                anyDouble(),
                anyLong()
        )).thenReturn(updated);

        TransportEntry result = service.updateEntry(
                user,
                household,
                1,
                6.0,
                1.0
        );

        assertEquals(6.0, result.getPublicTransportTripsPerWeek());
        assertEquals(1.0, result.getFlightsPerYear());
    }

    @Test
    void editingTransportEntryRecalculatesScore() {
        when(transportEntryDao.update(
                anyLong(),
                anyDouble(),
                anyDouble(),
                anyLong()
        )).thenReturn(new TransportEntry(
                1,
                household.getId(),
                "2026-09",
                6.0,
                1.0,
                user.getId(),
                "later"
        ));

        service.updateEntry(
                user,
                household,
                1,
                6.0,
                1.0
        );

        verify(scoreService, times(1))
                .recalculate(household.getId());
    }

    @Test
    void negativeTransportEditIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateEntry(
                        user,
                        household,
                        1,
                        -1.0,
                        2.0
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateEntry(
                        user,
                        household,
                        1,
                        4.0,
                        -1.0
                )
        );

        verifyNoInteractions(
                transportEntryDao,
                activityLogDao,
                scoreService
        );
    }

    @Test
    void editingTransportEntryLogsPreviousValue() {
        TransportEntry oldEntry = new TransportEntry(
                1,
                household.getId(),
                "2026-09",
                4.0,
                2.0,
                user.getId(),
                "now"
        );

        TransportEntry updatedEntry = new TransportEntry(
                1,
                household.getId(),
                "2026-09",
                6.0,
                1.0,
                user.getId(),
                "later"
        );

        when(transportEntryDao.findByHousehold(household.getId()))
                .thenReturn(List.of(oldEntry));

        when(transportEntryDao.update(
                anyLong(),
                anyDouble(),
                anyDouble(),
                anyLong()
        )).thenReturn(updatedEntry);

        service.updateEntry(
                user,
                household,
                1,
                6.0,
                1.0
        );

        verify(activityLogDao).log(
                household.getId(),
                user.getId(),
                "Resident changed transport from 4.0 PT trips/week and 2.0 flights/year to "
                        + "6.0 PT trips/week and 1.0 flights/year"
        );
    }

    @Test
    void canEditVehicle() {
        Vehicle updated = new Vehicle(
                1,
                household.getId(),
                "Family Car",
                "Hybrid",
                100.0
        );

        when(vehicleDao.update(
                anyLong(),
                anyString(),
                anyString(),
                anyDouble()
        )).thenReturn(updated);

        Vehicle result = service.updateVehicle(
                user,
                household,
                1,
                "Family Car",
                "Hybrid",
                100.0
        );

        assertEquals("Family Car", result.getLabel());
        assertEquals("Hybrid", result.getFuelType());
        assertEquals(100.0, result.getKmPerWeek());
    }

    @Test
    void editingVehicleRecalculatesScore() {
        Vehicle updated = new Vehicle(
                1,
                household.getId(),
                "Family Car",
                "Hybrid",
                100.0
        );

        when(vehicleDao.update(
                anyLong(),
                anyString(),
                anyString(),
                anyDouble()
        )).thenReturn(updated);

        service.updateVehicle(
                user,
                household,
                1,
                "Family Car",
                "Hybrid",
                100.0
        );

        verify(scoreService, times(1))
                .recalculate(household.getId());
    }

    @Test
    void editingVehicleWithNegativeKmIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateVehicle(
                        user,
                        household,
                        1,
                        "Family Car",
                        "Petrol",
                        -10.0
                )
        );

        verifyNoInteractions(
                vehicleDao,
                activityLogDao,
                scoreService
        );
    }

    @Test
    void editingVehicleLogsPreviousValue() {
        Vehicle oldVehicle = new Vehicle(
                1,
                household.getId(),
                "Vehicle 1",
                "Petrol",
                150.0
        );

        Vehicle updatedVehicle = new Vehicle(
                1,
                household.getId(),
                "Family Car",
                "Hybrid",
                100.0
        );

        when(vehicleDao.findByHousehold(household.getId()))
                .thenReturn(List.of(oldVehicle));

        when(vehicleDao.update(
                anyLong(),
                anyString(),
                anyString(),
                anyDouble()
        )).thenReturn(updatedVehicle);

        service.updateVehicle(
                user,
                household,
                1,
                "Family Car",
                "Hybrid",
                100.0
        );

        verify(activityLogDao).log(
                household.getId(),
                user.getId(),
                "Resident changed vehicle Vehicle 1 (Petrol, 150.0 km/week) to "
                        + "Family Car (Hybrid, 100.0 km/week)"
        );
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
