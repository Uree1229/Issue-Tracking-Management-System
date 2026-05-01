package com.example.its.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "issue_deltas")
public class IssueDelta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delta_id")
    private Long deltaId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "history_id", nullable = false, unique = true)
    private IssueHistory issueHistory;

    @Column(name = "old_title", length = 200)
    private String oldTitle;

    @Column(name = "new_title", length = 200)
    private String newTitle;

    @Column(name = "old_content", columnDefinition = "TEXT")
    private String oldContent;

    @Column(name = "new_content", columnDefinition = "TEXT")
    private String newContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_priority", length = 20)
    private Priority oldPriority;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_priority", length = 20)
    private Priority newPriority;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 20)
    private IssueStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 20)
    private IssueStatus newStatus;

    protected IssueDelta() {
    }

    public Long getDeltaId() {
        return deltaId;
    }

    public IssueHistory getIssueHistory() {
        return issueHistory;
    }

    public void setIssueHistory(IssueHistory issueHistory) {
        this.issueHistory = issueHistory;
        if (issueHistory != null && issueHistory.getIssueDelta() != this) {
            issueHistory.setIssueDelta(this);
        }
    }

    public String[] getTitleDelta() {
        if (oldTitle == null && newTitle == null) {
            return null;
        }
        return new String[]{oldTitle, newTitle};
    }

    public String[] getContentDelta() {
        if (oldContent == null && newContent == null) {
            return null;
        }
        return new String[]{oldContent, newContent};
    }

    public Priority[] getPriorityDelta() {
        if (oldPriority == null && newPriority == null) {
            return null;
        }
        return new Priority[]{oldPriority, newPriority};
    }

    public IssueStatus[] getStatusDelta() {
        if (oldStatus == null && newStatus == null) {
            return null;
        }
        return new IssueStatus[]{oldStatus, newStatus};
    }

    public String getOldTitle() {
        return oldTitle;
    }

    public void setOldTitle(String oldTitle) {
        this.oldTitle = oldTitle;
    }

    public String getNewTitle() {
        return newTitle;
    }

    public void setNewTitle(String newTitle) {
        this.newTitle = newTitle;
    }

    public String getOldContent() {
        return oldContent;
    }

    public void setOldContent(String oldContent) {
        this.oldContent = oldContent;
    }

    public String getNewContent() {
        return newContent;
    }

    public void setNewContent(String newContent) {
        this.newContent = newContent;
    }

    public Priority getOldPriority() {
        return oldPriority;
    }

    public void setOldPriority(Priority oldPriority) {
        this.oldPriority = oldPriority;
    }

    public Priority getNewPriority() {
        return newPriority;
    }

    public void setNewPriority(Priority newPriority) {
        this.newPriority = newPriority;
    }

    public IssueStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(IssueStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public IssueStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(IssueStatus newStatus) {
        this.newStatus = newStatus;
    }

    public static IssueDelta create(IssueStatus oldStatus, IssueStatus newStatus) {
        IssueDelta delta = new IssueDelta();
        // Setter가 없다면 필드 직접 접근(delta.oldStatus = oldStatus;)으로 변경 가능
        delta.setOldStatus(oldStatus);
        delta.setNewStatus(newStatus);
        return delta;
    }
}
