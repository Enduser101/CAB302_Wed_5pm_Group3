package com.ecotwin.service;

import com.ecotwin.dao.ActivityLogDao;
import com.ecotwin.dao.WaterEntryDao;
import com.ecotwin.model.Household;
import com.ecotwin.model.User;
import com.ecotwin.model.WaterEntry;
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

class WaterServiceTest {

    private final WaterEntryDao waterEntryDao = mock(WaterEntryDao.class);
    private final ActivityLogDao activityLogDao = mock(ActivityLogDao.class);
    private final ScoreService scoreService = mock(ScoreService.class);
    private final WaterService service = new WaterService(waterEntryDao, activityLogDao, scoreService);

    private final User user = new User(1, "resident", "r@example.com", "hash", "Resident", "now");
    private final Household household = new Household(1, "Test House", "ABC234", 2, "House", "QLD", "now");

    @Test
    void recordingAValidEntryPersistsIt() {
        WaterEntry saved = new WaterEntry(1, household.getId(), "2026-09", 350.0, null, user.getId(), "now");
        when(waterEntryDao.create(anyLong(), anyString(), anyDouble(), any(), any())).thenReturn(saved);

        WaterEntry result = service.recordEntry(user, household, 350.0, null);

        assertEquals(saved.getId(), result.getId());
        assertEquals(350.0, result.getLitres());
    }

    @Test
    void recordingAnEntryLogsTheChange() {
        when(waterEntryDao.create(anyLong(), anyString(), anyDouble(), any(), any()))
            .thenReturn(new WaterEntry(1, household.getId(), "2026-09", 350.0, null, user.getId(), "now"));

        service.recordEntry(user, household, 350.0, null);

        verify(activityLogDao, times(1)).log(eqLong(household.getId()), eqLong(user.getId()), anyString());
    }

    @Test
    void recordingAnEntryTriggersScoreRecalculation() {
        when(waterEntryDao.create(anyLong(), anyString(), anyDouble(), any(), any()))
            .thenReturn(new WaterEntry(1, household.getId(), "2026-09", 350.0, null, user.getId(), "now"));

        service.recordEntry(user, household, 350.0, null);

        verify(scoreService, times(1)).recalculate(household.getId());
    }

    @Test
    void negativeWaterUsageIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.recordEntry(user, household, -1.0, null));
        verifyNoInteractions(waterEntryDao, activityLogDao, scoreService);
    }

    private static long eqLong(long value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
