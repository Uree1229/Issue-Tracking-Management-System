package com.example.its.shared.dto.account;

import com.example.its.persistence.entity.Account;
import com.example.its.persistence.entity.Role;

import java.time.LocalDateTime;

public class AccountResponse {

    private Long accountId;
    private String loginId;
    private String name;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
    private boolean active;

    public AccountResponse() {
    }

    public AccountResponse(Long accountId, String loginId, String name, String email, Role role,
                           LocalDateTime createdAt, boolean active) {
        this.accountId = accountId;
        this.loginId = loginId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.active = active;
    }

    public static AccountResponse from(Account account) {
        return new AccountResponse(
            account.getAccountId(),
            account.getLoginId(),
            account.getName(),
            account.getEmail(),
            account.getRole(),
            account.getCreatedAt(),
            account.isActive()
        );
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return active;
    }
}
