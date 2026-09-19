package com.ecotwin.service;

/**
 * Seam for Epic 6 (US-20/21 sustainability scores). Resource-tracking services (US-15..18) call
 * this after every persisted change, per their "score recalculates" acceptance criterion, so the
 * real scoring algorithm has a single place to plug into once Epic 6 is built.
 */
public interface ScoreService {
    void recalculate(long householdId);
}
