package com.ecotwin.util;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SessionContextTest {
    // US -03 - try EcoTwin without an account (epic 1 priority should)
//Should be able to create a scenario but not be able to save. Shouldn't be able to join household.
//tests
//guest session is not registered
//guest can creat scenrario
//Guest cannot save scenario
// registered user can save a scernario

    @Test
    void guestIsNotRegistered(){
        Session session = SessionContext.getInstance();
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
        session.login(new User(1L, "tester", "tester@example.com", "hash", "Tester","2026-09-15"));
        assertTrue(session.canSaveScenario());
    }

}

// us -04 restrict household membership to registered users (epic 1, piroity should )
// Add a check to the household membership to check the user type when adding a user. reject anyone that isn't registered.
// shouldn't be able to join as an unregistered user
// registered user should be able to join
// registered user should be able leave and rejoin