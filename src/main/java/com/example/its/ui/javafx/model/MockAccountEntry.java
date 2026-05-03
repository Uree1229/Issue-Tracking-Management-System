package com.example.its.ui.javafx.model;

public record MockAccountEntry(
    Long accountId,
    String loginId,
    String password,
    String name,
    String email,
    UiRole role,
    boolean active,
    String disableReason
) {

    public AdminUserRowModel toAdminRowModel() {
        return new AdminUserRowModel(accountId, loginId, name, email, role, active, disableReason);
    }
}
