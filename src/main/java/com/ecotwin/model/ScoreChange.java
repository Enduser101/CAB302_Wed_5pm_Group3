package com.ecotwin.model;

import com.ecotwin.model.Recommendation.Domain;

/**
 * US-23: the household's scores just before and just after an update to one domain.
 *
 * Before is null when the update gave the household its first full set of readings,
 * so there was no score to compare against.
 */
public class ScoreChange {

    private final ScoreBreakdown before;
    private final ScoreBreakdown after;
    private final Domain domain;

    public ScoreChange(ScoreBreakdown before, ScoreBreakdown after, Domain domain) {
        this.before = before;
        this.after = after;
        this.domain = domain;
    }

    public Domain domain() {
        return domain;
    }

    public boolean isFirstScore() {
        return before == null;
    }

    /** Overall score before and after. */
    public ScenarioComparison overall() {
        double previous = isFirstScore() ? after.total() : before.total();
        return new ScenarioComparison(previous, after.total());
    }

    /** The updated domain's score before and after. */
    public ScenarioComparison domainChange() {
        double previous = isFirstScore() ? domainScore(after) : domainScore(before);
        return new ScenarioComparison(previous, domainScore(after));
    }

    private double domainScore(ScoreBreakdown scores) {
        return switch (domain) {
            case ENERGY -> scores.energy();
            case WATER -> scores.water();
            case WASTE -> scores.waste();
            case TRANSPORT -> scores.transport();
        };
    }
}
