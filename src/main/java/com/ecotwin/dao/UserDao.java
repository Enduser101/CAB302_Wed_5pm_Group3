package com.ecotwin.dao;

import com.ecotwin.model.User;

import java.util.Optional;

public interface UserDao {
    User create(String username, String email, String passwordHash, String displayName);
    Optional<User> findByUsername(String username);
    Optional<User> findById(long id);
}
