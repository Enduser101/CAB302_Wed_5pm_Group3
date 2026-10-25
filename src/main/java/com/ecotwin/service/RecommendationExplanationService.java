package com.ecotwin.service;

import com.ecotwin.model.Recommendation;

/**
 * US-26: provides a plain-language explanation of why a
 * recommendation was made for the household.
 */
public class RecommendationExplanationService {

    public String explain(Recommendation recommendation) {
        if (recommendation == null) {
            throw new IllegalArgumentException("Recommendation cannot be null.");
        }

        return recommendation.detail();
    }
}