package com.example.its.shared.dto.issue;

public class CommentCreateRequest {

    private Long issueId;
    private Long authorAccountId;
    private String content;

    public CommentCreateRequest() {
    }

    public CommentCreateRequest(Long issueId, Long authorAccountId, String content) {
        this.issueId = issueId;
        this.authorAccountId = authorAccountId;
        this.content = content;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public Long getAuthorAccountId() {
        return authorAccountId;
    }

    public void setAuthorAccountId(Long authorAccountId) {
        this.authorAccountId = authorAccountId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}