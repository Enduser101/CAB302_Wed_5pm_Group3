package com.ecotwin.service;

import com.ecotwin.model.Recommendation;
import com.ecotwin.model.Recommendation.Domain;
import com.ecotwin.model.ScoreBreakdown;
import com.ecotwin.model.SustainabilityScenario;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * US-24: recommendations worked out from the household's own values.
 * Only rules that apply to this household are offered, each worded from its numbers,
 * worth the points the total score would gain, and ordered weakest domain first.
 */
public class RecommendationService {

    private final ScoreCalculator scoreCalculator;
    private final List<RecommendationRule> rules;

    public RecommendationService(ScoreCalculator scoreCalculator) {
        this(scoreCalculator, RecommendationRules.all());
    }

    RecommendationService(ScoreCalculator scoreCalculator, List<RecommendationRule> rules) {
        this.scoreCalculator = scoreCalculator;
        this.rules = rules;
    }

    public List<Recommendation> forHousehold(SustainabilityScenario values, int occupants) {
        int people = Math.max(1, occupants);
        ScoreBreakdown now = scoreCalculator.breakdown(values, people);
        Map<Domain, Double> scoreOf = Map.of(
                Domain.ENERGY, now.energy(),
                Domain.WATER, now.water(),
                Domain.WASTE, now.waste(),
                Domain.TRANSPORT, now.transport());

        return rules.stream()
                .filter(rule -> rule.applies(values, people))
                .map(rule -> new Recommendation(rule.domain(), rule.title(), rule.detail(values, people),
                        gain(rule, values, people, now.total())))
                .filter(r -> r.points() > 0)
                .sorted(Comparator.<Recommendation>comparingDouble(r -> scoreOf.get(r.domain()))
                        .thenComparing(Comparator.comparingInt(Recommendation::points).reversed()))
                .toList();
    }

    private int gain(RecommendationRule rule, SustainabilityScenario values, int people, double currentTotal) {
        SustainabilityScenario changed = values.copy();
        rule.apply(changed, people);
        return (int) Math.round(scoreCalculator.breakdown(changed, people).total() - currentTotal);
    }
}