package com.example.its.ui.javafx.model;

public record SearchQueryPayload(
    Long issueId,
    String keyword,
    UiIssueStatus status,
    UiPriority priority,
    Long reporterAccountId,
    Long assigneeAccountId,
    Long projectId,
    boolean activeOnly,
    boolean includeDescription
) {

    public static SearchQueryPayload keywordOnly(String keyword) {
        return new SearchQueryPayload(null, keyword, null, null, null, null, null, false, true);
    }
}
