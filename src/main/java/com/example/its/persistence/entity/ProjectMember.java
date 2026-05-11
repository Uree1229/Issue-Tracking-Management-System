package com.example.its.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "project_members",
    uniqueConstraints = @UniqueConstraint(name = "uq_project_members_project_account", columnNames = {"project_id", "account_id"})
)
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_member_id")
    private Integer projectMemberId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "assigned_at", nullable = false)
    private String assignedAt;

    protected ProjectMember() {
    }

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now().toString();
        }
    }

    public Long getProjectMemberId() {
        return projectMemberId != null ? projectMemberId.longValue() : null;
    }

    public Project getProject() {
        return project;
    }

    public Account getAccount() {
        return account;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt != null ? LocalDateTime.parse(assignedAt.replace(' ', 'T')) : null;
    }

    public static ProjectMember create(Project project, Account account) {
        ProjectMember projectMember = new ProjectMember();
        projectMember.project = project;
        projectMember.account = account;
        return projectMember;
    }
}
