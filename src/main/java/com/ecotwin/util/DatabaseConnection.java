package com.ecotwin.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton JDBC entry point. Opens the SQLite file and applies schema.sql on first use.
 */
public final class DatabaseConnection {

    private static final String DB_FILE = "ecotwin.db";
    private static final String SCHEMA_RESOURCE = "/com/ecotwin/schema.sql";

    private static DatabaseConnection instance;

    private final Connection connection;

    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
            applySchema();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not open database at " + DB_FILE, e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void applySchema() throws SQLException {
        String schemaSql = stripLineComments(readSchemaResource());
        for (String rawStatement : schemaSql.split(";")) {
            String trimmed = rawStatement.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            // A fresh Statement per call - reusing one Statement across many execute() calls
            // triggers a "prepared statement has been finalized" error in sqlite-jdbc.
            try (Statement statement = connection.createStatement()) {
                statement.execute(trimmed);
            }
        }
    }

    /** Strips "-- ..." line comments before splitting on ';', so a semicolon inside a comment
     *  can't be mistaken for a statement terminator. */
    private String stripLineComments(String sql) {
        StringBuilder result = new StringBuilder();
        for (String line : sql.split("\n")) {
            int commentStart = line.indexOf("--");
            result.append(commentStart == -1 ? line : line.substring(0, commentStart)).append('\n');
        }
        return result.toString();
    }

    private String readSchemaResource() {
        try (InputStream in = DatabaseConnection.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing schema resource: " + SCHEMA_RESOURCE);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read schema resource: " + SCHEMA_RESOURCE, e);
        }
    }
}
