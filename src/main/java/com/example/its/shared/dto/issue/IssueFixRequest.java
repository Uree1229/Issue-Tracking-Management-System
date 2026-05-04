package com.example.its.shared.dto.issue;

public class IssueFixRequest {

    private Long issueId;
    private Long devAccountId;
    private String comment;

    public IssueFixRequest() {
    }

    public IssueFixRequest(Long issueId, Long devAccountId, String comment) {
        this.issueId = issueId;
        this.devAccountId = devAccountId;
        this.comment = comment;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public Long getDevAccountId() {
        return devAccountId;
    }

    public void setDevAccountId(Long devAccountId) {
        this.devAccountId = devAccountId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
