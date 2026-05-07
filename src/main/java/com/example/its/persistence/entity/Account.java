package com.example.its.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "accounts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_accounts_login_id", columnNames = "login_id"),
        @UniqueConstraint(name = "uq_accounts_email", columnNames = "email")
    }
)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Integer accountId;

    @Column(name = "login_id", nullable = false, length = 100)
    private String loginId;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    @Column(name = "is_active", nullable = false)
    private Integer isActive = 1;

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private List<Project> createdProjects = new ArrayList<>();

    @OneToMany(mappedBy = "reporter", fetch = FetchType.LAZY)
    private List<Issue> reportedIssues = new ArrayList<>();

    @OneToMany(mappedBy = "assignee", fetch = FetchType.LAZY)
    private List<Issue> assignedIssues = new ArrayList<>();

    @OneToMany(mappedBy = "fixer", fetch = FetchType.LAZY)
    private List<Issue> fixedIssues = new ArrayList<>();

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "changedBy", fetch = FetchType.LAZY)
    private List<IssueHistory> issueHistories = new ArrayList<>();

    protected Account() {
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now().toString();
        }
    }

    public Long getAccountId() {
        return accountId != null ? accountId.longValue() : null;
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

    public LocalDateTime getCreatedAt() {
        return parseDateTime(createdAt);
    }

    public boolean isActive() {
        return isActive != null && isActive == 1;
    }

    public void setActive(boolean active) {
        isActive = active ? 1 : 0;
    }

    public List<Project> getCreatedProjects() {
        return createdProjects;
    }

    public List<Issue> getReportedIssues() {
        return reportedIssues;
    }

    public List<Issue> getAssignedIssues() {
        return assignedIssues;
    }

    public List<Issue> getFixedIssues() {
        return fixedIssues;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public List<IssueHistory> getIssueHistories() {
        return issueHistories;
    }

    public static Account create(String loginId, String password, String name, String email, Role role) {
        Account account = new Account();
        account.loginId = loginId;
        account.password = password;
        account.name = name;
        account.email = email;
        account.role = role;
        account.isActive = 1;
        return account;
    }

    private static LocalDateTime parseDateTime(String value) {
        return value != null ? LocalDateTime.parse(value.replace(' ', 'T')) : null;
    }
}
