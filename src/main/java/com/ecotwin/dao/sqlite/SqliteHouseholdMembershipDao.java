package com.ecotwin.dao.sqlite;

import com.ecotwin.dao.HouseholdMembershipDao;
import com.ecotwin.model.HouseholdMembership;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Optional;

public class SqliteHouseholdMembershipDao implements HouseholdMembershipDao {

    private final Connection connection;

    public SqliteHouseholdMembershipDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public HouseholdMembership addMember(long userId, long householdId, HouseholdMembership.Role role) {
        String joinedAt = Instant.now().toString();
        String sql = "INSERT INTO household_memberships (user_id, household_id, role, joined_at, left_at) "
            + "VALUES (?, ?, ?, ?, NULL)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, userId);
            statement.setLong(2, householdId);
            statement.setString(3, role.name());
            statement.setString(4, joinedAt);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return new HouseholdMembership(keys.getLong(1), userId, householdId, role, joinedAt, null);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not add member to household " + householdId, e);
        }
    }

    @Override
    public Optional<HouseholdMembership> findActiveByUserAndHousehold(long userId, long householdId) {
        String sql = "SELECT * FROM household_memberships WHERE user_id = ? AND household_id = ? AND left_at IS NULL";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, householdId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not look up membership", e);
        }
    }

    @Override
    public Optional<HouseholdMembership> findAnyActiveByUser(long userId) {
        String sql = "SELECT * FROM household_memberships WHERE user_id = ? AND left_at IS NULL ORDER BY joined_at LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not look up active membership for user " + userId, e);
        }
    }

    private HouseholdMembership map(ResultSet rs) throws SQLException {
        return new HouseholdMembership(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getLong("household_id"),
            HouseholdMembership.Role.valueOf(rs.getString("role")),
            rs.getString("joined_at"),
            rs.getString("left_at")
        );
    }
}
