package com.cvinsight.service;

import com.cvinsight.model.User;

/**
 * PATTERN: Singleton
 *
 * Holds the currently authenticated user for the lifetime of the session.
 * Controllers call SessionManager.getInstance().getCurrentUser() to know
 * who is logged in without passing a User object through every constructor.
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
