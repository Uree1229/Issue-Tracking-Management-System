package com.example.its.shared.dto.account;

import com.example.its.persistence.entity.Role;

public class AccountCreateRequest {

    private String loginId;
    private String password;
    private String name;
    private String email;
    private Role role;

    public AccountCreateRequest() {
    }

    public AccountCreateRequest(String loginId, String password, String name, String email, Role role) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
