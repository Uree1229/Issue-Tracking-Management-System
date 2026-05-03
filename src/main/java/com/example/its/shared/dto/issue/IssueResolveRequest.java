package com.example.its.shared.dto.issue;

public class IssueResolveRequest {

    private Long issueId;
    private Long testerAccountId;

    public IssueResolveRequest() {
    }

    public IssueResolveRequest(Long issueId, Long testerAccountId) {
        this.issueId = issueId;
        this.testerAccountId = testerAccountId;
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
}