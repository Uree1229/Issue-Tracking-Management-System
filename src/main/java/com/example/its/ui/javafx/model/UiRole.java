package com.example.its.ui.javafx.model;

public enum UiRole {
    ADMIN,
    PL,
    DEV,
    TESTER;

    public boolean canCreateIssue() {
        return this == PL || this == TESTER;
    }

    public boolean canViewAnalytics() {
        return this == PL;
    }

    public boolean canOpenIssueBrowser() {
        return this == PL || this == DEV || this == TESTER;
    }

    public boolean canOpenSearch() {
        return this == PL || this == DEV || this == TESTER;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
