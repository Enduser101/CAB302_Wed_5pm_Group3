package com.ecotwin.service;

/** No-op until Epic 6 (US-20/21) implements real sustainability scoring. */
public class PlaceholderScoreService implements ScoreService {

    @Override
    public void recalculate(long householdId) {
        // Intentionally empty - see ScoreService.
    }
}
