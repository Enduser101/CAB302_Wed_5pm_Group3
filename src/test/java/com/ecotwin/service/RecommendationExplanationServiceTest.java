package com.ecotwin.service;

import com.ecotwin.model.Recommendation;
import com.ecotwin.model.Recommendation.Domain;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationExplanationServiceTest {

    private final RecommendationExplanationService service =
            new RecommendationExplanationService();

    @Test
    void explanationUsesRecommendationHouseholdDetail() {
        Recommendation recommendation = new Recommendation(
                Domain.WATER,
                "Install low-flow shower heads",
                "You use 12000 L a month, 3000 L per person against a 2500 L benchmark.",
                4
        );

        String explanation = service.explain(recommendation);

        assertTrue(explanation.contains("12000"));
        assertTrue(explanation.contains("3000"));
        assertTrue(explanation.contains("2500"));
    }

    @Test
    void explanationKeepsPlainLanguageReason() {
        Recommendation recommendation = new Recommendation(
                Domain.ENERGY,
                "Cut electricity use by 15%",
                "You use 145 kWh per person a month against a 120 kWh benchmark.",
                3
        );

        String explanation = service.explain(recommendation);

        assertEquals(
                "You use 145 kWh per person a month against a 120 kWh benchmark.",
                explanation
        );
    }
}