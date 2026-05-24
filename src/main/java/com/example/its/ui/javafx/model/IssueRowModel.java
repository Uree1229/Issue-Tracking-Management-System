package com.example.its.ui.javafx.model;

import java.time.LocalDateTime;
import java.util.List;

public class IssueRowModel {

    private final Long issueId;
    private final String title;
    private final String description;
    private final UiIssueStatus status;
    private final UiPriority priority;
    private final String reporterName;
    private final String assigneeName;
    private final String fixerName;
    private final LocalDateTime reportedAt;
    private final String projectName;
    private final List<CommentItemModel> comments;

    public IssueRowModel(
        Long issueId,
        String title,
        String description,
        UiIssueStatus status,
        UiPriority priority,
        String reporterName,
        String assigneeName,
        String fixerName,
        LocalDateTime reportedAt,
        String projectName,
        List<CommentItemModel> comments
    ) {
        this.issueId = issueId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.reporterName = reporterName;
        this.assigneeName = assigneeName;
        this.fixerName = fixerName;
        this.reportedAt = reportedAt;
        this.projectName = projectName;
        this.comments = List.copyOf(comments);
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

    public UiIssueStatus getStatus() {
        return status;
    }

    public UiPriority getPriority() {
        return priority;
    }

    public String getReporterName() {
        return reporterName;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public String getFixerName() {
        return fixerName;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public String getProjectName() {
        return projectName;
    }

    public List<CommentItemModel> getComments() {
        return comments;
    }

    public String getAssigneeDisplayName() {
        return assigneeName == null || assigneeName.isBlank() ? "Unassigned" : assigneeName;
    }

    public String getFixerDisplayName() {
        return fixerName == null || fixerName.isBlank() ? "-" : fixerName;
    }

    public String getStatusDisplayName() {
        return status.displayName();
    }

    public String getPriorityDisplayName() {
        return priority.displayName();
    }
}
