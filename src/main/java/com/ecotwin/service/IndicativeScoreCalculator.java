package com.ecotwin.service;

import com.ecotwin.model.ScoreBreakdown;
import com.ecotwin.model.SustainabilityScenario;

/**
 * Indicative scoring only - not a validated emissions model (out of scope, see docs/build_plan.md S4).
 *
 * Each domain is scored by its own DomainScorer (Strategy pattern).
 * The overall score is the average of the four domain scores.
 */
public class IndicativeScoreCalculator implements ScoreCalculator {

    private final DomainScorer energy;
    private final DomainScorer water;
    private final DomainScorer waste;
    private final DomainScorer transport;

    public IndicativeScoreCalculator() {
        this(new EnergyScorer(), new WaterScorer(), new WasteScorer(), new TransportScorer());
    }

    public IndicativeScoreCalculator(DomainScorer energy, DomainScorer water,
                                     DomainScorer waste, DomainScorer transport) {
        this.energy = energy;
        this.water = water;
        this.waste = waste;
        this.transport = transport;
    }

    @Override
    public double calculate(SustainabilityScenario values, int occupants) {
        return breakdown(values, occupants).total();
    }

    /** The four domain scores (US-21) and their average, the overall score. */
    public ScoreBreakdown breakdown(SustainabilityScenario values, int occupants) {
        int people = Math.max(1, occupants);

        double energyScore = energy.score(values, people);
        double waterScore = water.score(values, people);
        double wasteScore = waste.score(values, people);
        double transportScore = transport.score(values, people);

        return new ScoreBreakdown(energyScore, waterScore, wasteScore, transportScore,
                (energyScore + waterScore + wasteScore + transportScore) / 4);
    }
}