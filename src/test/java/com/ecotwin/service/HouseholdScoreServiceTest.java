package com.ecotwin.service;

import com.ecotwin.model.EnergyEntry;
import com.ecotwin.model.Household;
import com.ecotwin.model.ScoreBreakdown;
import com.ecotwin.model.SustainabilityScenario;
import com.ecotwin.model.TransportEntry;
import com.ecotwin.model.WasteEntry;
import com.ecotwin.model.WaterEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HouseholdScoreServiceTest {

    private final EnergyService energyService = mock(EnergyService.class);
    private final WaterService waterService = mock(WaterService.class);
    private final WasteService wasteService = mock(WasteService.class);
    private final TransportService transportService = mock(TransportService.class);
    private final IndicativeScoreCalculator calculator = new IndicativeScoreCalculator();

    private final HouseholdScoreService service = new HouseholdScoreService(
            energyService, waterService, wasteService, transportService, calculator);

    private final Household household = new Household(7, "Test House", "ABC234", 2, "House", "QLD", "now");

    @BeforeEach
    void givenAReadingInEveryDomain() {
        when(energyService.findEntriesForHousehold(household)).thenReturn(List.of(
                new EnergyEntry(1, 7, "2026-08", 900, null, null, 1L, null),
                new EnergyEntry(2, 7, "2026-09", 300, null, null, 1L, null),
                new EnergyEntry(3, 7, "2026-07", 700, null, null, 1L, null)));
        when(waterService.findEntriesForHousehold(household)).thenReturn(List.of(
                new WaterEntry(1, 7, "2026-09", 9000, null, 1L, null)));
        when(wasteService.findEntriesForHousehold(household)).thenReturn(List.of(
                new WasteEntry(1, 7, "2026-09", 30, 15, 5, 1L, null)));
        when(transportService.findEntriesForHousehold(household)).thenReturn(List.of(
                new TransportEntry(1, 7, "2026-09", 8, 2, 1L, null)));
    }

    @Test
    void usesTheNewestReadingInEachDomain() {
        SustainabilityScenario values = service.currentValues(household).orElseThrow();

        assertEquals(300, values.getEnergyKwh());
    }

    @Test
    void scoresTheHouseholdsCurrentValues() {
        SustainabilityScenario values = service.currentValues(household).orElseThrow();

        ScoreBreakdown scores = service.currentScores(household).orElseThrow();

        assertEquals(calculator.breakdown(values, 2), scores);
    }

    @Test
    void hasNoScoresUntilEveryDomainHasAReading() {
        when(transportService.findEntriesForHousehold(household)).thenReturn(List.of());

        assertTrue(service.currentScores(household).isEmpty());
    }

    @Test
    void scoreHistoryReturnsOnePointPerCompletePeriodOldestFirst() {
        when(waterService.findEntriesForHousehold(household)).thenReturn(List.of(
                new WaterEntry(1, 7, "2026-07", 11000, null, 1L, null),
                new WaterEntry(2, 7, "2026-08", 10000, null, 1L, null),
                new WaterEntry(3, 7, "2026-09", 9000, null, 1L, null)));
        when(wasteService.findEntriesForHousehold(household)).thenReturn(List.of(
                new WasteEntry(1, 7, "2026-07", 40, 10, 2, 1L, null),
                new WasteEntry(2, 7, "2026-08", 35, 12, 3, 1L, null),
                new WasteEntry(3, 7, "2026-09", 30, 15, 5, 1L, null)));
        when(transportService.findEntriesForHousehold(household)).thenReturn(List.of(
                new TransportEntry(1, 7, "2026-07", 4, 3, 1L, null),
                new TransportEntry(2, 7, "2026-08", 6, 2, 1L, null),
                new TransportEntry(3, 7, "2026-09", 8, 2, 1L, null)));

        List<ScorePoint> history = service.scoreHistory(household);

        assertEquals(3, history.size());
        assertEquals("2026-07", history.get(0).period());
        assertEquals("2026-09", history.get(2).period());
    }

    @Test
    void periodsMissingADomainReadingAreLeftOut() {
        List<ScorePoint> history = service.scoreHistory(household);

        assertEquals(1, history.size());
        assertEquals("2026-09", history.get(0).period());
    }

    @Test
    void aHouseholdWithNoReadingsHasNoHistory() {
        when(energyService.findEntriesForHousehold(household)).thenReturn(List.of());
        when(waterService.findEntriesForHousehold(household)).thenReturn(List.of());
        when(wasteService.findEntriesForHousehold(household)).thenReturn(List.of());
        when(transportService.findEntriesForHousehold(household)).thenReturn(List.of());

        assertTrue(service.scoreHistory(household).isEmpty());
    }
}