package com.ecotwin.dao.sqlite;

import com.ecotwin.model.ActivityLogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqliteActivityLogDaoTest {

    private Connection connection;
    private SqliteActivityLogDao dao;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        connection.createStatement().execute(
                "CREATE TABLE activity_log (id INTEGER PRIMARY KEY AUTOINCREMENT, household_id INTEGER NOT NULL, "
                        + "actor_user_id INTEGER, message TEXT NOT NULL, created_at TEXT NOT NULL)");
        connection.createStatement().execute(
                "CREATE TABLE household_memberships (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, "
                        + "household_id INTEGER NOT NULL, role TEXT NOT NULL, joined_at TEXT NOT NULL, left_at TEXT)");
        dao = new SqliteActivityLogDao(connection);
    }

    @Test
    void historyComesBackNewestFirst() {
        dao.log(1, 2L, "Joiner joined the household");
        dao.log(1, 2L, "Joiner left the household");

        List<ActivityLogEntry> history = dao.findByHousehold(1);

        assertEquals("Joiner left the household", history.get(0).getMessage());
        assertEquals("Joiner joined the household", history.get(1).getMessage());
    }

    @Test
    void historySurvivesAMemberLeaving() throws SQLException {
        connection.createStatement().execute(
                "INSERT INTO household_memberships (id, user_id, household_id, role, joined_at) "
                        + "VALUES (1, 2, 1, 'MEMBER', '2026-10-01T00:00:00Z')");
        dao.log(1, 2L, "Joiner joined the household");

        new SqliteHouseholdMembershipDao(connection).leaveHousehold(1);
        dao.log(1, 2L, "Joiner left the household");

        List<ActivityLogEntry> history = dao.findByHousehold(1);

        assertEquals(2, history.size());
        assertEquals(2L, history.get(1).getActorUserId());
    }
}