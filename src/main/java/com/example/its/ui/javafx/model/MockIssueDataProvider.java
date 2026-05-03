package com.example.its.ui.javafx.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.List;

public final class MockIssueDataProvider {

    private static final ObservableList<IssueRowModel> ISSUES = FXCollections.observableArrayList(seedIssues());

    private MockIssueDataProvider() {
    }

    public static ObservableList<IssueRowModel> createIssues() {
        return ISSUES;
    }

    public static long nextIssueId() {
        return ISSUES.stream()
            .mapToLong(IssueRowModel::getIssueId)
            .max()
            .orElse(100L) + 1L;
    }

    public static void addIssue(IssueRowModel issue) {
        ISSUES.add(0, issue);
    }

    private static List<IssueRowModel> seedIssues() {
        return List.of(
            new IssueRowModel(
                101L,
                "Login fails after password reset",
                "Users can request a reset mail, but the temporary password is rejected on the next login attempt.",
                UiIssueStatus.NEW,
                UiPriority.CRITICAL,
                "tester1",
                null,
                null,
                LocalDateTime.now().minusDays(1).minusHours(2),
                "project1",
                List.of(
                    new CommentItemModel("tester1", LocalDateTime.now().minusDays(1).minusHours(1), "Reproduced on staging with the seeded admin account."),
                    new CommentItemModel("PL1", LocalDateTime.now().minusHours(22), "Needs triage before the next demo.")
                )
            ),
            new IssueRowModel(
                102L,
                "Issue table filter resets unexpectedly",
                "The selected priority filter returns to 'All' whenever the issue detail panel refreshes.",
                UiIssueStatus.ASSIGNED,
                UiPriority.MAJOR,
                "tester2",
                "dev2",
                null,
                LocalDateTime.now().minusDays(3).minusHours(5),
                "project1",
                List.of(
                    new CommentItemModel("PL1", LocalDateTime.now().minusDays(3).minusHours(3), "Assigned to dev2 for the JavaFX sprint."),
                    new CommentItemModel("dev2", LocalDateTime.now().minusDays(2), "Investigating controller state sync.")
                )
            ),
            new IssueRowModel(
                103L,
                "Comment timestamps overlap in history panel",
                "Multiple comments posted in the same minute are displayed without seconds, which makes debugging harder.",
                UiIssueStatus.FIXED,
                UiPriority.MINOR,
                "tester3",
                "dev4",
                "dev4",
                LocalDateTime.now().minusDays(4).minusHours(8),
                "project1",
                List.of(
                    new CommentItemModel("dev4", LocalDateTime.now().minusDays(1), "Adjusted formatter locally. Waiting for tester verification.")
                )
            ),
            new IssueRowModel(
                104L,
                "Analytics chart does not load closed issues",
                "Closed issues are missing from the monthly statistics query even though the DB rows exist.",
                UiIssueStatus.RESOLVED,
                UiPriority.BLOCKER,
                "tester1",
                "dev1",
                "dev1",
                LocalDateTime.now().minusDays(6).minusHours(4),
                "project1",
                List.of(
                    new CommentItemModel("dev1", LocalDateTime.now().minusDays(3), "Fixed the query aggregation path."),
                    new CommentItemModel("tester1", LocalDateTime.now().minusDays(2), "Verified on seeded data. Ready to close.")
                )
            ),
            new IssueRowModel(
                105L,
                "Recommendation widget ignores tag similarity",
                "The assignee recommendation block is ranking developers only by frequency, not by tag overlap.",
                UiIssueStatus.REOPENED,
                UiPriority.MAJOR,
                "tester4",
                "dev5",
                "dev5",
                LocalDateTime.now().minusDays(8),
                "project1",
                List.of(
                    new CommentItemModel("tester4", LocalDateTime.now().minusDays(5), "The top recommendation changes after reload."),
                    new CommentItemModel("PL1", LocalDateTime.now().minusDays(4), "Reopened for another pass with recommendation logic.")
                )
            ),
            new IssueRowModel(
                106L,
                "Legacy Swing client opens with empty menu state",
                "The menu selection is not synchronized with the default content panel in the Swing demo.",
                UiIssueStatus.CLOSED,
                UiPriority.TRIVIAL,
                "tester2",
                "dev3",
                "dev3",
                LocalDateTime.now().minusDays(10),
                "project1",
                List.of(
                    new CommentItemModel("dev3", LocalDateTime.now().minusDays(9), "Added default tab sync."),
                    new CommentItemModel("PL1", LocalDateTime.now().minusDays(8), "Closed after smoke test.")
                )
            )
        );
    }
}
