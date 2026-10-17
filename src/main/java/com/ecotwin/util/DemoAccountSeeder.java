package com.ecotwin.util;

import com.ecotwin.AppContext;
import com.ecotwin.dao.ActivityLogDao;
import com.ecotwin.dao.EnergyEntryDao;
import com.ecotwin.dao.TransportEntryDao;
import com.ecotwin.dao.WasteEntryDao;
import com.ecotwin.dao.WaterEntryDao;
import com.ecotwin.dao.sqlite.SqliteActivityLogDao;
import com.ecotwin.dao.sqlite.SqliteEnergyEntryDao;
import com.ecotwin.dao.sqlite.SqliteTransportEntryDao;
import com.ecotwin.dao.sqlite.SqliteUserDao;
import com.ecotwin.dao.sqlite.SqliteWasteEntryDao;
import com.ecotwin.dao.sqlite.SqliteWaterEntryDao;
import com.ecotwin.model.EnergyEntry;
import com.ecotwin.model.Household;
import com.ecotwin.model.TransportEntry;
import com.ecotwin.model.User;
import com.ecotwin.model.Vehicle;
import com.ecotwin.model.WasteEntry;
import com.ecotwin.model.WaterEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * #115: creates the "Cab 302" demo account on first launch, so the dashboard, score chart and
 * activity history have something to show.
 *
 * Six months of readings (May - Oct 2026) that rise and fall, then a month of edits
 * (18 Sep - 17 Oct 2026). Periods are fixed so the demo looks the same whenever it is run.
 * Readings are created through the DAOs (the services always record the current month);
 * edits go through the services so the history wording is exactly what the app writes.
 * Each change is then backdated to when it "happened".
 */
public class DemoAccountSeeder {

    public static final String USERNAME = "Cab 302";
    public static final String PASSWORD = "Testuser1";

    private static final ZoneId BRISBANE = ZoneId.of("Australia/Brisbane");

    private final Connection connection;
    private final AppContext ctx;
    private final EnergyEntryDao energyDao;
    private final WaterEntryDao waterDao;
    private final WasteEntryDao wasteDao;
    private final TransportEntryDao transportDao;
    private final ActivityLogDao activityLogDao;

    private User user;
    private Household household;

    public DemoAccountSeeder(Connection connection, AppContext ctx) {
        this.connection = connection;
        this.ctx = ctx;
        this.energyDao = new SqliteEnergyEntryDao(connection);
        this.waterDao = new SqliteWaterEntryDao(connection);
        this.wasteDao = new SqliteWasteEntryDao(connection);
        this.transportDao = new SqliteTransportEntryDao(connection);
        this.activityLogDao = new SqliteActivityLogDao(connection);
    }

    /** @return true if the demo account was created, false if it already existed */
    public boolean seedIfMissing() {
        if (new SqliteUserDao(connection).findByUsername(USERNAME).isPresent()) {
            return false;
        }

        user = ctx.authService.register(USERNAME, "cab302@example.com", PASSWORD);
        household = ctx.householdService.createHousehold(user, "CAB302 Demo House", 2, "House", "QLD");
        String created = at(5, 1, 10, 0);
        backdate("UPDATE users SET created_at = ? WHERE id = ?", created, user.getId());
        backdate("UPDATE households SET created_at = ? WHERE id = ?", created, household.getId());
        backdate("UPDATE household_memberships SET joined_at = ? WHERE household_id = ?", created, household.getId());
        backdateLatestHistory(created);

        recordPastMonths();
        recordSeptember();
        recordOctober();
        return true;
    }

    /** May - Aug: heating pushes energy up over winter, a June holiday adds flights, August is dry. */
    private void recordPastMonths() {
        recordMonth("2026-05", 460, 10500, 32, 8, 2, 5, 2, at(6, 2, 19, 30));
        recordMonth("2026-06", 520, 9000, 28, 10, 3, 3, 4, at(7, 1, 19, 30));
        recordMonth("2026-07", 560, 8500, 35, 7, 2, 4, 3, at(8, 3, 19, 30));
        recordMonth("2026-08", 470, 11000, 26, 12, 4, 6, 2, at(9, 2, 19, 30));
    }

    private void recordMonth(String period, double kwh, double litres, double general, double recycled,
                             double compost, double tripsPerWeek, double flightsPerYear, String when) {
        energy(period, kwh, null, when);
        water(period, litres, when);
        waste(period, general, recycled, compost, when);
        transport(period, tripsPerWeek, flightsPerYear, when);
    }

