package com.example.its.shared.dto.issue;

import com.example.its.persistence.entity.Comment;

import java.time.LocalDateTime;

public class CommentResponse {

    private Long commentId;
    private Long issueId;
    private Long authorAccountId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;

    public CommentResponse() {
    }

    public CommentResponse(Long commentId, Long issueId, Long authorAccountId, String authorName,
                           String content, LocalDateTime createdAt) {
        this.commentId = commentId;
        this.issueId = issueId;
        this.authorAccountId = authorAccountId;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
            comment.getCommentId(),
            comment.getIssue() != null ? comment.getIssue().getIssueId() : null,
            comment.getAuthor() != null ? comment.getAuthor().getAccountId() : null,
            comment.getAuthor() != null ? comment.getAuthor().getName() : null,
            comment.getContent(),
            comment.getCreatedAt()
        );
    }

    public Long getCommentId() {
        return commentId;
    }

    public Long getIssueId() {
        return issueId;
    }

    public Long getAuthorAccountId() {
        return authorAccountId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
