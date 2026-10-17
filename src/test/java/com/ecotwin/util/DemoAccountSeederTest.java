package com.ecotwin.util;

import com.ecotwin.AppContext;
import com.ecotwin.model.ActivityLogEntry;
import com.ecotwin.model.Household;
import com.ecotwin.model.ScorePoint;
import com.ecotwin.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** #115: a demo account with months of readings, so the dashboard and history have something to show. */
class DemoAccountSeederTest {

    @TempDir
    Path tempDir;

    private Connection connection;
    private AppContext ctx;
    private DemoAccountSeeder seeder;

    @BeforeEach
    void openDatabase() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite:" + tempDir.resolve("demo-test.db"));
        applySchema(connection);
        ctx = new AppContext(connection);
        seeder = new DemoAccountSeeder(connection, ctx);
    }

    @AfterEach
    void closeDatabase() throws Exception {
        connection.close();
    }

    @Test
    void demoAccountCanLogIn() {
        seeder.seedIfMissing();

        User user = ctx.authService.login(DemoAccountSeeder.USERNAME, DemoAccountSeeder.PASSWORD);

        assertEquals("Cab 302", user.getUsername());
    }

    @Test
    void demoHouseholdHasSixMonthsOfScores() {
        seeder.seedIfMissing();

        List<ScorePoint> history = ctx.householdScoreService.scoreHistory(demoHousehold());

        assertEquals(List.of("2026-05", "2026-06", "2026-07", "2026-08", "2026-09", "2026-10"),
                history.stream().map(ScorePoint::period).toList());
    }

    @Test
    void scoresGoUpAndDownBetweenMonths() {
        seeder.seedIfMissing();

        List<ScorePoint> history = ctx.householdScoreService.scoreHistory(demoHousehold());

        boolean rises = false;
        boolean falls = false;
        for (int i = 1; i < history.size(); i++) {
            double change = history.get(i).scores().total() - history.get(i - 1).scores().total();
            rises |= change > 0;
            falls |= change < 0;
        }
        assertTrue(rises && falls);
    }

    @Test
    void historyShowsEditsWithTheirPreviousValues() {
        seeder.seedIfMissing();

        List<String> messages = ctx.householdService.getHistory(demoHousehold()).stream()
                .map(ActivityLogEntry::getMessage).toList();

        assertTrue(messages.contains("Cab 302 changed water usage from 10400.0 L to 9500.0 L"));
        assertTrue(messages.contains("Cab 302 changed energy usage from 410.0 kWh to 395.0 kWh"
                + " and solar generation from none to 60.0 kWh"));
    }

    @Test
    void historyIsDatedWhenEachChangeHappened() {
        seeder.seedIfMissing();

        List<ActivityLogEntry> history = ctx.householdService.getHistory(demoHousehold());

        // Newest first: 17 Oct 2026 9:00am and 1 May 2026 10:00am in Brisbane, stored as UTC.
        assertEquals("2026-10-16T23:00:00Z", history.get(0).getCreatedAt());
        assertEquals("2026-05-01T00:00:00Z", history.get(history.size() - 1).getCreatedAt());
    }

    @Test
    void seedingAgainDoesNotCreateASecondDemoAccount() {
        assertTrue(seeder.seedIfMissing());

        assertFalse(seeder.seedIfMissing());
        assertEquals(1, ctx.householdService.getHistory(demoHousehold()).stream()
                .filter(e -> e.getMessage().endsWith("created the household")).count());
    }

    private Household demoHousehold() {
        User user = ctx.authService.login(DemoAccountSeeder.USERNAME, DemoAccountSeeder.PASSWORD);
        return ctx.householdService.findActiveHouseholdForUser(user).orElseThrow();
    }

    private static void applySchema(Connection connection) throws Exception {
        String schema;
        try (InputStream in = DemoAccountSeederTest.class.getResourceAsStream("/com/ecotwin/schema.sql")) {
            schema = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        String withoutComments = schema.replaceAll("--[^\n]*", "");
        for (String sql : withoutComments.split(";")) {
            if (sql.isBlank()) {
                continue;
            }
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
        }
    }
}
