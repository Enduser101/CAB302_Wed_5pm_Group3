package com.ecotwin.service;

import com.ecotwin.dao.UserDao;
import com.ecotwin.model.User;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User register(String username, String email, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (userDao.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("That username is already taken");
        }
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        return userDao.create(username, email, hash, username);
    }

    public User login(String username, String password) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return user;
    }


    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!BCrypt.checkpw(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        validatePassword(newPassword);

        userDao.updatePassword(user.getId(), BCrypt.hashpw(newPassword, BCrypt.gensalt()));
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
    }
}