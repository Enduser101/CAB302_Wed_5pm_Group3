package com.ecotwin.service;

import com.ecotwin.model.SustainabilityScenario;

/** Half from how little goes to landfill, half from the share of waste diverted from it. */
public class WasteScorer implements DomainScorer {

    static final double LANDFILL_KG_BENCHMARK = 20;

    @Override
    public double score(SustainabilityScenario values, int people) {
        double diverted = values.getRecycledWasteKg() + values.getCompostKg();
        double total = values.getGeneralWasteKg() + diverted;
        double diversionScore = total == 0 ? 100 : 100 * diverted / total;
        double landfillScore = ScoreMaths.usageScore(values.getGeneralWasteKg() / people, LANDFILL_KG_BENCHMARK);
        return (landfillScore + diversionScore) / 2;
    }
}