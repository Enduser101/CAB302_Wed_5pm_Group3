package com.ecotwin.dao.sqlite;

import com.ecotwin.dao.ScenarioDao;
import com.ecotwin.model.SavedScenario;
import com.ecotwin.model.SustainabilityScenario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqliteScenarioDao implements ScenarioDao {

    // The scenario screen edits all four domains at once, so there is no single source domain.
    private static final String ALL_DOMAINS = "ALL";

    private final Connection connection;

    public SqliteScenarioDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public SavedScenario create(long householdId, Long createdByUserId, String name,
                                SustainabilityScenario values, double baselineScore, double projectedScore) {
        String createdAt = Instant.now().toString();
        String sql = "INSERT INTO scenarios (household_id, created_by_user_id, name, domain, changes_json, "
            + "baseline_score, projected_score, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, householdId);
            if (createdByUserId == null) {
                statement.setNull(2, Types.INTEGER);
            } else {
                statement.setLong(2, createdByUserId);
            }
            statement.setString(3, name);
            statement.setString(4, ALL_DOMAINS);
            statement.setString(5, toJson(values));
            statement.setDouble(6, baselineScore);
            statement.setDouble(7, projectedScore);
            statement.setString(8, createdAt);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return new SavedScenario(keys.getLong(1), householdId, createdByUserId, name,
                    values.copy(), baselineScore, projectedScore, createdAt);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not save scenario for household " + householdId, e);
        }
    }

    @Override
    public List<SavedScenario> findByHousehold(long householdId) {
        String sql = "SELECT * FROM scenarios WHERE household_id = ? ORDER BY id DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, householdId);
            try (ResultSet rs = statement.executeQuery()) {
                List<SavedScenario> scenarios = new ArrayList<>();
                while (rs.next()) {
                    scenarios.add(map(rs));
                }
                return scenarios;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not look up scenarios for household " + householdId, e);
        }
    }

    @Override
    public void delete(long scenarioId, long householdId) {
        String sql = "DELETE FROM scenarios WHERE id = ? AND household_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, scenarioId);
            statement.setLong(2, householdId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not delete scenario " + scenarioId, e);
        }
    }

    private SavedScenario map(ResultSet rs) throws SQLException {
        long createdBy = rs.getLong("created_by_user_id");
        Long createdByValue = rs.wasNull() ? null : createdBy;

        return new SavedScenario(
                rs.getLong("id"),
                rs.getLong("household_id"),
                createdByValue,
                rs.getString("name"),
                fromJson(rs.getString("changes_json")),
                rs.getDouble("baseline_score"),
                rs.getDouble("projected_score"),
                rs.getString("created_at")
        );
    }

    // changes_json is a flat object of numbers, so it is written and read by hand
    // rather than adding a JSON library to the build.
    private static String toJson(SustainabilityScenario values) {
        return "{"
            + "\"energyKwh\":" + values.getEnergyKwh()
            + ",\"solarGenerationKwh\":" + values.getSolarGenerationKwh()
            + ",\"waterLitres\":" + values.getWaterLitres()
            + ",\"generalWasteKg\":" + values.getGeneralWasteKg()
            + ",\"recycledWasteKg\":" + values.getRecycledWasteKg()
            + ",\"compostKg\":" + values.getCompostKg()
            + ",\"publicTransportTripsPerWeek\":" + values.getPublicTransportTripsPerWeek()
            + ",\"flightsPerYear\":" + values.getFlightsPerYear()
            + "}";
    }

    private static SustainabilityScenario fromJson(String json) {
        return new SustainabilityScenario(
                number(json, "energyKwh"),
                number(json, "solarGenerationKwh"),
                number(json, "waterLitres"),
                number(json, "generalWasteKg"),
                number(json, "recycledWasteKg"),
                number(json, "compostKg"),
                number(json, "publicTransportTripsPerWeek"),
                number(json, "flightsPerYear")
        );
    }

    private static Double number(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*([^,}\\s]+)").matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("Saved scenario is missing " + key + ": " + json);
        }
        String value = matcher.group(1);
        return value.equals("null") ? null : Double.valueOf(value);
    }
}
