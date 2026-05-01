package com.example.its.shared.dto.issue;

public class IssueFailRequest {

    private Long issueId;
    private Long testerAccountId;
    private String reason;

    public IssueFailRequest() {
    }

    public IssueFailRequest(Long issueId, Long testerAccountId, String reason) {
        this.issueId = issueId;
        this.testerAccountId = testerAccountId;
        this.reason = reason;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public Long getTesterAccountId() {
        return testerAccountId;
    }

    public void setTesterAccountId(Long testerAccountId) {
        this.testerAccountId = testerAccountId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
