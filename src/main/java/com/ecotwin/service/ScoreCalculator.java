package com.ecotwin.service;

import com.ecotwin.model.ScoreBreakdown;
import com.ecotwin.model.SustainabilityScenario;

/**
 * Turns a set of household resource values into an overall sustainability score.
 *
 * The current score and a scenario's score (US-28) are both produced by the same
 * calculator, so the two are always directly comparable. Epic 6 (US-20/21) can swap
 * in its own implementation without touching the scenario code.
 */
public interface ScoreCalculator {

    /** @return overall score from 0 (worst) to 100 (best) */
    double calculate(SustainabilityScenario values, int occupants);

    /** @return the four domain scores (US-21) and the overall score */
    ScoreBreakdown breakdown(SustainabilityScenario values, int occupants);
}