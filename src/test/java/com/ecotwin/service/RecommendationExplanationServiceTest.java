package com.ecotwin.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationExplanationServiceTest {

    @Test
    void waterExplanationReferencesHouseholdValue() {
        RecommendationExplanationService service =
                new RecommendationExplanationService();

        String explanation = service.explainWater(190);

        assertTrue(explanation.contains("190"));
        assertTrue(explanation.toLowerCase().contains("water"));
    }

    @Test
    void energyExplanationReferencesHouseholdValue() {
        RecommendationExplanationService service =
                new RecommendationExplanationService();

        String explanation = service.explainEnergy(145);

        assertTrue(explanation.contains("145"));
        assertTrue(explanation.toLowerCase().contains("energy"));
    }

    @Test
    void wasteExplanationReferencesHouseholdValue() {
        RecommendationExplanationService service =
                new RecommendationExplanationService();

        String explanation = service.explainWaste(65);

        assertTrue(explanation.contains("65"));
        assertTrue(explanation.toLowerCase().contains("waste"));
    }

    @Test
    void transportExplanationReferencesHouseholdValue() {
        RecommendationExplanationService service =
                new RecommendationExplanationService();

        String explanation = service.explainTransport(8);

        assertTrue(explanation.contains("8"));
        assertTrue(explanation.toLowerCase().contains("transport"));
    }
}
