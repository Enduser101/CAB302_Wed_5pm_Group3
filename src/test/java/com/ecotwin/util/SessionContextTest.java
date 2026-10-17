package com.ecotwin.util;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.ecotwin.model.User;

public class SessionContextTest {
    // US -03 - try EcoTwin without an account (epic 1 priority should)
//Should be able to create a scenario but not be able to save. Shouldn't be able to join household.
//tests
//guest session is not registered
//guest can creat scenrario
//Guest cannot save scenario
// registered user can save a scernario
// Guests can sign into account
// Guests can create account
// Guests can't access HH tab
// Guests can access dashboard
// Guests can access scenario
// Guests can access resources
// user who started as guest and signed in is now classified as signed in user.

    @Test
    void guestIsNotRegistered(){
        SessionContext session = SessionContext.getInstance();
        session.startGuestSession();
        assertFalse(session.isLoggedIn());
    }
    @Test
    void guestCreateScenario(){
        SessionContext session = SessionContext.getInstance();
        session.startGuestSession();
        assertTrue(session.canCreateScenario());
    }

    @Test
    void guestFailsScenarioSave(){
        SessionContext session = SessionContext.getInstance();
        session.startGuestSession();
        assertFalse(session.canSaveScenario());
    }

    @Test
    void userSavesScenario(){
        SessionContext session = SessionContext.getInstance();
        session.login(new User(1L, "tester", "tester@example.com", "passwordHash", "Tester","2026-09-15"));
        assertTrue(session.canSaveScenario());
    }

    @Test
    void guestLogout_clearsSession(){
        SessionContext session = SessionContext.getInstance();
        session.startGuestSession();
        session.logout();
        assertFalse(session.isGuest());
        assertFalse(session.isLoggedIn());
    }

    @Test
    void guestSignsIn_isNoLongerGuest(){
        SessionContext session = SessionContext.getInstance();
        session.startGuestSession();
        session.login(new User(1L, "tester", "tester@example.com", "passwordHash", "Tester","2026-09-15"));
        assertFalse(session.isGuest());
        assertTrue(session.isLoggedIn());
    }

    @Test
    void guest_canSeeEveryPageExceptHousehold() {
        SessionContext session = SessionContext.getInstance();
        session.startGuestSession();
        assertTrue(session.canAccessPage(Page.DASHBOARD));
        assertTrue(session.canAccessPage(Page.RESOURCES));
        assertTrue(session.canAccessPage(Page.RECOMMENDATIONS));
        assertTrue(session.canAccessPage(Page.SCENARIOS));
        assertFalse(session.canAccessPage(Page.HOUSEHOLD));
    }

    @Test
    void guest_cannotRecordEntries() {
        SessionContext session = SessionContext.getInstance();
        session.startGuestSession();
        assertFalse(session.canRecordEntries());   // no household to save into
    }

// us -04 restrict household membership to registered users (epic 1, piroity should )
// Add a check to the household membership to check the user type when adding a user. reject anyone that isn't registered.
// shouldn't be able to join as an unregistered user
// registered user should be able to join
// registered user should be able leave and rejoin
    @Test
    void guestCannotJoinHousehold(){
        SessionContext session = SessionContext.getInstance();
        session.startGuestSession();
        assertFalse(session.canJoinHousehold());
    }
    @Test
    void userCanJoinHousehold(){
        SessionContext session = SessionContext.getInstance();
        session.login(new User(1L, "tester", "tester@example.com", "passwordHash", "Tester", "2026-09-15"));
        assertTrue(session.canJoinHousehold());
    }

    @Test
    void guestIsBlockedFromEnteringHousehold(){
        SessionContext session = SessionContext.getInstance();
        session.startGuestSession();
        assertThrows(IllegalStateException.class, () -> session.enterHousehold(null, null));
    }

}

