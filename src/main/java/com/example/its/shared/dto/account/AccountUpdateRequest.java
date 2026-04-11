package com.example.its.shared.dto.account;

public class AccountUpdateRequest {

    private String password;
    private String name;
    private String email;
    private Boolean active;

    public AccountUpdateRequest() {
    }

    public AccountUpdateRequest(String password, String name, String email, Boolean active) {
        this.password = password;
        this.name = name;
        this.email = email;
        this.active = active;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
