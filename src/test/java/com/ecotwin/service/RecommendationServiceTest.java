package com.ecotwin.service;

import com.ecotwin.model.Recommendation;
import com.ecotwin.model.ScoreBreakdown;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecommendationServiceTest {

    @Test
    void weakestDomainComesFirst() {
        ScoreBreakdown scores = new ScoreBreakdown(97, 100, 96, 6, 75);

        List<Recommendation> recommendations = new RecommendationService().forScores(scores);

        assertEquals(Recommendation.Domain.TRANSPORT, recommendations.get(0).domain());
    }
    @Test
    void everyDomainGetsARecommendation() {
        List<Recommendation> recommendations =
                new RecommendationService().forScores(new ScoreBreakdown(50, 50, 50, 50, 50));

        assertEquals(4, recommendations.size());
        for (Recommendation.Domain domain : Recommendation.Domain.values()) {
            assertTrue(recommendations.stream().anyMatch(r -> r.domain() == domain),
                    "no recommendation for " + domain);
        }
    }
}