    /** Recorded mid-month, then corrected once the bills arrived. */
    private void recordSeptember() {
        EnergyEntry energy = energy("2026-09", 450, null, at(9, 18, 9, 12));
        WaterEntry water = water("2026-09", 12800, at(9, 18, 9, 15));
        waste("2026-09", 30, 10, 3, at(9, 21, 18, 40));
        transport("2026-09", 4, 3, at(9, 21, 18, 44));

        ctx.waterService.updateEntry(user, household, water.getId(), 12000, "Corrected from water bill");
        backdateEntry("water_entries", water.getId(), at(9, 25, 20, 5));
        ctx.energyService.updateEntry(user, household, energy.getId(), 420, null, "Corrected from power bill");
        backdateEntry("energy_entries", energy.getId(), at(9, 29, 19, 50));
    }

    /** Solar goes in, showers get shorter and the car is used less. */
    private void recordOctober() {
        EnergyEntry energy = energy("2026-10", 410, null, at(10, 2, 8, 30));
        Vehicle car = ctx.transportService.addVehicle(user, household, "Family car", "Petrol", 180);
        backdateLatestHistory(at(10, 3, 12, 10));
        WaterEntry water = water("2026-10", 10400, at(10, 5, 19, 0));

        ctx.energyService.updateEntry(user, household, energy.getId(), 395, 60.0, "Solar panels installed");
        backdateEntry("energy_entries", energy.getId(), at(10, 8, 17, 45));

        WasteEntry waste = waste("2026-10", 27, 12, 4, at(10, 9, 20, 15));

        ctx.transportService.updateVehicle(user, household, car.getId(), "Family car", "Petrol", 150);
        backdateLatestHistory(at(10, 11, 10, 20));

        TransportEntry transport = transport("2026-10", 6, 2, at(10, 12, 18, 5));

        ctx.waterService.updateEntry(user, household, water.getId(), 9500, "Shorter showers");
        backdateEntry("water_entries", water.getId(), at(10, 14, 21, 0));
        ctx.wasteService.updateEntry(user, household, waste.getId(), 24, 14, 6);
        backdateEntry("waste_entries", waste.getId(), at(10, 15, 19, 30));
        ctx.transportService.updateEntry(user, household, transport.getId(), 8, 2);
        backdateEntry("transport_entries", transport.getId(), at(10, 16, 8, 0));
        ctx.energyService.updateEntry(user, household, energy.getId(), 380, 60.0, "Solar panels installed");
        backdateEntry("energy_entries", energy.getId(), at(10, 17, 9, 0));
    }

    private EnergyEntry energy(String period, double kwh, Double solarKwh, String when) {
        EnergyEntry entry = energyDao.create(household.getId(), period, kwh, solarKwh, null, user.getId());
        log("recorded energy usage for " + period);
        backdateEntry("energy_entries", entry.getId(), when);
        return entry;
    }

    private WaterEntry water(String period, double litres, String when) {
        WaterEntry entry = waterDao.create(household.getId(), period, litres, null, user.getId());
        log("recorded water usage for " + period);
        backdateEntry("water_entries", entry.getId(), when);
        return entry;
    }

    private WasteEntry waste(String period, double general, double recycled, double compost, String when) {
        WasteEntry entry = wasteDao.create(household.getId(), period, general, recycled, compost, user.getId());
        log("recorded waste for " + period);
        backdateEntry("waste_entries", entry.getId(), when);
        return entry;
    }

    private TransportEntry transport(String period, double tripsPerWeek, double flightsPerYear, String when) {
        TransportEntry entry = transportDao.upsertForPeriod(
                household.getId(), period, tripsPerWeek, flightsPerYear, user.getId());
        log("updated transport information for " + period);
        backdateEntry("transport_entries", entry.getId(), when);
        return entry;
    }

    /** Same wording the services use when recording a new reading. */
    private void log(String action) {
        activityLogDao.log(household.getId(), user.getId(), USERNAME + " " + action);
    }

    /** Backdates a reading and the history entry its change just wrote. */
    private void backdateEntry(String table, long entryId, String when) {
        backdate("UPDATE " + table + " SET updated_at = ? WHERE id = ?", when, entryId);
        backdateLatestHistory(when);
    }

    private void backdateLatestHistory(String when) {
        backdate("UPDATE activity_log SET created_at = ? WHERE id = "
                + "(SELECT MAX(id) FROM activity_log WHERE household_id = ?)", when, household.getId());
    }

    // The DAOs always stamp "now", so backdating is the one place the seeder writes SQL itself.
    private void backdate(String sql, String when, long id) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, when);
            statement.setLong(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not backdate demo data", e);
        }
    }

    /** A 2026 Brisbane date and time, stored as UTC like the DAOs do. */
    private static String at(int month, int day, int hour, int minute) {
        return LocalDateTime.of(2026, month, day, hour, minute).atZone(BRISBANE).toInstant().toString();
    }
}
