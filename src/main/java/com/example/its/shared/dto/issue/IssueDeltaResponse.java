package com.example.its.shared.dto.issue;

import com.example.its.persistence.entity.IssueDelta;
import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Priority;

public class IssueDeltaResponse {

    private Long deltaId;
    private String oldTitle;
    private String newTitle;
    private String oldContent;
    private String newContent;
    private Priority oldPriority;
    private Priority newPriority;
    private IssueStatus oldStatus;
    private IssueStatus newStatus;

    public IssueDeltaResponse() {
    }

    public IssueDeltaResponse(Long deltaId, String oldTitle, String newTitle, String oldContent, String newContent,
                              Priority oldPriority, Priority newPriority, IssueStatus oldStatus,
                              IssueStatus newStatus) {
        this.deltaId = deltaId;
        this.oldTitle = oldTitle;
        this.newTitle = newTitle;
        this.oldContent = oldContent;
        this.newContent = newContent;
        this.oldPriority = oldPriority;
        this.newPriority = newPriority;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public static IssueDeltaResponse from(IssueDelta delta) {
        return new IssueDeltaResponse(
            delta.getDeltaId(),
            delta.getOldTitle(),
            delta.getNewTitle(),
            delta.getOldContent(),
            delta.getNewContent(),
            delta.getOldPriority(),
            delta.getNewPriority(),
            delta.getOldStatus(),
            delta.getNewStatus()
        );
    }

    public Long getDeltaId() {
        return deltaId;
    }

    public String getOldTitle() {
        return oldTitle;
    }

    public String getNewTitle() {
        return newTitle;
    }

    public String getOldContent() {
        return oldContent;
    }

    public String getNewContent() {
        return newContent;
    }

    public Priority getOldPriority() {
        return oldPriority;
    }

    public Priority getNewPriority() {
        return newPriority;
    }

    public IssueStatus getOldStatus() {
        return oldStatus;
    }

    public IssueStatus getNewStatus() {
        return newStatus;
    }
}
