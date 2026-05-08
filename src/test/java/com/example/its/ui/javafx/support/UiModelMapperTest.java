package com.example.its.ui.javafx.support;

import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Priority;
import com.example.its.persistence.entity.Role;
import com.example.its.shared.dto.account.AccountResponse;
import com.example.its.shared.dto.issue.CommentResponse;
import com.example.its.shared.dto.issue.IssueDeltaResponse;
import com.example.its.shared.dto.issue.IssueDetailResponse;
import com.example.its.shared.dto.issue.IssueHistoryResponse;
import com.example.its.shared.dto.issue.IssueSummaryResponse;
import com.example.its.ui.javafx.model.AuthenticatedUser;
import com.example.its.ui.javafx.model.IssueRowModel;
import com.example.its.ui.javafx.model.UiIssueStatus;
import com.example.its.ui.javafx.model.UiPriority;
import com.example.its.ui.javafx.model.UiRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
class UiModelMapperTest {

    @Test
    void toAuthenticatedUserConvertsRoleAndIdentity() {
        AccountResponse response = new AccountResponse(
            11L,
            "pl1",
            "Project Lead",
            "pl1@its.local",
            Role.PL,
            LocalDateTime.of(2026, 5, 8, 9, 0),
            true
        );

        AuthenticatedUser user = UiModelMapper.toAuthenticatedUser(response);

        assertEquals(11L, user.accountId());
        assertEquals("pl1", user.loginId());
        assertEquals("Project Lead (pl1)", user.displayName());
        assertEquals(UiRole.PL, user.role());
    }

    @Test
    void toIssueRowModelUsesProjectMapAndDefaultAssigneeDisplay() {
        IssueSummaryResponse response = new IssueSummaryResponse(
            99L,
            "UI bug",
            IssueStatus.NEW,
            Priority.MINOR,
            10L,
            "Tester One",
            null,
            null,
            300L,
            LocalDateTime.of(2026, 5, 8, 11, 0),
            LocalDateTime.of(2026, 5, 8, 11, 5)
        );

        IssueRowModel row = UiModelMapper.toIssueRowModel(response, Map.of(300L, "Project Alpha"));

        assertEquals("Project Alpha", row.getProjectName());
        assertEquals(UiIssueStatus.NEW, row.getStatus());
        assertEquals(UiPriority.MINOR, row.getPriority());
        assertEquals("Unassigned", row.getAssigneeDisplayName());
    }

    @Test
    void toActivityTimelineSortsCommentsAndHistoryChronologically() {
        IssueDetailResponse response = new IssueDetailResponse(
            77L,
            "Search issue",
            "Description",
            IssueStatus.ASSIGNED,
            Priority.MAJOR,
            10L,
            "Tester One",
            20L,
            "Developer One",
            null,
            null,
            300L,
            "Project Alpha",
            LocalDateTime.of(2026, 5, 8, 9, 0),
            LocalDateTime.of(2026, 5, 8, 9, 20),
            List.of(),
            List.of(
                new CommentResponse(1L, 77L, 10L, "Tester One", "comment later", LocalDateTime.of(2026, 5, 8, 9, 15)),
                new CommentResponse(2L, 77L, 20L, "Developer One", "comment first", LocalDateTime.of(2026, 5, 8, 9, 5))
            ),
            List.of(
                new IssueHistoryResponse(
                    7L,
                    77L,
                    20L,
                    "dev1",
                    LocalDateTime.of(2026, 5, 8, 9, 10),
                    new IssueDeltaResponse(9L, null, "Search issue", null, null, null, null, null, IssueStatus.NEW)
                ),
                new IssueHistoryResponse(
                    8L,
                    77L,
                    20L,
                    "dev1",
                    LocalDateTime.of(2026, 5, 8, 9, 12),
                    new IssueDeltaResponse(10L, null, null, null, null, Priority.MINOR, Priority.MAJOR, IssueStatus.NEW, IssueStatus.ASSIGNED)
                )
            )
        );

        List<String> timeline = UiModelMapper.toActivityTimeline(response);

        assertEquals(4, timeline.size());
        assertEquals("[2026-05-08 09:05] Developer One: comment first", timeline.get(0));
        assertEquals("[2026-05-08 09:10] dev1: created the issue with status NEW.", timeline.get(1));
        assertEquals("[2026-05-08 09:12] dev1: changed status from NEW to ASSIGNED.", timeline.get(2));
        assertEquals("[2026-05-08 09:15] Tester One: comment later", timeline.get(3));
    }
}
