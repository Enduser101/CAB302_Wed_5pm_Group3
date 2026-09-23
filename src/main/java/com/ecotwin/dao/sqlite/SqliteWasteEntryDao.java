package com.ecotwin.dao.sqlite;

import com.ecotwin.dao.WasteEntryDao;
import com.ecotwin.model.WasteEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SqliteWasteEntryDao implements WasteEntryDao {

    private final Connection connection;

    public SqliteWasteEntryDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public WasteEntry create(long householdId, String period, double generalKg, double recycledKg, double compostKg,
                              Long updatedByUserId) {
        String updatedAt = Instant.now().toString();
        String sql = "INSERT INTO waste_entries "
            + "(household_id, period, general_kg, recycled_kg, compost_kg, updated_by_user_id, updated_at) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, householdId);
            statement.setString(2, period);
            statement.setDouble(3, generalKg);
            statement.setDouble(4, recycledKg);
            statement.setDouble(5, compostKg);
            if (updatedByUserId == null) {
                statement.setNull(6, Types.INTEGER);
            } else {
                statement.setLong(6, updatedByUserId);
            }
            statement.setString(7, updatedAt);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return new WasteEntry(keys.getLong(1), householdId, period, generalKg, recycledKg, compostKg,
                    updatedByUserId, updatedAt);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not record waste entry for household " + householdId, e);
        }
    }

    @Override
    public List<WasteEntry> findByHousehold(long householdId) {
        String sql = "SELECT * FROM waste_entries WHERE household_id = ? ORDER BY id DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, householdId);
            try (ResultSet rs = statement.executeQuery()) {
                List<WasteEntry> entries = new ArrayList<>();
                while (rs.next()) {
                    entries.add(map(rs));
                }
                return entries;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not look up waste entries for household " + householdId, e);
        }
    }
    @Override
    public WasteEntry update(long entryId, double generalKg, double recycledKg,
                             double compostKg, long updatedByUserId) {

        String updatedAt = Instant.now().toString();

        String sql = "UPDATE waste_entries SET general_kg = ?, recycled_kg = ?, "
                + "compost_kg = ?, updated_by_user_id = ?, updated_at = ? "
                + "WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, generalKg);
            statement.setDouble(2, recycledKg);
            statement.setDouble(3, compostKg);
            statement.setLong(4, updatedByUserId);
            statement.setString(5, updatedAt);
            statement.setLong(6, entryId);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not update waste entry " + entryId, e
            );
        }

        String findSql = "SELECT * FROM waste_entries WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(findSql)) {
            statement.setLong(1, entryId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not find waste entry " + entryId, e
            );
        }

        throw new IllegalArgumentException("Waste entry not found");
    }

    private WasteEntry map(ResultSet rs) throws SQLException {
        long updatedBy = rs.getLong("updated_by_user_id");
        Long updatedByValue = rs.wasNull() ? null : updatedBy;
        return new WasteEntry(
            rs.getLong("id"),
            rs.getLong("household_id"),
            rs.getString("period"),
            rs.getDouble("general_kg"),
            rs.getDouble("recycled_kg"),
            rs.getDouble("compost_kg"),
            updatedByValue,
            rs.getString("updated_at")
        );
    }
}
