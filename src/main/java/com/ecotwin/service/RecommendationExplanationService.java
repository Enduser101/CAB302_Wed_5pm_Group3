package com.ecotwin.service;

/**
 * Provides plain-language explanations for sustainability recommendations.
 * Each explanation refers to the household value that caused the recommendation.
 */
public class RecommendationExplanationService {

    public String explainWater(double litres) {
        return "Your household currently uses " + format(litres)
                + " litres of water. Reducing water use could make your household more sustainable.";
    }

    public String explainEnergy(double energyKwh) {
        return "Your household currently uses " + format(energyKwh)
                + " kWh of energy. Reducing energy use could make your household more sustainable.";
    }

    public String explainWaste(double wasteValue) {
        return "Your household's current waste value is " + format(wasteValue)
                + ". Improving how your household manages waste could make it more sustainable.";
    }

    public String explainTransport(double transportValue) {
        return "Your household's current transport value is " + format(transportValue)
                + ". Choosing more sustainable transport options could improve your household's sustainability.";
    }

    private String format(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }

        return String.valueOf(value);
    }
}
