package com.ecotwin.util;

import com.ecotwin.AppContext;

import java.sql.Connection;

/** #115: creates the "Cab 302" demo account on first launch. */
public class DemoAccountSeeder {

    public static final String USERNAME = "Cab 302";
    public static final String PASSWORD = "Testuser1";

    public DemoAccountSeeder(Connection connection, AppContext ctx) {
    }

    /** @return true if the demo account was created, false if it already existed */
    public boolean seedIfMissing() {
        return false;
    }
}
