package com.ecotwin.dao;

import com.ecotwin.model.User;

import java.util.Optional;

/**
 * Stores user accounts (DAO / Repository pattern). Only password hashes are ever passed
 * in; hashing is done by {@link com.ecotwin.service.AuthService} before anything is saved.
 */
public interface UserDao {

    /**
     * @param username     the unique username
     * @param email        the user's email address
     * @param passwordHash a BCrypt hash, never the plain password
     * @param displayName  the name shown in the app and in history messages
     * @return the saved user, with its new id
     */
    User create(String username, String email, String passwordHash, String displayName);

    /**
     * @param username the exact username to find
     * @return the matching user, or empty if no account has that username
     */
    Optional<User> findByUsername(String username);

    /**
     * @param id the user's id
     * @return the matching user, or empty if there is none
     */
    Optional<User> findById(long id);

    /**
     * @param id           the user whose password is changing
     * @param passwordHash the BCrypt hash of the new password
     */
    void updatePassword(long id, String passwordHash);
}
