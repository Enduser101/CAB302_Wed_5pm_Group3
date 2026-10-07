package com.ecotwin.dao.sqlite;

import com.ecotwin.dao.VehicleDao;
import com.ecotwin.model.Vehicle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqliteVehicleDao implements VehicleDao {

    private final Connection connection;

    public SqliteVehicleDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Vehicle create(long householdId, String label, String fuelType, double kmPerWeek) {
        String sql = "INSERT INTO vehicles (household_id, label, fuel_type, km_per_week) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, householdId);
            statement.setString(2, label);
            statement.setString(3, fuelType);
            statement.setDouble(4, kmPerWeek);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return new Vehicle(keys.getLong(1), householdId, label, fuelType, kmPerWeek);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not add vehicle for household " + householdId, e);
        }
    }

    @Override
    public List<Vehicle> findByHousehold(long householdId) {
        String sql = "SELECT * FROM vehicles WHERE household_id = ? ORDER BY id ASC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, householdId);
            try (ResultSet rs = statement.executeQuery()) {
                List<Vehicle> vehicles = new ArrayList<>();
                while (rs.next()) {
                    vehicles.add(map(rs));
                }
                return vehicles;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not look up vehicles for household " + householdId, e);
        }
    }

    private Vehicle map(ResultSet rs) throws SQLException {
        return new Vehicle(
            rs.getLong("id"),
            rs.getLong("household_id"),
            rs.getString("label"),
            rs.getString("fuel_type"),
            rs.getDouble("km_per_week")
        );
    }
}
