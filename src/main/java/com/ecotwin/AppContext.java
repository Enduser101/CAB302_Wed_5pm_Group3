package com.ecotwin;

import com.ecotwin.dao.sqlite.SqliteActivityLogDao;
import com.ecotwin.dao.sqlite.SqliteEnergyEntryDao;
import com.ecotwin.dao.sqlite.SqliteHouseholdDao;
import com.ecotwin.dao.sqlite.SqliteHouseholdMembershipDao;
import com.ecotwin.dao.sqlite.SqliteScenarioDao;
import com.ecotwin.dao.sqlite.SqliteTransportEntryDao;
import com.ecotwin.dao.sqlite.SqliteUserDao;
import com.ecotwin.dao.sqlite.SqliteVehicleDao;
import com.ecotwin.dao.sqlite.SqliteWasteEntryDao;
import com.ecotwin.dao.sqlite.SqliteWaterEntryDao;
import com.ecotwin.service.AuthService;
import com.ecotwin.service.EnergyService;
import com.ecotwin.service.HouseholdScoreService;
import com.ecotwin.service.HouseholdService;
import com.ecotwin.service.IndicativeScoreCalculator;
import com.ecotwin.service.PlaceholderScoreService;
import com.ecotwin.service.ScenarioService;
import com.ecotwin.service.ScoreService;
import com.ecotwin.service.TransportService;
import com.ecotwin.service.WasteService;
import com.ecotwin.service.WaterService;
import com.ecotwin.util.SessionContext;
import com.ecotwin.service.ScoreExplanationService;

import java.sql.Connection;

/**
 * Manual dependency-injection container: wires DAOs into services once, at startup.
 *
 * <p>This is the only place that knows which DAO implementations are used. Services only
 * depend on DAO interfaces, so they can be unit tested with mocks, and controllers get their
 * services from here instead of creating them. Swapping SQLite for another database would
 * only change this class.
 *
 * <p>The fields are public and final: they are set once in the constructor and never change.
 */
public class AppContext {

    /** Registration, login and password changes. */
    public final AuthService authService;
    /** Creating, joining, leaving and managing households, plus their activity history. */
    public final HouseholdService householdService;
    /** Recording and editing monthly energy readings. */
    public final EnergyService energyService;
    /** Recording and editing monthly water readings. */
    public final WaterService waterService;
    /** Recording and editing monthly waste readings. */
    public final WasteService wasteService;
    /** Household vehicles and monthly transport readings. */
    public final TransportService transportService;
    /** "What if" scenarios and their saved results. */
    public final ScenarioService scenarioService;
    /** A household's current scores, score history and score changes. */
    public final HouseholdScoreService householdScoreService;
    /** Plain-language explanations of what each score means. */
    public final ScoreExplanationService scoreExplanationService;
    /** The signed-in user (or guest) and their active household. */
    public final SessionContext session = SessionContext.getInstance();

    /**
     * Builds every DAO on the given connection, then every service on those DAOs.
     *
     * @param connection the open database connection; the app passes
     *                   {@link com.ecotwin.util.DatabaseConnection}'s, and tests pass a temporary database
     */
    public AppContext(Connection connection) {
        SqliteUserDao userDao = new SqliteUserDao(connection);
        SqliteHouseholdDao householdDao = new SqliteHouseholdDao(connection);
        SqliteHouseholdMembershipDao membershipDao = new SqliteHouseholdMembershipDao(connection);
        SqliteActivityLogDao activityLogDao = new SqliteActivityLogDao(connection);
        SqliteEnergyEntryDao energyEntryDao = new SqliteEnergyEntryDao(connection);
        SqliteWaterEntryDao waterEntryDao = new SqliteWaterEntryDao(connection);
        SqliteWasteEntryDao wasteEntryDao = new SqliteWasteEntryDao(connection);
        SqliteVehicleDao vehicleDao = new SqliteVehicleDao(connection);
        SqliteTransportEntryDao transportEntryDao = new SqliteTransportEntryDao(connection);
        SqliteScenarioDao scenarioDao = new SqliteScenarioDao(connection);
        ScoreService scoreService = new PlaceholderScoreService();

        this.authService = new AuthService(userDao);
        this.householdService = new HouseholdService(householdDao, membershipDao, activityLogDao, userDao);
        this.energyService = new EnergyService(energyEntryDao, activityLogDao, scoreService);
        this.waterService = new WaterService(waterEntryDao, activityLogDao, scoreService);
        this.wasteService = new WasteService(wasteEntryDao, activityLogDao, scoreService);
        this.transportService = new TransportService(vehicleDao, transportEntryDao, activityLogDao, scoreService);
        IndicativeScoreCalculator scoreCalculator = new IndicativeScoreCalculator();
        this.scenarioService = new ScenarioService(scenarioDao, scoreCalculator);
        this.householdScoreService = new HouseholdScoreService(
                energyService, waterService, wasteService, transportService, scoreCalculator);
        this.scoreExplanationService = new ScoreExplanationService();
    }
}
