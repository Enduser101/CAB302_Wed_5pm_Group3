package com.ecotwin.service;

import com.ecotwin.model.SustainabilityScenario;

/** Electricity used after solar, per person, against a monthly benchmark. */
public class EnergyScorer implements DomainScorer {

    static final double KWH_BENCHMARK = 180;

    @Override
    public double score(SustainabilityScenario values, int people) {
        double solar = values.getSolarGenerationKwh() == null ? 0 : values.getSolarGenerationKwh();
        double netKwh = Math.max(0, values.getEnergyKwh() - solar);
        return ScoreMaths.usageScore(netKwh / people, KWH_BENCHMARK);
    }
}