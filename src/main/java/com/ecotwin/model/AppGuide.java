package com.ecotwin.model;

import com.ecotwin.util.Page;

import java.util.Arrays;
import java.util.List;

/**
 * #94: the "About EcoTwin" text on the Household page, the first page a signed-in user sees.
 * It says what the app is for and what each sidebar tab does.
 *
 * <p>The text lives here rather than in the FXML so it is unit tested: every {@link Page} must
 * have a description, so a new tab cannot be added without explaining it.
 */
public final class AppGuide {

    /**
     * One sidebar tab and what it is for.
     *
     * @param name        the tab's label, as shown in the sidebar
     * @param description one sentence on what the tab is used for
     */
    public record Tab(String name, String description) {
    }

    private AppGuide() {
    }

    /**
     * @return a one-sentence summary of what EcoTwin does
     */
    public static String intro() {
        return "EcoTwin tracks your household's energy, water, waste and transport each month "
                + "and turns them into a sustainability score out of 100.";
    }

    /**
     * @param page a sidebar page
     * @return one sentence on what that page is used for
     */
    public static String describe(Page page) {
        return switch (page) {
            case DASHBOARD -> "Your overall score, the score for each area and how they have changed over time.";
            case RESOURCES -> "Record this month's readings, or correct earlier ones.";
            case RECOMMENDATIONS -> "Suggestions for your weakest areas, and why each one is made.";
            case SCENARIOS -> "Try \"what if\" changes and compare the score before you save them.";
            case HOUSEHOLD -> "Create or join a household, see its members and its history of changes.";
            case ACCOUNT -> "Your details, your password and signing out.";
        };
    }

    /**
     * @return every tab with its description, in the same order as the sidebar
     */
    public static List<Tab> tabs() {
        return Arrays.stream(Page.values())
                .map(page -> new Tab(tabName(page), describe(page)))
                .toList();
    }

    /**
     * @param page a sidebar page
     * @return its sidebar label, e.g. "Dashboard" for {@link Page#DASHBOARD}
     */
    private static String tabName(Page page) {
        String name = page.name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
}
