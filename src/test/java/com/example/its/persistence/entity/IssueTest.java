package com.example.its.persistence.entity;

import com.example.its.util.TestDatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IssueTest {


    @Test
    void testCreateInitializesRequiredFieldsAndDefaults() {
        Account reporter = Account.create("tester", "password", "Tester", "tester@its.test", Role.TESTER);
        Project project = Project.create("Project", "Description", reporter);

        Issue issue = Issue.create("Login fails", "Cannot login", null, project, reporter);

        assertEquals("Login fails", issue.getTitle());
        assertEquals("Cannot login", issue.getDescription());
        assertEquals(IssueStatus.NEW, issue.getStatus());
        assertEquals(Priority.MAJOR, issue.getPriority());
        assertSame(project, issue.getProject());
        assertSame(reporter, issue.getReporter());
    }

    @Test
    void testAddAndRemoveTagMaintainsBothSides() {
        Account reporter = Account.create("tester", "password", "Tester", "tester@its.test", Role.TESTER);
        Project project = Project.create("Project", "Description", reporter);
        Issue issue = Issue.create("Button typo", "Typo in submit button", Priority.MINOR, project, reporter);
        Tag tag = Tag.create("ui", "UI issues", project);

        issue.addTag(tag);

        assertTrue(issue.getTags().contains(tag));
        assertTrue(tag.getIssues().contains(issue));

        issue.removeTag(tag);

        assertFalse(issue.getTags().contains(tag));
        assertFalse(tag.getIssues().contains(issue));
    }
}
