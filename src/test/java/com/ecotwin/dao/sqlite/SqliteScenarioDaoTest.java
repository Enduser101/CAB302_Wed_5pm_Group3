package com.ecotwin.dao.sqlite;

import com.ecotwin.model.SavedScenario;
import com.ecotwin.model.SustainabilityScenario;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs against a real SQLite file in a temporary folder, never the app's ecotwin.db. */
class SqliteScenarioDaoTest {

    private static final long HOUSEHOLD = 1;
    private static final long OTHER_HOUSEHOLD = 2;
    private static final long USER = 5;

    @TempDir
    Path tempDir;

    private Connection connection;
    private SqliteScenarioDao dao;

    private final SustainabilityScenario values =
            new SustainabilityScenario(350.5, 100.0, 3000, 20, 15, 5, 12, 2);

    @BeforeEach
    void openDatabase() throws Exception {
        connection = open();
        applySchema(connection);
        dao = new SqliteScenarioDao(connection);
    }

    @AfterEach
    void closeDatabase() throws Exception {
        connection.close();
    }

    @Test
    void savedScenarioIsStillThereAfterTheAppRestarts() throws Exception {
        dao.create(HOUSEHOLD, USER, "Shorter showers", values, 50.0, 62.5);

        connection.close();
        connection = open();
        List<SavedScenario> afterRestart = new SqliteScenarioDao(connection).findByHousehold(HOUSEHOLD);

        assertEquals(1, afterRestart.size());
        SavedScenario saved = afterRestart.get(0);
        assertEquals("Shorter showers", saved.getName());
        assertEquals(50.0, saved.getBaselineScore());
        assertEquals(62.5, saved.getProjectedScore());
        assertEquals(350.5, saved.getValues().getEnergyKwh());
        assertEquals(100.0, saved.getValues().getSolarGenerationKwh());
        assertEquals(3000, saved.getValues().getWaterLitres());
        assertEquals(20, saved.getValues().getGeneralWasteKg());
        assertEquals(15, saved.getValues().getRecycledWasteKg());
        assertEquals(5, saved.getValues().getCompostKg());
        assertEquals(12, saved.getValues().getPublicTransportTripsPerWeek());
        assertEquals(2, saved.getValues().getFlightsPerYear());
    }

    @Test
    void scenarioForAHouseholdWithoutSolarKeepsSolarEmpty() {
        SustainabilityScenario noSolar = new SustainabilityScenario(350, null, 3000, 20, 15, 5, 12, 2);

        dao.create(HOUSEHOLD, USER, "No solar", noSolar, 50.0, 55.0);

        assertNull(dao.findByHousehold(HOUSEHOLD).get(0).getValues().getSolarGenerationKwh());
    }

    @Test
    void scenarioSavedByOneMemberIsVisibleToTheWholeHousehold() {
        dao.create(HOUSEHOLD, USER, "Shorter showers", values, 50.0, 62.5);
        dao.create(HOUSEHOLD, USER + 1, "Add solar", values, 50.0, 70.0);

        List<SavedScenario> scenarios = dao.findByHousehold(HOUSEHOLD);

        assertEquals(2, scenarios.size());
    }

    @Test
    void scenariosFromAnotherHouseholdAreNotListed() {
        dao.create(HOUSEHOLD, USER, "Ours", values, 50.0, 62.5);
        dao.create(OTHER_HOUSEHOLD, USER, "Theirs", values, 40.0, 45.0);

        List<SavedScenario> scenarios = dao.findByHousehold(HOUSEHOLD);

        assertEquals(1, scenarios.size());
        assertEquals("Ours", scenarios.get(0).getName());
    }

    @Test
    void deletedScenarioIsRemovedPermanently() throws Exception {
        SavedScenario obsolete = dao.create(HOUSEHOLD, USER, "Obsolete", values, 50.0, 62.5);
        dao.create(HOUSEHOLD, USER, "Keep", values, 50.0, 70.0);

        dao.delete(obsolete.getId(), HOUSEHOLD);

        connection.close();
        connection = open();
        List<SavedScenario> afterRestart = new SqliteScenarioDao(connection).findByHousehold(HOUSEHOLD);
        assertEquals(1, afterRestart.size());
        assertEquals("Keep", afterRestart.get(0).getName());
    }

    @Test
    void anotherHouseholdsScenarioCannotBeDeleted() {
        SavedScenario theirs = dao.create(OTHER_HOUSEHOLD, USER, "Theirs", values, 40.0, 45.0);

        dao.delete(theirs.getId(), HOUSEHOLD);

        assertEquals(1, dao.findByHousehold(OTHER_HOUSEHOLD).size());
    }

    @Test
    void deletingAScenarioLeavesHouseholdDataUnaffected() {
        SqliteWaterEntryDao waterEntryDao = new SqliteWaterEntryDao(connection);
        waterEntryDao.create(HOUSEHOLD, "2026-10", 4000, null, USER);
        SavedScenario scenario = dao.create(HOUSEHOLD, USER, "Shorter showers", values, 50.0, 62.5);

        dao.delete(scenario.getId(), HOUSEHOLD);

        assertEquals(1, waterEntryDao.findByHousehold(HOUSEHOLD).size());
        assertEquals(4000, waterEntryDao.findByHousehold(HOUSEHOLD).get(0).getLitres());
        assertTrue(dao.findByHousehold(HOUSEHOLD).isEmpty());
    }

    private Connection open() throws Exception {
        return DriverManager.getConnection("jdbc:sqlite:" + tempDir.resolve("scenarios-test.db"));
    }

    private static void applySchema(Connection connection) throws Exception {
        String schema;
        try (InputStream in = SqliteScenarioDaoTest.class.getResourceAsStream("/com/ecotwin/schema.sql")) {
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
