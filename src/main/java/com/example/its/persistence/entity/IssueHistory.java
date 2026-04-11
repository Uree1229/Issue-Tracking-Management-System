package com.example.its.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "issue_histories")
public class IssueHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by_account_id", nullable = false)
    private Account changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @OneToOne(mappedBy = "issueHistory", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private IssueDelta issueDelta;

    protected IssueHistory() {
    }

    @PrePersist
    protected void onCreate() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }

    public Long getHistoryId() {
        return historyId;
    }

    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public Account getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(Account changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public IssueDelta getIssueDelta() {
        return issueDelta;
    }

    public void setIssueDelta(IssueDelta issueDelta) {
        this.issueDelta = issueDelta;
        if (issueDelta != null && issueDelta.getIssueHistory() != this) {
            issueDelta.setIssueHistory(this);
        }
    }

    public IssueDelta getDelta() {
        return issueDelta;
    }
}
