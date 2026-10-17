package com.ecotwin.service;

import com.ecotwin.model.EnergyEntry;
import com.ecotwin.model.Household;
import com.ecotwin.model.Recommendation.Domain;
import com.ecotwin.model.ScenarioComparison.Direction;
import com.ecotwin.model.ScoreBreakdown;
import com.ecotwin.model.ScoreChange;
import com.ecotwin.model.TransportEntry;
import com.ecotwin.model.WasteEntry;
import com.ecotwin.model.WaterEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** US-23: see how an update to household data changes the sustainability score. */
class ScoreChangeTrackingTest {

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
                new EnergyEntry(1, 7, "2026-10", 300, null, null, 1L, null)));
        waterReadingIs(9000);
        when(wasteService.findEntriesForHousehold(household)).thenReturn(List.of(
                new WasteEntry(1, 7, "2026-10", 30, 15, 5, 1L, null)));
        when(transportService.findEntriesForHousehold(household)).thenReturn(List.of(
                new TransportEntry(1, 7, "2026-10", 8, 2, 1L, null)));
    }

    @Test
    void previousAndNewScoreAreBothShownAfterAnUpdate() {
        ScoreBreakdown before = service.currentScores(household).orElseThrow();

        ScoreChange change = service.trackChange(household, Domain.WATER, () -> waterReadingIs(4000)).orElseThrow();

        ScoreBreakdown after = service.currentScores(household).orElseThrow();
        assertEquals(oneDecimal(before.total()), change.overall().getCurrentScore());
        assertEquals(oneDecimal(after.total()), change.overall().getScenarioScore());
    }

    @Test
    void affectedDomainScoreIsAlsoShown() {
        ScoreBreakdown before = service.currentScores(household).orElseThrow();

        ScoreChange change = service.trackChange(household, Domain.WATER, () -> waterReadingIs(4000)).orElseThrow();

        ScoreBreakdown after = service.currentScores(household).orElseThrow();
        assertEquals(Domain.WATER, change.domain());
        assertEquals(oneDecimal(before.water()), change.domainChange().getCurrentScore());
        assertEquals(oneDecimal(after.water()), change.domainChange().getScenarioScore());
    }

    @Test
    void usingLessIsReportedAsAnImprovement() {
        ScoreChange change = service.trackChange(household, Domain.WATER, () -> waterReadingIs(4000)).orElseThrow();

        assertEquals(Direction.IMPROVED, change.overall().getDirection());
        assertEquals(Direction.IMPROVED, change.domainChange().getDirection());
    }

    @Test
    void usingMoreIsReportedAsWorse() {
        ScoreChange change = service.trackChange(household, Domain.WATER, () -> waterReadingIs(15000)).orElseThrow();

        assertEquals(Direction.WORSENED, change.overall().getDirection());
        assertEquals(Direction.WORSENED, change.domainChange().getDirection());
    }

    @Test
    void anUpdateThatChangesNothingIsReportedAsUnchanged() {
        ScoreChange change = service.trackChange(household, Domain.WATER, () -> waterReadingIs(9000)).orElseThrow();

        assertEquals(Direction.UNCHANGED, change.overall().getDirection());
    }

    @Test
    void theReadingThatCompletesEveryDomainGivesTheFirstScore() {
        when(transportService.findEntriesForHousehold(household)).thenReturn(List.of());

        ScoreChange change = service.trackChange(household, Domain.TRANSPORT, () ->
                when(transportService.findEntriesForHousehold(household)).thenReturn(List.of(
                        new TransportEntry(1, 7, "2026-10", 8, 2, 1L, null)))).orElseThrow();

        assertTrue(change.isFirstScore());
        assertEquals(oneDecimal(service.currentScores(household).orElseThrow().total()),
                change.overall().getScenarioScore());
    }

    @Test
    void aScoredHouseholdHasAPreviousScore() {
        ScoreChange change = service.trackChange(household, Domain.WATER, () -> waterReadingIs(4000)).orElseThrow();

        assertFalse(change.isFirstScore());
    }

    @Test
    void noScoreChangeUntilEveryDomainHasAReading() {
        when(transportService.findEntriesForHousehold(household)).thenReturn(List.of());

        assertTrue(service.trackChange(household, Domain.WATER, () -> waterReadingIs(4000)).isEmpty());
    }

    @Test
    void aRejectedUpdateIsPassedBackToTheCaller() {
        assertThrows(IllegalArgumentException.class, () -> service.trackChange(household, Domain.WATER, () -> {
            throw new IllegalArgumentException("Water usage cannot be negative");
        }));
    }

    private void waterReadingIs(double litres) {
        when(waterService.findEntriesForHousehold(household)).thenReturn(List.of(
                new WaterEntry(1, 7, "2026-10", litres, null, 1L, null)));
    }

    private static double oneDecimal(double score) {
        return Math.round(score * 10) / 10.0;
    }
}
