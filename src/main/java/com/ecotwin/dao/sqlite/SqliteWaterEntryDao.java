package com.ecotwin.dao.sqlite;

import com.ecotwin.dao.WaterEntryDao;
import com.ecotwin.model.WaterEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SqliteWaterEntryDao implements WaterEntryDao {

    private final Connection connection;

    public SqliteWaterEntryDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public WaterEntry create(long householdId, String period, double litres, String notes, Long updatedByUserId) {
        String updatedAt = Instant.now().toString();
        String sql = "INSERT INTO water_entries (household_id, period, litres, notes, updated_by_user_id, updated_at) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, householdId);
            statement.setString(2, period);
            statement.setDouble(3, litres);
            statement.setString(4, notes);
            if (updatedByUserId == null) {
                statement.setNull(5, Types.INTEGER);
            } else {
                statement.setLong(5, updatedByUserId);
            }
            statement.setString(6, updatedAt);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return new WaterEntry(keys.getLong(1), householdId, period, litres, notes, updatedByUserId, updatedAt);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not record water entry for household " + householdId, e);
        }
    }

    @Override
    public List<WaterEntry> findByHousehold(long householdId) {
        String sql = "SELECT * FROM water_entries WHERE household_id = ? ORDER BY id DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, householdId);
            try (ResultSet rs = statement.executeQuery()) {
                List<WaterEntry> entries = new ArrayList<>();
                while (rs.next()) {
                    entries.add(map(rs));
                }
                return entries;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not look up water entries for household " + householdId, e);
        }
    }

    @Override
    public WaterEntry update(long entryId, double litres, String notes,
                             long updatedByUserId) {
        String updatedAt = Instant.now().toString();

        String sql = "UPDATE water_entries SET litres = ?, notes = ?, "
                + "updated_by_user_id = ?, updated_at = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, litres);
            statement.setString(2, notes);
            statement.setLong(3, updatedByUserId);
            statement.setString(4, updatedAt);
            statement.setLong(5, entryId);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not update water entry " + entryId, e);
        }

        String findSql = "SELECT * FROM water_entries WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(findSql)) {
            statement.setLong(1, entryId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not find water entry " + entryId, e);
        }

        throw new IllegalArgumentException("Water entry not found");
    }

    private WaterEntry map(ResultSet rs) throws SQLException {
        long updatedBy = rs.getLong("updated_by_user_id");
        Long updatedByValue = rs.wasNull() ? null : updatedBy;

        return new WaterEntry(
                rs.getLong("id"),
                rs.getLong("household_id"),
                rs.getString("period"),
                rs.getDouble("litres"),
                rs.getString("notes"),
                updatedByValue,
                rs.getString("updated_at")
        );
    }
}
