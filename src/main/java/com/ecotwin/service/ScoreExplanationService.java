package com.ecotwin.service;

/**
 * Provides a simple explanation of the EcoTwin sustainability score.
 */
public class ScoreExplanationService {

    public String getExplanation() {
        return "Your EcoTwin sustainability score ranges from 0 to 100. "
                + "A higher score means your household's everyday resource use "
                + "is more sustainable.";
    }
}