package com.ecotwin.service;

import com.ecotwin.model.Recommendation.Domain;
import com.ecotwin.model.SustainabilityScenario;

/**
 * US-24: one way a household could improve. A rule applies only when the household's
 * values call for it, models the change on a copy, and words itself from those values.
 */
public interface RecommendationRule {

    Domain domain();

    /** @return true when this change makes sense for these values */
    boolean applies(SustainabilityScenario values, int people);

    /** Change the copy to what the household would look like after taking the advice. */
    void apply(SustainabilityScenario copy, int people);

    String title();

    /** Wording that quotes the household's own numbers. */
    String detail(SustainabilityScenario values, int people);
}