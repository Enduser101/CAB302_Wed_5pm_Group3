package com.ecotwin.service;

import com.ecotwin.dao.UserDao;
import com.ecotwin.model.User;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UserDao userDao = mock(UserDao.class);
    private final AuthService service = new AuthService(userDao);

    @Test
    void registerHashesPasswordBeforeStoringIt() {
        when(userDao.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userDao.create(anyString(), any(), anyString(), anyString()))
            .thenAnswer(invocation -> new User(1, "newuser", "n@example.com", invocation.getArgument(2), "newuser", "now"));

        service.register("newuser", "n@example.com", "password123");

        verify(userDao).create(eq("newuser"), any(), argThat(hash -> hash != null && !hash.equals("password123")), anyString());
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(userDao.findByUsername("taken")).thenReturn(
            Optional.of(new User(1, "taken", "t@example.com", "hash", "taken", "now")));

        assertThrows(IllegalArgumentException.class,
            () -> service.register("taken", "t@example.com", "password123"));
    }

    @Test
    void registerRejectsShortPassword() {
        assertThrows(IllegalArgumentException.class,
            () -> service.register("newuser", "n@example.com", "short"));
    }

    @Test
    void loginSucceedsWithCorrectPassword() {
        String hash = BCrypt.hashpw("password123", BCrypt.gensalt());
        when(userDao.findByUsername("someone")).thenReturn(
            Optional.of(new User(1, "someone", "s@example.com", hash, "someone", "now")));

        User result = service.login("someone", "password123");

        assertEquals("someone", result.getUsername());
    }

    @Test
    void loginFailsWithWrongPasswordAndDoesNotRevealWhichFieldWasWrong() {
        String hash = BCrypt.hashpw("password123", BCrypt.gensalt());
        when(userDao.findByUsername("someone")).thenReturn(
            Optional.of(new User(1, "someone", "s@example.com", hash, "someone", "now")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.login("someone", "wrongpassword"));
        assertTrue(ex.getMessage().equalsIgnoreCase("Invalid username or password"));
    }

    @Test
    void loginFailsWithUnknownUsernameUsingTheSameGenericMessage() {
        when(userDao.findByUsername("ghost")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.login("ghost", "password123"));
        assertTrue(ex.getMessage().equalsIgnoreCase("Invalid username or password"));
    }
}
