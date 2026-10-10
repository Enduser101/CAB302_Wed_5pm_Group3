package com.ecotwin.service;

/** Maths shared by the domain scorers. */
final class ScoreMaths {

    private ScoreMaths() {
    }

    /** Using nothing scores 100, the benchmark scores 50, and double the benchmark or more scores 0. */
    static double usageScore(double usagePerPerson, double benchmark) {
        return clamp(100 * (1 - usagePerPerson / (2 * benchmark)));
    }

    static double clamp(double score) {
        return Math.max(0, Math.min(100, score));
    }
}