package com.example.its.ui.javafx.model;

public enum UiIssueStatus {
    NEW("New"),
    ASSIGNED("Assigned"),
    FIXED("Fixed"),
    RESOLVED("Resolved"),
    CLOSED("Closed"),
    REOPENED("Reopened");

    private final String displayName;

    UiIssueStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isActiveWorkflowStatus() {
        return this == NEW || this == ASSIGNED || this == REOPENED;
    }

    public boolean isVerificationStatus() {
        return this == FIXED || this == RESOLVED;
    }

    public static UiIssueStatus fromDisplayName(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (UiIssueStatus status : values()) {
            if (status.displayName.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}
