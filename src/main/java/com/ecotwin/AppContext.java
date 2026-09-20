package com.ecotwin;

import com.ecotwin.dao.sqlite.SqliteActivityLogDao;
import com.ecotwin.dao.sqlite.SqliteEnergyEntryDao;
import com.ecotwin.dao.sqlite.SqliteHouseholdDao;
import com.ecotwin.dao.sqlite.SqliteHouseholdMembershipDao;
import com.ecotwin.dao.sqlite.SqliteTransportEntryDao;
import com.ecotwin.dao.sqlite.SqliteUserDao;
import com.ecotwin.dao.sqlite.SqliteVehicleDao;
import com.ecotwin.dao.sqlite.SqliteWasteEntryDao;
import com.ecotwin.dao.sqlite.SqliteWaterEntryDao;
import com.ecotwin.service.AuthService;
import com.ecotwin.service.EnergyService;
import com.ecotwin.service.HouseholdService;
import com.ecotwin.service.PlaceholderScoreService;
import com.ecotwin.service.ScoreService;
import com.ecotwin.service.TransportService;
import com.ecotwin.service.WasteService;
import com.ecotwin.service.WaterService;
import com.ecotwin.util.SessionContext;

import java.sql.Connection;

/** Manual dependency-injection container: wires DAOs into services once, at startup. */
public class AppContext {

    public final AuthService authService;
    public final HouseholdService householdService;
    public final EnergyService energyService;
    public final WaterService waterService;
    public final WasteService wasteService;
    public final TransportService transportService;
    public final SessionContext session = SessionContext.getInstance();

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
        ScoreService scoreService = new PlaceholderScoreService();

        this.authService = new AuthService(userDao);
        this.householdService = new HouseholdService(householdDao, membershipDao, activityLogDao, userDao);
        this.energyService = new EnergyService(energyEntryDao, activityLogDao, scoreService);
        this.waterService = new WaterService(waterEntryDao, activityLogDao, scoreService);
        this.wasteService = new WasteService(wasteEntryDao, activityLogDao, scoreService);
        this.transportService = new TransportService(vehicleDao, transportEntryDao, activityLogDao, scoreService);
    }
}
