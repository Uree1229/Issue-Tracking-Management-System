package com.example.its.shared.dto.issue;

import com.example.its.persistence.entity.Issue;
import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Priority;

import java.time.LocalDateTime;

public class IssueSummaryResponse {

    private Long issueId;
    private String title;
    private IssueStatus status;
    private Priority priority;
    private Long reporterAccountId;
    private String reporterName;
    private Long assigneeAccountId;
    private String assigneeName;
    private Long projectId;
    private LocalDateTime reportedAt;
    private LocalDateTime lastModifiedAt;

    public IssueSummaryResponse() {
    }

    public IssueSummaryResponse(Long issueId, String title, IssueStatus status, Priority priority,
                                Long reporterAccountId, String reporterName, Long assigneeAccountId,
                                String assigneeName, Long projectId, LocalDateTime reportedAt,
                                LocalDateTime lastModifiedAt) {
        this.issueId = issueId;
        this.title = title;
        this.status = status;
        this.priority = priority;
        this.reporterAccountId = reporterAccountId;
        this.reporterName = reporterName;
        this.assigneeAccountId = assigneeAccountId;
        this.assigneeName = assigneeName;
        this.projectId = projectId;
        this.reportedAt = reportedAt;
        this.lastModifiedAt = lastModifiedAt;
    }

    public static IssueSummaryResponse from(Issue issue) {
        return new IssueSummaryResponse(
            issue.getIssueId(),
            issue.getTitle(),
            issue.getStatus(),
            issue.getPriority(),
            issue.getReporter() != null ? issue.getReporter().getAccountId() : null,
            issue.getReporter() != null ? issue.getReporter().getName() : null,
            issue.getAssignee() != null ? issue.getAssignee().getAccountId() : null,
            issue.getAssignee() != null ? issue.getAssignee().getName() : null,
            issue.getProject() != null ? issue.getProject().getProjectId() : null,
            issue.getReportedAt(),
            issue.getLastModifiedAt()
        );
    }

    public Long getIssueId() {
        return issueId;
    }

    public String getTitle() {
        return title;
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

    public Long getProjectId() {
        return projectId;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }
}
