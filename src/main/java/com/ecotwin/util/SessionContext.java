package com.ecotwin.util;

import com.ecotwin.model.Household;
import com.ecotwin.model.HouseholdMembership;
import com.ecotwin.model.User;

/**
 * Singleton: who's logged in and which household they're currently viewing.
 *
 * <p>Every screen needs the same answer to "who is using the app?", so there is one shared
 * instance. It only holds state in memory; nothing here is saved to the database. Signing
 * out (or starting a guest session) clears it.
 */
public final class SessionContext {

    private static final SessionContext INSTANCE = new SessionContext();

    private User currentUser;
    private Household currentHousehold;
    private HouseholdMembership currentMembership;
    private boolean guest;

    private SessionContext() {
    }

    /**
     * @return the one shared session
     */
    public static SessionContext getInstance() {
        return INSTANCE;
    }

    /**
     * Starts a signed-in session. The household is set separately by
     * {@link #enterHousehold(Household, HouseholdMembership)}.
     *
     * @param user the user who has just logged in
     */
    public void login(User user) {
        this.currentUser = user;
        this.guest = false;
    }

    /**
     * Sets the household the signed-in user is working in.
     *
     * @param household  the household
     * @param membership the user's membership in it, which decides whether they are admin
     * @throws IllegalStateException if no registered user is signed in (US-04)
     */
    public void enterHousehold(Household household, HouseholdMembership membership) {
        if (!canJoinHousehold()) {
            throw new IllegalStateException("Only registered users can join a household");
        }
        this.currentHousehold = household;
        this.currentMembership = membership;
    }

    /**
     * Clears the user, household and guest flag, ready for the login screen.
     */
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

    // US-03 / bug #85 guests may use every page except household
    public boolean canAccessPage(Page page) {
        if (page == Page.HOUSEHOLD) return isLoggedIn();
        return true;
    }

    // US-03  #85 check whether a save press has entries and has an active household.
    public boolean canRecordEntries() {
        return isLoggedIn() && hasActiveHousehold();
    }

    /**
     * @return true if a registered user is signed in (false for guests)
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * @return true if the signed-in user has a household selected
     */
    public boolean hasActiveHousehold() {
        return currentHousehold != null;
    }

    /**
     * @return true if the signed-in user is the current household's administrator
     */
    public boolean isAdmin() {
        return currentMembership != null && currentMembership.getRole() == HouseholdMembership.Role.ADMIN;
    }

    /**
     * @return the signed-in user, or {@code null} for a guest or when no one is signed in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * @return the current household, or {@code null} if none is selected
     */
    public Household getCurrentHousehold() {
        return currentHousehold;
    }

    /**
     * @return the signed-in user's membership in the current household, or {@code null}
     */
    public HouseholdMembership getCurrentMembership() {
        return currentMembership;
    }
    
    // US-10: leave a household
    public void leaveHousehold() {
        currentHousehold = null;
        currentMembership = null;
    }
}
