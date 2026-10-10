package com.ecotwin.service;

import com.ecotwin.model.SustainabilityScenario;
import org.junit.jupiter.api.Test;
import com.ecotwin.model.ScoreBreakdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndicativeScoreCalculatorTest {

    private final IndicativeScoreCalculator calculator = new IndicativeScoreCalculator();

    private SustainabilityScenario baseline() {
        return new SustainabilityScenario(500, null, 9000, 30, 15, 5, 8, 2);
    }

    @Test
    void sameValuesAlwaysGiveTheSameScore() {
        assertEquals(calculator.calculate(baseline(), 2), calculator.calculate(baseline(), 2));
    }

    @Test
    void usingLessElectricityScoresHigher() {
        SustainabilityScenario lower = baseline();
        lower.setEnergyKwh(300);

        assertTrue(calculator.calculate(lower, 2) > calculator.calculate(baseline(), 2));
    }

    @Test
    void solarGenerationOffsetsElectricityUse() {
        SustainabilityScenario withSolar = baseline();
        withSolar.setSolarGenerationKwh(200.0);

        assertTrue(calculator.calculate(withSolar, 2) > calculator.calculate(baseline(), 2));
    }

    @Test
    void usingMoreWaterScoresLower() {
        SustainabilityScenario higher = baseline();
        higher.setWaterLitres(15000);

        assertTrue(calculator.calculate(higher, 2) < calculator.calculate(baseline(), 2));
    }

    @Test
    void movingWasteFromLandfillToRecyclingScoresHigher() {
        SustainabilityScenario diverted = baseline();
        diverted.setGeneralWasteKg(20);
        diverted.setRecycledWasteKg(25);

        assertTrue(calculator.calculate(diverted, 2) > calculator.calculate(baseline(), 2));
    }

    @Test
    void takingFewerFlightsScoresHigher() {
        SustainabilityScenario fewerFlights = baseline();
        fewerFlights.setFlightsPerYear(0);

        assertTrue(calculator.calculate(fewerFlights, 2) > calculator.calculate(baseline(), 2));
    }

    @Test
    void sameUsageSharedByMorePeopleScoresHigher() {
        assertTrue(calculator.calculate(baseline(), 4) > calculator.calculate(baseline(), 2));
    }

    @Test
    void scoreNeverLeavesTheZeroToHundredRange() {
        SustainabilityScenario nothingUsed = new SustainabilityScenario(0, null, 0, 0, 0, 0, 0, 0);
        SustainabilityScenario extremeUse =
                new SustainabilityScenario(1_000_000, null, 1_000_000_000, 100_000, 0, 0, 0, 500);

        double best = calculator.calculate(nothingUsed, 1);
        double worst = calculator.calculate(extremeUse, 1);

        assertTrue(best <= 100 && best >= 0, "best was " + best);
        assertTrue(worst <= 100 && worst >= 0, "worst was " + worst);
    }
    @Test
    void breakdownGivesFourDomainScoresThatAverageToTheTotal() {
        ScoreBreakdown breakdown = calculator.breakdown(baseline(), 2);

        double average = (breakdown.energy() + breakdown.water()
                + breakdown.waste() + breakdown.transport()) / 4;

        assertEquals(calculator.calculate(baseline(), 2), breakdown.total(), 0.001);
        assertEquals(breakdown.total(), average, 0.001);
    }

    @Test
    void usingMoreWaterLowersOnlyTheWaterScore() {
        SustainabilityScenario higher = baseline();
        higher.setWaterLitres(15000);

        ScoreBreakdown before = calculator.breakdown(baseline(), 2);
        ScoreBreakdown after = calculator.breakdown(higher, 2);

        assertTrue(after.water() < before.water());
        assertEquals(before.energy(), after.energy(), 0.001);
        assertEquals(before.waste(), after.waste(), 0.001);
        assertEquals(before.transport(), after.transport(), 0.001);
    }
}
