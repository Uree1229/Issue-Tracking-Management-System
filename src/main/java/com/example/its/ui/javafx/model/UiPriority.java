package com.example.its.ui.javafx.model;

public enum UiPriority {
    BLOCKER("Blocker"),
    CRITICAL("Critical"),
    MAJOR("Major"),
    MINOR("Minor"),
    TRIVIAL("Trivial");

    private final String displayName;

    UiPriority(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static UiPriority fromDisplayName(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (UiPriority priority : values()) {
            if (priority.displayName.equalsIgnoreCase(value)) {
                return priority;
            }
        }
        return null;
    }
}
