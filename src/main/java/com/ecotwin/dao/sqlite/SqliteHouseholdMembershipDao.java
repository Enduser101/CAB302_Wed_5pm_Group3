package com.ecotwin.dao.sqlite;

import com.ecotwin.dao.HouseholdMembershipDao;
import com.ecotwin.model.HouseholdMembership;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

    // US-10: leave a household
    @Override
    public void leaveHousehold(long membershipId) {
        String sql = "UPDATE household_memberships SET left_at = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, Instant.now().toString());
            statement.setLong(2, membershipId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not leave household", e);
        }
    }

    // US-11: view current household members
    @Override
    public List<HouseholdMembership> findActiveByHousehold(long householdId) {
        // Show the admin first
        String sql = "SELECT * FROM household_memberships WHERE household_id = ? AND left_at IS NULL "
                + "ORDER BY CASE WHEN role = 'ADMIN' THEN 0 ELSE 1 END, joined_at";
        List<HouseholdMembership> memberships = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, householdId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    memberships.add(map(rs));
                }
            }

            return memberships;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not look up household members", e);
        }
    }

    // US-35: transfer administrator rights to another active member
    @Override
    public void transferAdmin(long householdId, long currentAdminUserId, long newAdminUserId) {
        String sql = """
            UPDATE household_memberships
            SET role = CASE
                WHEN user_id = ? THEN 'MEMBER'
                WHEN user_id = ? THEN 'ADMIN'
            END
            WHERE household_id = ?
              AND left_at IS NULL
              AND user_id IN (?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, currentAdminUserId);
            statement.setLong(2, newAdminUserId);
            statement.setLong(3, householdId);
            statement.setLong(4, currentAdminUserId);
            statement.setLong(5, newAdminUserId);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not transfer administrator rights", e);
        }
    }
}
