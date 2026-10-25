package com.ecotwin.model;

/** US-23: one periods scores, for the dashboard's score over time chart. */
public record ScorePoint(String period, ScoreBreakdown scores) {
}