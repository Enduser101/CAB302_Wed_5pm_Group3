package com.ecotwin.service;

import com.ecotwin.dao.ActivityLogDao;
import com.ecotwin.dao.WasteEntryDao;
import com.ecotwin.model.Household;
import com.ecotwin.model.User;
import com.ecotwin.model.WasteEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WasteServiceTest {

    private final WasteEntryDao wasteEntryDao = mock(WasteEntryDao.class);
    private final ActivityLogDao activityLogDao = mock(ActivityLogDao.class);
    private final ScoreService scoreService = mock(ScoreService.class);
    private final WasteService service = new WasteService(wasteEntryDao, activityLogDao, scoreService);

    private final User user = new User(1, "resident", "r@example.com", "hash", "Resident", "now");
    private final Household household = new Household(1, "Test House", "ABC234", 2, "House", "QLD", "now");

    @Test
    void recordingAValidEntryPersistsIt() {
        WasteEntry saved = new WasteEntry(1, household.getId(), "2026-09", 5.0, 3.0, 1.0, user.getId(), "now");
        when(wasteEntryDao.create(anyLong(), anyString(), anyDouble(), anyDouble(), anyDouble(), any())).thenReturn(saved);

        WasteEntry result = service.recordEntry(user, household, 5.0, 3.0, 1.0);

        assertEquals(saved.getId(), result.getId());
        assertEquals(5.0, result.getGeneralKg());
    }

    @Test
    void recordingAnEntryLogsTheChange() {
        when(wasteEntryDao.create(anyLong(), anyString(), anyDouble(), anyDouble(), anyDouble(), any()))
            .thenReturn(new WasteEntry(1, household.getId(), "2026-09", 5.0, 3.0, 1.0, user.getId(), "now"));

        service.recordEntry(user, household, 5.0, 3.0, 1.0);

        verify(activityLogDao, times(1)).log(eqLong(household.getId()), eqLong(user.getId()), anyString());
    }

    @Test
    void recordingAnEntryTriggersScoreRecalculation() {
        when(wasteEntryDao.create(anyLong(), anyString(), anyDouble(), anyDouble(), anyDouble(), any()))
            .thenReturn(new WasteEntry(1, household.getId(), "2026-09", 5.0, 3.0, 1.0, user.getId(), "now"));

        service.recordEntry(user, household, 5.0, 3.0, 1.0);

        verify(scoreService, times(1)).recalculate(household.getId());
    }

    @Test
    void negativeWasteAmountsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.recordEntry(user, household, -1.0, 3.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> service.recordEntry(user, household, 5.0, -3.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> service.recordEntry(user, household, 5.0, 3.0, -1.0));
        verifyNoInteractions(wasteEntryDao, activityLogDao, scoreService);
    }

    private static long eqLong(long value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
