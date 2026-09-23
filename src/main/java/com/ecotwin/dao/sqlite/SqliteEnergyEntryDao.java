package com.ecotwin.dao.sqlite;

import com.ecotwin.dao.EnergyEntryDao;
import com.ecotwin.model.EnergyEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SqliteEnergyEntryDao implements EnergyEntryDao {

    private final Connection connection;

    public SqliteEnergyEntryDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public EnergyEntry create(long householdId, String period, double electricityKwh, Double solarGenerationKwh,
                               String notes, Long updatedByUserId) {
        String updatedAt = Instant.now().toString();
        String sql = "INSERT INTO energy_entries "
            + "(household_id, period, electricity_kwh, solar_generation_kwh, notes, updated_by_user_id, updated_at) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, householdId);
            statement.setString(2, period);
            statement.setDouble(3, electricityKwh);
            if (solarGenerationKwh == null) {
                statement.setNull(4, Types.REAL);
            } else {
                statement.setDouble(4, solarGenerationKwh);
            }
            statement.setString(5, notes);
            if (updatedByUserId == null) {
                statement.setNull(6, Types.INTEGER);
            } else {
                statement.setLong(6, updatedByUserId);
            }
            statement.setString(7, updatedAt);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return new EnergyEntry(keys.getLong(1), householdId, period, electricityKwh, solarGenerationKwh,
                    notes, updatedByUserId, updatedAt);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not record energy entry for household " + householdId, e);
        }
    }
    @Override
    public EnergyEntry update(long entryId, double electricityKwh, Double solarGenerationKwh,
                              String notes, long updatedByUserId) {
        String updatedAt = Instant.now().toString();

        String sql = "UPDATE energy_entries SET electricity_kwh = ?, solar_generation_kwh = ?, "
                + "notes = ?, updated_by_user_id = ?, updated_at = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, electricityKwh);

            if (solarGenerationKwh == null) {
                statement.setNull(2, Types.REAL);
            } else {
                statement.setDouble(2, solarGenerationKwh);
            }

            statement.setString(3, notes);
            statement.setLong(4, updatedByUserId);
            statement.setString(5, updatedAt);
            statement.setLong(6, entryId);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not update energy entry " + entryId, e);
        }

        String findSql = "SELECT * FROM energy_entries WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(findSql)) {
            statement.setLong(1, entryId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not find energy entry " + entryId, e);
        }

        throw new IllegalArgumentException("Energy entry not found");
    }

    @Override
    public List<EnergyEntry> findByHousehold(long householdId) {
        String sql = "SELECT * FROM energy_entries WHERE household_id = ? ORDER BY id DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, householdId);
            try (ResultSet rs = statement.executeQuery()) {
                List<EnergyEntry> entries = new ArrayList<>();
                while (rs.next()) {
                    entries.add(map(rs));
                }
                return entries;
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Energy entry not found");
        }

    }

    private EnergyEntry map(ResultSet rs) throws SQLException {
        double solar = rs.getDouble("solar_generation_kwh");
        Double solarValue = rs.wasNull() ? null : solar;
        long updatedBy = rs.getLong("updated_by_user_id");
        Long updatedByValue = rs.wasNull() ? null : updatedBy;
        return new EnergyEntry(
            rs.getLong("id"),
            rs.getLong("household_id"),
            rs.getString("period"),
            rs.getDouble("electricity_kwh"),
            solarValue,
            rs.getString("notes"),
            updatedByValue,
            rs.getString("updated_at")
        );
    }
}
