package com.example.its.shared.dto.issue;

public class IssueAssignRequest {

    private Long issueId;
    private Long plAccountId;
    private Long assigneeAccountId;

    public IssueAssignRequest() {
    }

    public IssueAssignRequest(Long issueId, Long plAccountId, Long assigneeAccountId) {
        this.issueId = issueId;
        this.plAccountId = plAccountId;
        this.assigneeAccountId = assigneeAccountId;
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

    public Long getAssigneeAccountId() {
        return assigneeAccountId;
    }

    public void setAssigneeAccountId(Long assigneeAccountId) {
        this.assigneeAccountId = assigneeAccountId;
    }
}