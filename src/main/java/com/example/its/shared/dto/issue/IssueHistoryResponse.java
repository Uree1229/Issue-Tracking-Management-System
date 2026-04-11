package com.example.its.shared.dto.issue;

import com.example.its.persistence.entity.IssueHistory;

import java.time.LocalDateTime;

public class IssueHistoryResponse {

    private Long historyId;
    private Long issueId;
    private Long changedByAccountId;
    private String changedByLoginId;
    private LocalDateTime changedAt;
    private IssueDeltaResponse delta;

    public IssueHistoryResponse() {
    }

    public IssueHistoryResponse(Long historyId, Long issueId, Long changedByAccountId,
                                String changedByLoginId, LocalDateTime changedAt,
                                IssueDeltaResponse delta) {
        this.historyId = historyId;
        this.issueId = issueId;
        this.changedByAccountId = changedByAccountId;
        this.changedByLoginId = changedByLoginId;
        this.changedAt = changedAt;
        this.delta = delta;
    }

    public static IssueHistoryResponse from(IssueHistory history) {
        return new IssueHistoryResponse(
            history.getHistoryId(),
            history.getIssue() != null ? history.getIssue().getIssueId() : null,
            history.getChangedBy() != null ? history.getChangedBy().getAccountId() : null,
            history.getChangedBy() != null ? history.getChangedBy().getLoginId() : null,
            history.getChangedAt(),
            history.getIssueDelta() != null ? IssueDeltaResponse.from(history.getIssueDelta()) : null
        );
    }

    public Long getHistoryId() {
        return historyId;
    }

    public Long getIssueId() {
        return issueId;
    }

    public Long getChangedByAccountId() {
        return changedByAccountId;
    }

    public String getChangedByLoginId() {
        return changedByLoginId;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public IssueDeltaResponse getDelta() {
        return delta;
    }
}
