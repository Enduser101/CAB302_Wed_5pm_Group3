package com.ecotwin.dao.sqlite;

import com.ecotwin.dao.HouseholdDao;
import com.ecotwin.model.Household;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Optional;

public class SqliteHouseholdDao implements HouseholdDao {

    private final Connection connection;

    public SqliteHouseholdDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Household create(String name, int occupants, String dwellingType, String state, String joinCode) {
        String createdAt = Instant.now().toString();
        String sql = "INSERT INTO households (name, join_code, occupants, dwelling_type, state, created_at) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, joinCode);
            statement.setInt(3, occupants);
            statement.setString(4, dwellingType);
            statement.setString(5, state);
            statement.setString(6, createdAt);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return new Household(keys.getLong(1), name, joinCode, occupants, dwellingType, state, createdAt);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create household " + name, e);
        }
    }

    @Override
    public Optional<Household> findById(long id) {
        String sql = "SELECT * FROM households WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not look up household " + id, e);
        }
    }

    @Override
    public Optional<Household> findByJoinCode(String joinCode) {
        String sql = "SELECT * FROM households WHERE join_code = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, joinCode);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not look up household by join code", e);
        }
    }

    private Household map(ResultSet rs) throws SQLException {
        return new Household(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("join_code"),
            rs.getInt("occupants"),
            rs.getString("dwelling_type"),
            rs.getString("state"),
            rs.getString("created_at")
        );
    }
}
