package com.example.its.shared.dto.issue;

import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Priority;

import java.time.LocalDateTime;
import java.util.List;

public class IssueDetailResponse {

    private Long issueId;
    private String title;
    private String description;
    private IssueStatus status;
    private Priority priority;
    private Long reporterAccountId;
    private String reporterName;
    private Long assigneeAccountId;
    private String assigneeName;
    private Long fixerAccountId;
    private String fixerName;
    private Long projectId;
    private String projectName;
    private LocalDateTime reportedAt;
    private LocalDateTime lastModifiedAt;
    private List<String> tagNames;
    private List<CommentResponse> comments;
    private List<IssueHistoryResponse> histories;

    public IssueDetailResponse() {
    }

    public IssueDetailResponse(Long issueId, String title, String description, IssueStatus status, Priority priority,
                               Long reporterAccountId, String reporterName, Long assigneeAccountId,
                               String assigneeName, Long fixerAccountId, String fixerName, Long projectId,
                               String projectName, LocalDateTime reportedAt, LocalDateTime lastModifiedAt,
                               List<String> tagNames, List<CommentResponse> comments,
                               List<IssueHistoryResponse> histories) {
        this.issueId = issueId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.reporterAccountId = reporterAccountId;
        this.reporterName = reporterName;
        this.assigneeAccountId = assigneeAccountId;
        this.assigneeName = assigneeName;
        this.fixerAccountId = fixerAccountId;
        this.fixerName = fixerName;
        this.projectId = projectId;
        this.projectName = projectName;
        this.reportedAt = reportedAt;
        this.lastModifiedAt = lastModifiedAt;
        this.tagNames = tagNames;
        this.comments = comments;
        this.histories = histories;
    }

    public Long getIssueId() {
        return issueId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public Priority getPriority() {
        return priority;
    }

    public Long getReporterAccountId() {
        return reporterAccountId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public Long getAssigneeAccountId() {
        return assigneeAccountId;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public Long getFixerAccountId() {
        return fixerAccountId;
    }

    public String getFixerName() {
        return fixerName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    public List<String> getTagNames() {
        return tagNames;
    }

    public List<CommentResponse> getComments() {
        return comments;
    }

    public List<IssueHistoryResponse> getHistories() {
        return histories;
    }
}
