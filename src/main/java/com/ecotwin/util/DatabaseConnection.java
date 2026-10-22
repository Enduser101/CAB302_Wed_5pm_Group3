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
 *
 * <p>The whole app shares one {@link Connection}, so every DAO sees the same data and the
 * schema is only created once. {@link com.ecotwin.AppContext} passes the connection on to the
 * DAOs, so tests can give them an in-memory or temporary database instead.
 */
public final class DatabaseConnection {

    private static final String DB_FILE = "ecotwin.db";
    private static final String SCHEMA_RESOURCE = "/com/ecotwin/schema.sql";

    private static DatabaseConnection instance;

    private final Connection connection;

    /**
     * Opens {@value #DB_FILE} in the working directory and creates any missing tables.
     *
     * @throws IllegalStateException if the database file cannot be opened
     */
    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
            applySchema();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not open database at " + DB_FILE, e);
        }
    }

    /**
     * Returns the single shared instance, opening the database the first time it is called.
     * Synchronized so two threads cannot both create a connection.
     *
     * @return the shared database connection holder
     * @throws IllegalStateException if the database or schema cannot be loaded
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Returns the open SQLite connection. Callers should not close it, as the rest of the app
     * is still using it.
     *
     * @return the shared JDBC connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Runs each statement in schema.sql. The schema uses {@code CREATE TABLE IF NOT EXISTS},
     * so running it on an existing database leaves the data alone.
     *
     * @throws SQLException if a statement in the schema fails
     */
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

    /**
     * Strips "-- ..." line comments before splitting on ';', so a semicolon inside a comment
     * can't be mistaken for a statement terminator.
     *
     * @param sql the raw schema text
     * @return the schema with line comments removed
     */
    private String stripLineComments(String sql) {
        StringBuilder result = new StringBuilder();
        for (String line : sql.split("\n")) {
            int commentStart = line.indexOf("--");
            result.append(commentStart == -1 ? line : line.substring(0, commentStart)).append('\n');
        }
        return result.toString();
    }

    /**
     * Reads schema.sql from the classpath.
     *
     * @return the schema text
     * @throws IllegalStateException if the resource is missing or unreadable
     */
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
