package com.example.its.shared.dto.issue;

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
