package com.example.its.ui.javafx.model;

public record IssueSearchCriteria(
    Long issueId,
    String keyword,
    UiIssueStatus status,
    UiPriority priority,
    String reporterName,
    String assigneeName,
    String projectName,
    boolean activeOnly,
    boolean searchDescription
) {
}
