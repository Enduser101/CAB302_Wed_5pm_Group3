package com.ecotwin.dao.sqlite;

import com.ecotwin.dao.ActivityLogDao;
import com.ecotwin.model.ActivityLogEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<ActivityLogEntry> findByHousehold(long householdId) {
        String sql = "SELECT id, household_id, actor_user_id, message, created_at "
                + "FROM activity_log WHERE household_id = ? ORDER BY id DESC";
        List<ActivityLogEntry> history = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, householdId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    long actor = rows.getLong("actor_user_id");
                    Long actorUserId = rows.wasNull() ? null : actor;
                    history.add(new ActivityLogEntry(
                            rows.getLong("id"),
                            rows.getLong("household_id"),
                            actorUserId,
                            rows.getString("message"),
                            rows.getString("created_at")));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read activity log", e);
        }
        return history;
    }
}