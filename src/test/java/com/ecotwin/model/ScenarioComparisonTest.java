package com.ecotwin.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScenarioComparisonTest {

    @Test
    void differenceIsScenarioScoreMinusCurrentScore() {
        ScenarioComparison comparison = new ScenarioComparison(48.0, 55.5);

        assertEquals(7.5, comparison.getDifference());
    }

    @Test
    void equalScoresAreReportedAsUnchanged() {
        ScenarioComparison comparison = new ScenarioComparison(48.0, 48.0);

        assertEquals(0.0, comparison.getDifference());
        assertEquals(ScenarioComparison.Direction.UNCHANGED, comparison.getDirection());
    }

    @Test
    void differenceAlwaysMatchesTheTwoScoresAsDisplayed() {
        // 48.04 and 48.06 display as 48.0 and 48.1, so the difference shown must be 0.1, not 0.0.
        ScenarioComparison comparison = new ScenarioComparison(48.04, 48.06);

        assertEquals(48.0, comparison.getCurrentScore());
        assertEquals(48.1, comparison.getScenarioScore());
        assertEquals(0.1, comparison.getDifference());
        assertEquals(ScenarioComparison.Direction.IMPROVED, comparison.getDirection());
    }

    @Test
    void changeTooSmallToDisplayIsReportedAsUnchanged() {
        ScenarioComparison comparison = new ScenarioComparison(48.01, 48.02);

        assertEquals(ScenarioComparison.Direction.UNCHANGED, comparison.getDirection());
    }
}
