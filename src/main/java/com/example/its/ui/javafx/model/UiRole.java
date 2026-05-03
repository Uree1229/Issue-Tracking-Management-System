package com.example.its.ui.javafx.model;

public enum UiRole {
    ADMIN,
    PL,
    DEV,
    TESTER;

    public boolean canCreateIssue() {
        return this != ADMIN;
    }

    public boolean canViewAnalytics() {
        return this == ADMIN || this == PL;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
