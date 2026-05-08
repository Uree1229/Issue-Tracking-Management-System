package com.example.its.application.mapper;

import com.example.its.persistence.entity.Account;
import com.example.its.persistence.entity.Comment;
import com.example.its.persistence.entity.Issue;
import com.example.its.persistence.entity.IssueDelta;
import com.example.its.persistence.entity.IssueHistory;
import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Priority;
import com.example.its.persistence.entity.Project;
import com.example.its.persistence.entity.Role;
import com.example.its.shared.dto.issue.IssueDetailResponse;
import com.example.its.shared.dto.issue.IssueSummaryResponse;
import com.example.its.support.ReflectionTestUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IssueMapperTest {

    private final IssueMapper mapper = new IssueMapper();

    @Test
    void toDetailResponseIncludesCommentsAndHistory() {
        Account reporter = account(10, "tester1", "Tester One", Role.TESTER);
        Account assignee = account(20, "dev1", "Developer One", Role.DEV);
        Account fixer = account(30, "dev2", "Developer Two", Role.DEV);
        Project project = project(100, "Project1", reporter);

        Issue issue = Issue.create("Login issue", "Initial description", Priority.CRITICAL, project, reporter);
        issue.setStatus(IssueStatus.ASSIGNED);
        issue.setAssignee(assignee);
        issue.setFixer(fixer);
        ReflectionTestUtils.setField(issue, "issueId", 55);
        ReflectionTestUtils.setField(issue, "reportedAt", "2026-05-08T09:00:00");
        ReflectionTestUtils.setField(issue, "lastModifiedAt", "2026-05-08T09:30:00");

        Comment comment = Comment.create("Please check the login flow.", reporter, issue);
        ReflectionTestUtils.setField(comment, "commentId", 501);
        ReflectionTestUtils.setField(comment, "createdAt", "2026-05-08T09:05:00");
        issue.addComment(comment);

        IssueDelta delta = IssueDelta.create(
            "Login issue", "Login issue",
            "Initial description", "Updated description",
            Priority.MAJOR, Priority.CRITICAL,
            IssueStatus.NEW, IssueStatus.ASSIGNED
        );
        ReflectionTestUtils.setField(delta, "deltaId", 900);

        IssueHistory history = IssueHistory.create(assignee, delta);
        ReflectionTestUtils.setField(history, "historyId", 701);
        ReflectionTestUtils.setField(history, "changedAt", "2026-05-08T09:10:00");
        issue.addIssueHistory(history);

        IssueDetailResponse response = mapper.toDetailResponse(issue);

        assertNotNull(response);
        assertEquals(55L, response.getIssueId());
        assertEquals("Login issue", response.getTitle());
        assertEquals(IssueStatus.ASSIGNED, response.getStatus());
        assertEquals(Priority.CRITICAL, response.getPriority());
        assertEquals("Tester One", response.getReporterName());
        assertEquals("Developer One", response.getAssigneeName());
        assertEquals("Developer Two", response.getFixerName());
        assertEquals("Project1", response.getProjectName());
        assertEquals(1, response.getComments().size());
        assertEquals("Please check the login flow.", response.getComments().get(0).getContent());
        assertEquals(1, response.getHistories().size());
        assertEquals("dev1", response.getHistories().get(0).getChangedByLoginId());
        assertEquals(IssueStatus.ASSIGNED, response.getHistories().get(0).getDelta().getNewStatus());
    }

    @Test
    void toSummaryResponseMapsListFieldsForBrowserView() {
        Account reporter = account(10, "tester1", "Tester One", Role.TESTER);
        Project project = project(100, "Project1", reporter);
        Issue issue = Issue.create("Search bug", "Description", Priority.MAJOR, project, reporter);

        ReflectionTestUtils.setField(issue, "issueId", 56);
        ReflectionTestUtils.setField(issue, "reportedAt", "2026-05-08T10:00:00");
        ReflectionTestUtils.setField(issue, "lastModifiedAt", "2026-05-08T10:20:00");

        IssueSummaryResponse response = mapper.toSummaryResponse(issue);

        assertNotNull(response);
        assertEquals(56L, response.getIssueId());
        assertEquals("Search bug", response.getTitle());
        assertEquals(Priority.MAJOR, response.getPriority());
        assertEquals("Tester One", response.getReporterName());
        assertEquals(100L, response.getProjectId());
        assertEquals(LocalDateTime.of(2026, 5, 8, 10, 0), response.getReportedAt());
    }

    private static Account account(int id, String loginId, String name, Role role) {
        Account account = Account.create(loginId, "pw", name, loginId + "@its.local", role);
        ReflectionTestUtils.setField(account, "accountId", id);
        ReflectionTestUtils.setField(account, "createdAt", "2026-05-08T08:00:00");
        return account;
    }

    private static Project project(int id, String name, Account createdBy) {
        Project project = Project.create(name, "Project description", createdBy);
        ReflectionTestUtils.setField(project, "projectId", id);
        ReflectionTestUtils.setField(project, "createdAt", "2026-05-08T08:30:00");
        return project;
    }
}
