package com.example.its.ui.javafx.model;

public class AdminUserRowModel {

    private final Long accountId;
    private final String loginId;
    private final String realName;
    private final String email;
    private final UiRole role;
    private final boolean active;
    private final String disableReason;

    public AdminUserRowModel(
        Long accountId,
        String loginId,
        String realName,
        String email,
        UiRole role,
        boolean active,
        String disableReason
    ) {
        this.accountId = accountId;
        this.loginId = loginId;
        this.realName = realName;
        this.email = email;
        this.role = role;
        this.active = active;
        this.disableReason = disableReason;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getRealName() {
        return realName;
    }

    public String getEmail() {
        return email;
    }

    public UiRole getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public String getDisableReason() {
        return disableReason;
    }
}
