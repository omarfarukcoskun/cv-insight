package com.cvinsight.service;

import com.cvinsight.db.dao.UserDao;
import com.cvinsight.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class UserService {

    private final UserDao userDao;

    public UserService() {
        this.userDao = new UserDao();
    }

    /**
     * Registers a new user.
     *
     * @throws IllegalArgumentException if username or email is already taken
     * @throws RuntimeException         if a database error occurs
     */
    public User register(String username, String email, String plainPassword) {
        validateRegistrationInput(username, email, plainPassword);

        try {
            if (userDao.usernameExists(username)) {
                throw new IllegalArgumentException("Username already taken.");
            }
            if (userDao.emailExists(email)) {
                throw new IllegalArgumentException("An account with this email already exists.");
            }

            String passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
            User user = new User(
                UUID.randomUUID().toString(),
                username,
                email,
                passwordHash,
                LocalDateTime.now()
            );
            userDao.insert(user);
            return user;

        } catch (SQLException e) {
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    /**
     * Attempts to log in with email and password.
     *
     * @return the authenticated User, or empty if credentials are invalid
     * @throws RuntimeException if a database error occurs
     */
    public Optional<User> login(String email, String plainPassword) {
        try {
            Optional<User> userOpt = userDao.findByEmail(email);
            if (userOpt.isEmpty()) {
                return Optional.empty();
            }
            User user = userOpt.get();
            if (!BCrypt.checkpw(plainPassword, user.getPasswordHash())) {
                return Optional.empty();
            }
            return Optional.of(user);

        } catch (SQLException e) {
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    private void validateRegistrationInput(String username, String email, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
    }
}
