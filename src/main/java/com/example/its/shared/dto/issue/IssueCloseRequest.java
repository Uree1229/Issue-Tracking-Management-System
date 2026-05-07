package com.example.its.shared.dto.issue;

public class IssueCloseRequest {

    private Long issueId;
    private Long plAccountId;

    public IssueCloseRequest() {
    }

    public IssueCloseRequest(Long issueId, Long plAccountId) {
        this.issueId = issueId;
        this.plAccountId = plAccountId;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public Long getPlAccountId() {
        return plAccountId;
    }

    public void setPlAccountId(Long plAccountId) {
        this.plAccountId = plAccountId;
    }
}
