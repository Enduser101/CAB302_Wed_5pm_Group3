package com.ecotwin.model;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SessionTest {
    // US -03 - try EcoTwin without an account (epic 1 priority should)
//Should be able to create a scenario but not be able to save. Shouldn't be able to join household.
//tests
//guest session is not registered
//guest can creat scenrario
//Guest cannot save scenario
// registered user can save a scernario

    @Test
    void guestIsNotRegistered(){
        Session session = Session.guest();
        boolean registered = session.isRegistered();
        assertFalse(registered);
    }
    @Test
    void guestCreateScenario(){
        Session session =Session.guest();
        boolean createScenario = session.canCreateScenario();
        assertTrue(createScenario);
    }

    @Test
    void guestFailsScenarioSave(){
        Session session = Session.guest();
        boolean guestSavesScenario = session.save();
        assertFalse(guestSavesScenario);
    }

    @Test
    void userSavesScenario(){
        Session session = Session.user();
        boolean userCanSavesScenario = session.save();
        assertTrue(userCanSavesScenario);
    }

}

// us -04 restrict household membership to registered users (epic 1, piroity should )
// Add a check to the household membership to check the user type when adding a user. reject anyone that isn't registered.
// shouldn't be able to join as an unregistered user
// registered user should be able to join
// registered user should be able leave and rejoin