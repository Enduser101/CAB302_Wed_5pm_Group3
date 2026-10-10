package com.ecotwin.service;

import com.ecotwin.model.SustainabilityScenario;

/**
 * Indicative scoring only - not a validated emissions model (out of scope, see docs/build_plan.md S4).
 *
 * Each domain compares per-person usage with a rough benchmark: using nothing scores 100,
 * using the benchmark amount scores 50, and using double the benchmark or more scores 0.
 * The overall score is the average of the four domain scores.
 */
public class IndicativeScoreCalculator implements ScoreCalculator {

    // Per person, per monthly period unless noted.
    static final double ENERGY_KWH_BENCHMARK = 180;
    static final double WATER_LITRES_BENCHMARK = 5500;
    static final double LANDFILL_KG_BENCHMARK = 20;
    static final double FLIGHTS_PER_YEAR_BENCHMARK = 2;
    static final double PUBLIC_TRANSPORT_TRIPS_PER_WEEK_TARGET = 10;

    private static final double FLIGHTS_WEIGHT = 0.7;
    private static final double PUBLIC_TRANSPORT_WEIGHT = 0.3;

    @Override
    public double calculate(SustainabilityScenario values, int occupants) {
        int people = Math.max(1, occupants);

        return (energyScore(values, people)
                + waterScore(values, people)
                + wasteScore(values, people)
                + transportScore(values, people)) / 4;
    }

    private double energyScore(SustainabilityScenario values, int people) {
        double solar = values.getSolarGenerationKwh() == null ? 0 : values.getSolarGenerationKwh();
        double netKwh = Math.max(0, values.getEnergyKwh() - solar);
        return usageScore(netKwh / people, ENERGY_KWH_BENCHMARK);
    }

    private double waterScore(SustainabilityScenario values, int people) {
        return usageScore(values.getWaterLitres() / people, WATER_LITRES_BENCHMARK);
    }

    /** Half from how little goes to landfill, half from the share of waste diverted from it. */
    private double wasteScore(SustainabilityScenario values, int people) {
        double diverted = values.getRecycledWasteKg() + values.getCompostKg();
        double total = values.getGeneralWasteKg() + diverted;
        double diversionScore = total == 0 ? 100 : 100 * diverted / total;
        double landfillScore = usageScore(values.getGeneralWasteKg() / people, LANDFILL_KG_BENCHMARK);
        return (landfillScore + diversionScore) / 2;
    }

    private double transportScore(SustainabilityScenario values, int people) {
        double flightsScore = usageScore(values.getFlightsPerYear() / people, FLIGHTS_PER_YEAR_BENCHMARK);
        double publicTransportScore = clamp(
                100 * (values.getPublicTransportTripsPerWeek() / people) / PUBLIC_TRANSPORT_TRIPS_PER_WEEK_TARGET);
        return FLIGHTS_WEIGHT * flightsScore + PUBLIC_TRANSPORT_WEIGHT * publicTransportScore;
    }

    private double usageScore(double usagePerPerson, double benchmark) {
        return clamp(100 * (1 - usagePerPerson / (2 * benchmark)));
    }

    private double clamp(double score) {
        return Math.max(0, Math.min(100, score));
    }
}
