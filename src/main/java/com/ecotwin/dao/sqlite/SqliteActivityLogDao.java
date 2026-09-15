package com.ecotwin.dao.sqlite;

import com.ecotwin.dao.ActivityLogDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;

public class SqliteActivityLogDao implements ActivityLogDao {

    private final Connection connection;

    public SqliteActivityLogDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void log(long householdId, Long actorUserId, String message) {
        String sql = "INSERT INTO activity_log (household_id, actor_user_id, message, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, householdId);
            if (actorUserId == null) {
                statement.setNull(2, Types.INTEGER);
            } else {
                statement.setLong(2, actorUserId);
            }
            statement.setString(3, message);
            statement.setString(4, Instant.now().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not write activity log entry", e);
        }
    }
}
