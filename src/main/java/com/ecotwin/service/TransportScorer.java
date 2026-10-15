package com.ecotwin.service;

import com.ecotwin.model.SustainabilityScenario;

/** Mostly flights per person, plus credit for public transport trips. */
public class TransportScorer implements DomainScorer {

    static final double FLIGHTS_PER_YEAR_BENCHMARK = 2;
    static final double PUBLIC_TRANSPORT_TRIPS_PER_WEEK_TARGET = 10;

    private static final double FLIGHTS_WEIGHT = 0.7;
    private static final double PUBLIC_TRANSPORT_WEIGHT = 0.3;

    @Override
    public double score(SustainabilityScenario values, int people) {
        double flightsScore = ScoreMaths.usageScore(values.getFlightsPerYear() / people, FLIGHTS_PER_YEAR_BENCHMARK);
        double publicTransportScore = ScoreMaths.clamp(
                100 * (values.getPublicTransportTripsPerWeek() / people) / PUBLIC_TRANSPORT_TRIPS_PER_WEEK_TARGET);
        return FLIGHTS_WEIGHT * flightsScore + PUBLIC_TRANSPORT_WEIGHT * publicTransportScore;
    }
}