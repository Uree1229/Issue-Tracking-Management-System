package com.example.its.ui.javafx.service;

import com.example.its.ui.javafx.model.IssueRowModel;
import com.example.its.ui.javafx.model.IssueSearchCriteria;
import com.example.its.ui.javafx.model.MockIssueDataProvider;
import com.example.its.ui.javafx.model.UiIssueStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class MockIssueQueryService {

    public List<IssueRowModel> search(IssueSearchCriteria criteria) {
        return MockIssueDataProvider.createIssues().stream()
            .filter(issue -> matchesIssueId(issue, criteria.issueId()))
            .filter(issue -> matchesKeyword(issue, criteria.keyword(), criteria.searchDescription()))
            .filter(issue -> criteria.status() == null || issue.getStatus() == criteria.status())
            .filter(issue -> criteria.priority() == null || issue.getPriority() == criteria.priority())
            .filter(issue -> matchesValue(issue.getReporterName(), criteria.reporterName()))
            .filter(issue -> matchesValue(issue.getAssigneeDisplayName(), criteria.assigneeName()))
            .filter(issue -> matchesValue(issue.getProjectName(), criteria.projectName()))
            .filter(issue -> !criteria.activeOnly() || issue.getStatus() != UiIssueStatus.CLOSED)
            .sorted(Comparator.comparing(IssueRowModel::getReportedAt).reversed())
            .collect(Collectors.toList());
    }

    public Optional<IssueRowModel> findById(Long issueId) {
        if (issueId == null) {
            return Optional.empty();
        }

        return MockIssueDataProvider.createIssues().stream()
            .filter(issue -> issue.getIssueId().equals(issueId))
            .findFirst();
    }

    public List<IssueRowModel> recentIssues(int limit) {
        return MockIssueDataProvider.createIssues().stream()
            .sorted(Comparator.comparing(IssueRowModel::getReportedAt).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    private boolean matchesIssueId(IssueRowModel issue, Long issueId) {
        return issueId == null || issue.getIssueId().equals(issueId);
    }

    private boolean matchesKeyword(IssueRowModel issue, String keyword, boolean searchDescription) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String normalized = normalize(keyword);
        boolean titleMatch = normalize(issue.getTitle()).contains(normalized);
        boolean reporterMatch = normalize(issue.getReporterName()).contains(normalized);
        boolean assigneeMatch = normalize(issue.getAssigneeDisplayName()).contains(normalized);
        boolean projectMatch = normalize(issue.getProjectName()).contains(normalized);
        boolean descriptionMatch = searchDescription && normalize(issue.getDescription()).contains(normalized);

        return titleMatch || reporterMatch || assigneeMatch || projectMatch || descriptionMatch;
    }

    private boolean matchesValue(String issueValue, String selectedValue) {
        return selectedValue == null
            || selectedValue.isBlank()
            || selectedValue.equalsIgnoreCase("All")
            || normalize(issueValue).equals(normalize(selectedValue));
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}
