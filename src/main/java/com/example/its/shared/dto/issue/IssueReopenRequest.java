package com.example.its.shared.dto.issue;

public class IssueReopenRequest {

    private Long issueId;
    private Long reopenAccountId;
    private String reason;

    public IssueReopenRequest() {
    }

    public IssueReopenRequest(Long issueId, Long reopenAccountId, String reason) {
        this.issueId = issueId;
        this.reopenAccountId = reopenAccountId;
        this.reason = reason;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public Long getReopenAccountId() {
        return reopenAccountId;
    }

    public void setReopenAccountId(Long reopenAccountId) {
        this.reopenAccountId = reopenAccountId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
