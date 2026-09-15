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
    private boolean guest;

    private SessionContext() {
    }

    public static SessionContext getInstance() {
        return INSTANCE;
    }

    public void login(User user) {
        this.currentUser = user;
        this.guest = false;
    }

    public void enterHousehold(Household household, HouseholdMembership membership) {
        if (!canJoinHousehold()) {
            throw new IllegalStateException("Only registered users can join a household");
        }
        this.currentHousehold = household;
        this.currentMembership = membership;
    }

    public void logout() {
        currentUser = null;
        currentHousehold = null;
        currentMembership = null;
        guest = false;
    }

    //US-03: use the app without an account.
    public void startGuestSession() {
        currentUser = null;
        currentHousehold = null;
        currentMembership = null;
        guest = true;
    }

    public boolean isGuest() { return guest; }

    public boolean canCreateScenario() { return true; }

    public boolean canSaveScenario() { return isLoggedIn(); }

    // US 04 only registered users join a household.
    public boolean canJoinHousehold() { return isLoggedIn(); }

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
    
    // US-10: leave a household
    public void leaveHousehold() {
        currentHousehold = null;
        currentMembership = null;
    }
}
