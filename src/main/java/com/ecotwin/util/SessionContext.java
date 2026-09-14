package com.ecotwin.util;

import com.ecotwin.model.Household;
import com.ecotwin.model.HouseholdMembership;
import com.ecotwin.model.User;

/** Singleton: who's logged in and which household they're currently viewing. */
public final class SessionContext {

    private static final SessionContext INSTANCE = new SessionContext();

    private User currentUser;
    private Household currentHousehold;
    private HouseholdMembership currentMembership;

    private SessionContext() {
    }

    public static SessionContext getInstance() {
        return INSTANCE;
    }

    public void login(User user) {
        this.currentUser = user;
    }

    public void enterHousehold(Household household, HouseholdMembership membership) {
        this.currentHousehold = household;
        this.currentMembership = membership;
    }

    public void logout() {
        currentUser = null;
        currentHousehold = null;
        currentMembership = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean hasActiveHousehold() {
        return currentHousehold != null;
    }

    public boolean isAdmin() {
        return currentMembership != null && currentMembership.getRole() == HouseholdMembership.Role.ADMIN;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Household getCurrentHousehold() {
        return currentHousehold;
    }

    public HouseholdMembership getCurrentMembership() {
        return currentMembership;
    }
}
