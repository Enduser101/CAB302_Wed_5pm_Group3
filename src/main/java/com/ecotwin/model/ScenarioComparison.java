package com.ecotwin.model;

/**
 * US-28: a scenario's score next to the household's current score.
 *
 * Scores are rounded to one decimal place, as displayed, so the difference always
 * matches the two scores shown beside it.
 */
public class ScenarioComparison {

    public enum Direction { IMPROVED, WORSENED, UNCHANGED }

    private final double currentScore;
    private final double scenarioScore;
    private final double difference;

    public ScenarioComparison(double currentScore, double scenarioScore) {
        this.currentScore = round(currentScore);
        this.scenarioScore = round(scenarioScore);
        this.difference = round(this.scenarioScore - this.currentScore);
    }

    public double getCurrentScore() {
        return currentScore;
    }

    public double getScenarioScore() {
        return scenarioScore;
    }

    /** Scenario score minus current score: positive means the scenario is better. */
    public double getDifference() {
        return difference;
    }

    public Direction getDirection() {
        if (difference > 0) {
            return Direction.IMPROVED;
        }
        if (difference < 0) {
            return Direction.WORSENED;
        }
        return Direction.UNCHANGED;
    }

    private static double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
