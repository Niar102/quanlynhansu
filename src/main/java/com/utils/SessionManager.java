package com.utils;

import com.model.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRoleCode());
    }

    public boolean isHR() {
        return currentUser != null && "HR".equals(currentUser.getRoleCode());
    }

    public boolean isDeptManager() {
        return currentUser != null && "DEPT_MANAGER".equals(currentUser.getRoleCode());
    }
}