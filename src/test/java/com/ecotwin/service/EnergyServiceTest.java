package com.ecotwin.service;

import com.ecotwin.dao.ActivityLogDao;
import com.ecotwin.dao.EnergyEntryDao;
import com.ecotwin.model.EnergyEntry;
import com.ecotwin.model.Household;
import com.ecotwin.model.User;
import org.junit.jupiter.api.Test;
import java.util.List;

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

class EnergyServiceTest {

    private final EnergyEntryDao energyEntryDao = mock(EnergyEntryDao.class);
    private final ActivityLogDao activityLogDao = mock(ActivityLogDao.class);
    private final ScoreService scoreService = mock(ScoreService.class);
    private final EnergyService service = new EnergyService(energyEntryDao, activityLogDao, scoreService);

    private final User user = new User(1, "resident", "r@example.com", "hash", "Resident", "now");
    private final Household household = new Household(1, "Test House", "ABC234", 2, "House", "QLD", "now");

    @Test
    void recordingAValidEntryPersistsIt() {
        EnergyEntry saved = new EnergyEntry(1, household.getId(), "2026-09", 120.0, 15.0, null, user.getId(), "now");
        when(energyEntryDao.create(anyLong(), anyString(), anyDouble(), any(), any(), any())).thenReturn(saved);

        EnergyEntry result = service.recordEntry(user, household, 120.0, 15.0, null);

        assertEquals(saved.getId(), result.getId());
        assertEquals(120.0, result.getElectricityKwh());
    }

    @Test
    void recordingAnEntryLogsTheChange() {
        when(energyEntryDao.create(anyLong(), anyString(), anyDouble(), any(), any(), any()))
            .thenReturn(new EnergyEntry(1, household.getId(), "2026-09", 120.0, null, null, user.getId(), "now"));

        service.recordEntry(user, household, 120.0, null, null);

        verify(activityLogDao, times(1)).log(eqLong(household.getId()), eqLong(user.getId()), anyString());
    }

    @Test
    void recordingAnEntryTriggersScoreRecalculation() {
        when(energyEntryDao.create(anyLong(), anyString(), anyDouble(), any(), any(), any()))
            .thenReturn(new EnergyEntry(1, household.getId(), "2026-09", 120.0, null, null, user.getId(), "now"));

        service.recordEntry(user, household, 120.0, null, null);

        verify(scoreService, times(1)).recalculate(household.getId());
    }

    @Test
    void negativeElectricityUsageIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.recordEntry(user, household, -1.0, null, null));
        verifyNoInteractions(energyEntryDao, activityLogDao, scoreService);
    }

    @Test
    void negativeSolarGenerationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.recordEntry(user, household, 100.0, -5.0, null));
        verifyNoInteractions(energyEntryDao, activityLogDao, scoreService);
    }

    @Test
    void canEditEnergyEntry() {
        EnergyEntry updatedEntry = new EnergyEntry(
                1,
                household.getId(),
                "2026-09",
                100.0,
                20.0,
                "Updated usage",
                user.getId(),
                "now"
        );

        when(energyEntryDao.update(anyLong(), anyDouble(), any(), any(), anyLong()))
                .thenReturn(updatedEntry);

        EnergyEntry result = service.updateEntry(
                user,
                household,
                1,
                100.0,
                20.0,
                "Updated usage"
        );

        assertEquals(100.0, result.getElectricityKwh());
    }

    @Test
    void editingEnergyEntryRecalculatesScore() {
        EnergyEntry updatedEntry = new EnergyEntry(
                1, household.getId(), "2026-09",
                100.0, 20.0, "Updated usage", user.getId(), "now"
        );

        when(energyEntryDao.update(anyLong(), anyDouble(), any(), any(), anyLong()))
                .thenReturn(updatedEntry);

        service.updateEntry(
                user, household, 1,
                100.0, 20.0, "Updated usage"
        );

        verify(scoreService).recalculate(household.getId());
    }

    @Test
    void negativeElectricityEditIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                service.updateEntry(
                        user, household, 1,
                        -50.0, 20.0, "Updated usage"
                )
        );
    }

    @Test
    void negativeSolarEditIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                service.updateEntry(
                        user, household, 1,
                        100.0, -20.0, "Updated usage"
                )
        );
    }

    @Test
    void editingEnergyEntryLogsPreviousValue() {
        EnergyEntry oldEntry = new EnergyEntry(
                1,
                household.getId(),
                "2026-09",
                120.0,
                15.0,
                "Old usage",
                user.getId(),
                "before"
        );

        EnergyEntry updatedEntry = new EnergyEntry(
                1,
                household.getId(),
                "2026-09",
                100.0,
                20.0,
                "Updated usage",
                user.getId(),
                "now"
        );

        when(energyEntryDao.findByHousehold(household.getId()))
                .thenReturn(List.of(oldEntry));

        when(energyEntryDao.update(anyLong(), anyDouble(), any(), any(), anyLong()))
                .thenReturn(updatedEntry);

        service.updateEntry(
                user,
                household,
                1,
                100.0,
                20.0,
                "Updated usage"
        );

        verify(activityLogDao).log(
                household.getId(),
                user.getId(),
                "Resident changed energy usage from 120.0 kWh to 100.0 kWh"
        );
    }

    private static long eqLong(long value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
