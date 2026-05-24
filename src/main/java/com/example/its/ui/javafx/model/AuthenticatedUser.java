package com.example.its.ui.javafx.model;

public record AuthenticatedUser(
    Long accountId,
    String loginId,
    String name,
    UiRole role
) {

    public String displayName() {
        return name + " (" + loginId + ")";
    }
}
