package com.ecotwin.service;

import com.ecotwin.model.SustainabilityScenario;

/** Water used per person against a monthly benchmark. */
public class WaterScorer implements DomainScorer {

    static final double LITRES_BENCHMARK = 5500;

    @Override
    public double score(SustainabilityScenario values, int people) {
        return ScoreMaths.usageScore(values.getWaterLitres() / people, LITRES_BENCHMARK);
    }
}