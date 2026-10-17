package com.ecotwin.service;

import com.ecotwin.model.Recommendation;
import com.ecotwin.model.Recommendation.Domain;
import com.ecotwin.model.SustainabilityScenario;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationServiceTest {

    private final RecommendationService service = new RecommendationService(new IndicativeScoreCalculator());

    // energyKwh, solar, waterLitres, generalKg, recycledKg, compostKg, ptTripsPerWeek, flightsPerYear
    private static final SustainabilityScenario HEAVY_FLYER =
            new SustainabilityScenario(300, 100.0, 9000, 20, 20, 10, 0, 12);
    private static final SustainabilityScenario WATER_WASTER =
            new SustainabilityScenario(300, 100.0, 40000, 20, 20, 10, 20, 0);

    @Test
    void weakestDomainComesFirst() {
        List<Recommendation> recommendations = service.forHousehold(HEAVY_FLYER, 3);

        assertEquals(Domain.TRANSPORT, recommendations.get(0).domain());
    }

    @Test
    void everyDomainGetsARecommendationWhenEveryDomainCanImprove() {
        SustainabilityScenario poorEverywhere = new SustainabilityScenario(1500, null, 40000, 150, 5, 0, 0, 12);

        List<Recommendation> recommendations = service.forHousehold(poorEverywhere, 3);

        for (Domain domain : Domain.values()) {
            assertTrue(recommendations.stream().anyMatch(r -> r.domain() == domain), "none for " + domain);
        }
    }

    @Test
    void differentHouseholdsGetDifferentRecommendations() {
        assertNotEquals(service.forHousehold(HEAVY_FLYER, 3), service.forHousehold(WATER_WASTER, 3));
    }

    @Test
    void pointsAreTheScoreGainedByApplyingTheChange() {
        for (Recommendation r : service.forHousehold(HEAVY_FLYER, 3)) {
            assertTrue(r.points() > 0, r.title() + " should gain points");
        }
    }

    @Test
    void recommendationQuotesTheHouseholdsOwnNumbers() {
        Recommendation water = service.forHousehold(WATER_WASTER, 3).get(0);

        assertEquals(Domain.WATER, water.domain());
        assertTrue(water.detail().contains("40,000"), water.detail());
    }

    @Test
    void noRecommendationForAChangeAlreadyMade() {
        assertTrue(service.forHousehold(HEAVY_FLYER, 3).stream()
                .noneMatch(r -> r.title().toLowerCase().contains("solar")));
    }
}