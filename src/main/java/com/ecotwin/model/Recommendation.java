package com.ecotwin.model;

/** US-24: one suggested change, the domain it improves, and the points it is worth. */
public record Recommendation(Domain domain, String title, String detail, int points) {

    public enum Domain { ENERGY, WATER, WASTE, TRANSPORT }
}