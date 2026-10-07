package com.ecotwin.dao.sqlite;

public class SqliteActivityLogDaoTest {
}
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

    private SqliteActivityLogDao dao;

    @BeforeEach
    void setUp() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        connection.createStatement().execute(
                "CREATE TABLE activity_log (id INTEGER PRIMARY KEY AUTOINCREMENT, household_id INTEGER NOT NULL, "
                        + "actor_user_id INTEGER, message TEXT NOT NULL, created_at TEXT NOT NULL)");
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
}