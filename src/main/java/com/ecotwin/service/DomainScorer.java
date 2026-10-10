package com.ecotwin.service;

import com.ecotwin.model.SustainabilityScenario;

/**
 * Scores one domain (energy, water, waste or transport) from 0 (worst) to 100 (best).
 *
 */
public interface DomainScorer {

    /** @param people household size, already at least 1 */
    double score(SustainabilityScenario values, int people);
}