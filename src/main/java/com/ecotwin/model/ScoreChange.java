package com.ecotwin.model;

import com.ecotwin.model.Recommendation.Domain;

/** US-23: the household's scores just before and just after an update to one domain. */
public class ScoreChange {

    public ScoreChange(ScoreBreakdown before, ScoreBreakdown after, Domain domain) {
    }

    public Domain domain() {
        return null;
    }

    public ScenarioComparison overall() {
        return null;
    }

    public ScenarioComparison domainChange() {
        return null;
    }

    public boolean isFirstScore() {
        return false;
    }
}
