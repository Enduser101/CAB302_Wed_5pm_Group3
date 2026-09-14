package com.ecotwin;

import com.ecotwin.dao.sqlite.SqliteActivityLogDao;
import com.ecotwin.dao.sqlite.SqliteHouseholdDao;
import com.ecotwin.dao.sqlite.SqliteHouseholdMembershipDao;
import com.ecotwin.dao.sqlite.SqliteUserDao;
import com.ecotwin.service.AuthService;
import com.ecotwin.service.HouseholdService;
import com.ecotwin.util.SessionContext;

import java.sql.Connection;

/** Manual dependency-injection container: wires DAOs into services once, at startup. */
public class AppContext {

    public final AuthService authService;
    public final HouseholdService householdService;
    public final SessionContext session = SessionContext.getInstance();

    public AppContext(Connection connection) {
        SqliteUserDao userDao = new SqliteUserDao(connection);
        SqliteHouseholdDao householdDao = new SqliteHouseholdDao(connection);
        SqliteHouseholdMembershipDao membershipDao = new SqliteHouseholdMembershipDao(connection);
        SqliteActivityLogDao activityLogDao = new SqliteActivityLogDao(connection);

        this.authService = new AuthService(userDao);
        this.householdService = new HouseholdService(householdDao, membershipDao, activityLogDao);
    }
}
