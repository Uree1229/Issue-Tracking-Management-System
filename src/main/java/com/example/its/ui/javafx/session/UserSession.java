package com.example.its.ui.javafx.session;

import com.example.its.ui.javafx.model.AuthenticatedUser;

public final class UserSession {

    private static AuthenticatedUser currentUser;

    private UserSession() {
    }

    public static AuthenticatedUser getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(AuthenticatedUser authenticatedUser) {
        currentUser = authenticatedUser;
    }

    public static void clear() {
        currentUser = null;
    }
}
