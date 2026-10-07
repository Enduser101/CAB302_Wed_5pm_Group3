package com.ecotwin.dao.sqlite;

import com.ecotwin.dao.TransportEntryDao;
import com.ecotwin.model.TransportEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SqliteTransportEntryDao implements TransportEntryDao {

    private final Connection connection;

    public SqliteTransportEntryDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public TransportEntry upsertForPeriod(long householdId, String period, double publicTransportTripsPerWeek,
                                           double flightsPerYear, Long updatedByUserId) {
        String updatedAt = Instant.now().toString();
        // schema.sql has UNIQUE (household_id, period) on transport_entries - recording again for the
        // same period is meant to update the existing row, not append a duplicate.
        String sql = "INSERT INTO transport_entries "
            + "(household_id, period, public_transport_trips_per_week, flights_per_year, updated_by_user_id, updated_at) "
            + "VALUES (?, ?, ?, ?, ?, ?) "
            + "ON CONFLICT(household_id, period) DO UPDATE SET "
            + "public_transport_trips_per_week = excluded.public_transport_trips_per_week, "
            + "flights_per_year = excluded.flights_per_year, "
            + "updated_by_user_id = excluded.updated_by_user_id, "
            + "updated_at = excluded.updated_at "
            + "RETURNING *";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, householdId);
            statement.setString(2, period);
            statement.setDouble(3, publicTransportTripsPerWeek);
            statement.setDouble(4, flightsPerYear);
            if (updatedByUserId == null) {
                statement.setNull(5, Types.INTEGER);
            } else {
                statement.setLong(5, updatedByUserId);
            }
            statement.setString(6, updatedAt);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return map(rs);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not record transport entry for household " + householdId, e);
        }
    }

    @Override
    public List<TransportEntry> findByHousehold(long householdId) {
        String sql = "SELECT * FROM transport_entries WHERE household_id = ? ORDER BY period DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, householdId);
            try (ResultSet rs = statement.executeQuery()) {
                List<TransportEntry> entries = new ArrayList<>();
                while (rs.next()) {
                    entries.add(map(rs));
                }
                return entries;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not look up transport entries for household " + householdId, e);
        }
    }

    private TransportEntry map(ResultSet rs) throws SQLException {
        long updatedBy = rs.getLong("updated_by_user_id");
        Long updatedByValue = rs.wasNull() ? null : updatedBy;
        return new TransportEntry(
            rs.getLong("id"),
            rs.getLong("household_id"),
            rs.getString("period"),
            rs.getDouble("public_transport_trips_per_week"),
            rs.getDouble("flights_per_year"),
            updatedByValue,
            rs.getString("updated_at")
        );
    }
}
