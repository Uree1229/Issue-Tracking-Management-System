package com.example.its.ui.javafx.session;

import com.example.its.ui.javafx.model.AuthenticatedUser;

public final class UserSession {

    private static AuthenticatedUser currentUser;
    private static Long currentProjectId;
    private static String currentProjectName;

    private UserSession() {
    }

    public static AuthenticatedUser getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(AuthenticatedUser authenticatedUser) {
        currentUser = authenticatedUser;
    }

    public static Long getCurrentProjectId() {
        return currentProjectId;
    }

    public static String getCurrentProjectName() {
        return currentProjectName;
    }

    public static void setCurrentProject(Long projectId, String projectName) {
        currentProjectId = projectId;
        currentProjectName = projectName;
    }

    public static void clear() {
        currentUser = null;
        currentProjectId = null;
        currentProjectName = null;
    }
}
