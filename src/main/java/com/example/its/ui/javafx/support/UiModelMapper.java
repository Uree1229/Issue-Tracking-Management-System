package com.example.its.ui.javafx.support;

import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Priority;
import com.example.its.persistence.entity.Role;
import com.example.its.shared.dto.account.AccountResponse;
import com.example.its.shared.dto.issue.CommentResponse;
import com.example.its.shared.dto.issue.IssueDetailResponse;
import com.example.its.shared.dto.issue.IssueHistoryResponse;
import com.example.its.shared.dto.issue.IssueSummaryResponse;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.ui.javafx.model.AdminProjectRowModel;
import com.example.its.ui.javafx.model.AdminUserRowModel;
import com.example.its.ui.javafx.model.AuthenticatedUser;
import com.example.its.ui.javafx.model.CommentItemModel;
import com.example.its.ui.javafx.model.IssueRowModel;
import com.example.its.ui.javafx.model.UiIssueStatus;
import com.example.its.ui.javafx.model.UiPriority;
import com.example.its.ui.javafx.model.UiRole;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class UiModelMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private UiModelMapper() {
    }

    public static AuthenticatedUser toAuthenticatedUser(AccountResponse response) {
        return new AuthenticatedUser(
            response.getAccountId(),
            response.getLoginId(),
            response.getName(),
            toUiRole(response.getRole())
        );
    }

    public static AdminUserRowModel toAdminUserRowModel(AccountResponse response) {
        return new AdminUserRowModel(
            response.getAccountId(),
            response.getLoginId(),
            response.getName(),
            response.getEmail(),
            toUiRole(response.getRole()),
            response.isActive(),
            ""
        );
    }

    public static AdminProjectRowModel toAdminProjectRowModel(ProjectResponse response) {
        return new AdminProjectRowModel(
            response.getProjectId(),
            response.getName(),
            response.getDescription(),
            "-",
            true,
            "-",
            response.getCreatedByLoginId() == null || response.getCreatedByLoginId().isBlank()
                ? "-"
                : response.getCreatedByLoginId()
        );
    }

    public static IssueRowModel toIssueRowModel(IssueSummaryResponse response, Map<Long, String> projectNames) {
        return new IssueRowModel(
            response.getIssueId(),
            response.getTitle(),
            "",
            toUiIssueStatus(response.getStatus()),
            toUiPriority(response.getPriority()),
            defaultText(response.getReporterName(), "-"),
            response.getAssigneeName(),
            null,
            response.getReportedAt(),
            resolveProjectName(response.getProjectId(), projectNames),
            List.of()
        );
    }

    public static IssueRowModel toIssueRowModel(IssueDetailResponse response) {
        return new IssueRowModel(
            response.getIssueId(),
            response.getTitle(),
            defaultText(response.getDescription(), ""),
            toUiIssueStatus(response.getStatus()),
            toUiPriority(response.getPriority()),
            defaultText(response.getReporterName(), "-"),
            response.getAssigneeName(),
            response.getFixerName(),
            response.getReportedAt(),
            defaultText(response.getProjectName(), "-"),
            toCommentItems(response.getComments())
        );
    }

    public static List<CommentItemModel> toCommentItems(List<CommentResponse> comments) {
        if (comments == null) {
            return List.of();
        }

        return comments.stream()
            .map(comment -> new CommentItemModel(
                defaultText(comment.getAuthorName(), "System"),
                comment.getCreatedAt(),
                defaultText(comment.getContent(), "")
            ))
            .toList();
    }

    public static List<String> toActivityTimeline(IssueDetailResponse response) {
        List<TimedText> items = new ArrayList<>();

        if (response.getComments() != null) {
            for (CommentResponse comment : response.getComments()) {
                items.add(new TimedText(
                    comment.getCreatedAt(),
                    "[" + comment.getCreatedAt().format(DATE_TIME_FORMATTER) + "] "
                        + defaultText(comment.getAuthorName(), "System")
                        + ": "
                        + defaultText(comment.getContent(), "")
                ));
            }
        }

        if (response.getHistories() != null) {
            for (IssueHistoryResponse history : response.getHistories()) {
                LocalDateTime changedAt = history.getChangedAt();
                items.add(new TimedText(
                    changedAt,
                    "[" + changedAt.format(DATE_TIME_FORMATTER) + "] "
                        + defaultText(history.getChangedByLoginId(), "system")
                        + ": "
                        + formatHistory(history)
                ));
            }
        }

        items.sort(Comparator.comparing(TimedText::time));
        return items.stream().map(TimedText::text).toList();
    }

    public static UiRole toUiRole(Role role) {
        return role == null ? UiRole.TESTER : UiRole.valueOf(role.name());
    }

    public static Role toBackendRole(UiRole role) {
        return role == null ? Role.TESTER : Role.valueOf(role.name());
    }

    public static UiPriority toUiPriority(Priority priority) {
        return priority == null ? UiPriority.MAJOR : UiPriority.valueOf(priority.name());
    }

    public static Priority toBackendPriority(UiPriority priority) {
        return priority == null ? Priority.MAJOR : Priority.valueOf(priority.name());
    }

    public static UiIssueStatus toUiIssueStatus(IssueStatus status) {
        return status == null ? UiIssueStatus.NEW : UiIssueStatus.valueOf(status.name());
    }

    public static IssueStatus toBackendStatus(UiIssueStatus status) {
        return status == null ? null : IssueStatus.valueOf(status.name());
    }

    private static String resolveProjectName(Long projectId, Map<Long, String> projectNames) {
        if (projectId == null || projectNames == null) {
            return "-";
        }
        return defaultText(projectNames.get(projectId), "-");
    }

    private static String formatHistory(IssueHistoryResponse history) {
        if (history.getDelta() == null) {
            return "updated the issue.";
        }

        IssueStatus oldStatus = history.getDelta().getOldStatus();
        IssueStatus newStatus = history.getDelta().getNewStatus();

        if (oldStatus == null && newStatus != null) {
            return "created the issue with status " + newStatus.name() + ".";
        }
        if (oldStatus != newStatus && newStatus != null) {
            return "changed status from " + oldStatus.name() + " to " + newStatus.name() + ".";
        }
        if (history.getDelta().getOldPriority() != history.getDelta().getNewPriority()
            && history.getDelta().getNewPriority() != null) {
            return "changed priority to " + history.getDelta().getNewPriority().name() + ".";
        }
        if (history.getDelta().getOldTitle() == null && history.getDelta().getNewTitle() != null) {
            return "created the issue record.";
        }
        return "updated the issue.";
    }

    private static String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private record TimedText(LocalDateTime time, String text) {
    }
}
