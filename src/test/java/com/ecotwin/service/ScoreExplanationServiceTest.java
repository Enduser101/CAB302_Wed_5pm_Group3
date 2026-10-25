package com.ecotwin.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScoreExplanationServiceTest {

    @Test
    void explanationStatesScoreBounds() {
        ScoreExplanationService service = new ScoreExplanationService();

        String explanation = service.getExplanation();

        assertTrue(explanation.contains("0"));
        assertTrue(explanation.contains("100"));
    }

    @Test
    void explanationDescribesWhatHigherScoresMean() {
        ScoreExplanationService service = new ScoreExplanationService();

        String explanation = service.getExplanation();

        assertTrue(explanation.toLowerCase().contains("higher"));
        assertTrue(explanation.toLowerCase().contains("sustainable"));
    }
}
