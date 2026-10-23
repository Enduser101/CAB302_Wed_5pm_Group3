package com.ecotwin.service;

import com.ecotwin.dao.UserDao;
import com.ecotwin.model.User;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Account registration, login and password changes (US-01, US-02, US-06).
 *
 * <p>Passwords are never stored: only a BCrypt hash, which includes its own random salt.
 * Login gives the same "Invalid username or password" message whichever part is wrong, so
 * the app does not reveal which usernames exist.
 */
public class AuthService {

    private final UserDao userDao;

    /**
     * @param userDao where user accounts are stored
     */
    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Creates a new account. The username is also used as the starting display name.
     *
     * @param username a unique, non-blank username
     * @param email    a valid email address
     * @param password at least 8 characters; stored only as a BCrypt hash
     * @return the newly created user
     * @throws IllegalArgumentException if the email is invalid, the username is blank or
     *                                  already taken, or the password is too short
     */
    public User register(String username, String email, String password) {
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Please enter a valid email address");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        validatePassword(password);
        if (userDao.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("That username is already taken");
        }
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        return userDao.create(username, email, hash, username);
    }

    /**
     * Checks a username and password against the stored hash.
     *
     * @param username the account's username
     * @param password the password as typed
     * @return the signed-in user
     * @throws IllegalArgumentException with the same message whether the username or the
     *                                  password is wrong
     */
    public User login(String username, String password) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return user;
    }

    /**
     * Changes a signed-in user's password after confirming their current one.
     *
     * @param username        the signed-in user's username
     * @param currentPassword their existing password, as a check that it is really them
     * @param newPassword     the new password, at least 8 characters
     * @throws IllegalArgumentException if the current password is wrong or the new one is
     *                                  too short
     */
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!BCrypt.checkpw(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        validatePassword(newPassword);

        userDao.updatePassword(user.getId(), BCrypt.hashpw(newPassword, BCrypt.gensalt()));
    }

    /**
     * The one place the password rule lives, shared by registration and password changes.
     *
     * @param password the password to check
     * @throws IllegalArgumentException if it is missing or shorter than 8 characters
     */
    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
    }
}
