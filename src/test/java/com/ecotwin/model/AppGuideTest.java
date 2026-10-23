package com.ecotwin.model;

import com.ecotwin.util.Page;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** #94: the "About EcoTwin" guide on the Household page explains the app and each tab. */
class AppGuideTest {

    @Test
    void everyPageHasADescription() {
        for (Page page : Page.values()) {
            String description = AppGuide.describe(page);
            assertFalse(description == null || description.isBlank(), page + " has no description");
        }
    }

    @Test
    void tabsAreListedInSidebarOrder() {
        List<String> names = AppGuide.tabs().stream().map(AppGuide.Tab::name).toList();

        assertEquals(List.of("Dashboard", "Resources", "Recommendations", "Scenarios", "Household", "Account"), names);
    }

    @Test
    void introNamesTheFourAreasThatAreScored() {
        String intro = AppGuide.intro().toLowerCase();

        for (String area : List.of("energy", "water", "waste", "transport")) {
            assertTrue(intro.contains(area), "intro does not mention " + area);
        }
    }
}
