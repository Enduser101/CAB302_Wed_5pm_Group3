package com.ecotwin.util;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseConnectionTest {

    @Test
    void applyingTheSchemaCreatesEveryExpectedTable() throws Exception {
        Connection connection = DatabaseConnection.getInstance().getConnection();

        List<String> tables = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getTables(null, null, "%", new String[] {"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME").toLowerCase());
            }
        }

        List<String> expected = List.of(
            "users", "households", "household_memberships",
            "energy_entries", "water_entries", "waste_entries",
            "vehicles", "transport_entries", "scenarios", "activity_log"
        );
        for (String table : expected) {
            assertTrue(tables.contains(table), "Expected table " + table + " to exist, found: " + tables);
        }
    }

    @Test
    void getInstanceAlwaysReturnsTheSameConnectionWrapper() {
        assertTrue(DatabaseConnection.getInstance() == DatabaseConnection.getInstance());
    }
}
