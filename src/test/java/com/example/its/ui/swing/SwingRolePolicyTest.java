package com.example.its.ui.swing;

import com.example.its.persistence.entity.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwingRolePolicyTest {

    @Test
    void adminCanOnlyManageProjectsAndAccounts() {
        // Then: ADMIN should be confined to the administrative path
        assertTrue(SwingRolePolicy.isAdmin(Role.ADMIN));
        assertTrue(SwingRolePolicy.canManageProjects(Role.ADMIN));
        assertTrue(SwingRolePolicy.canManageAccounts(Role.ADMIN));
        assertFalse(SwingRolePolicy.canBrowseIssues(Role.ADMIN));
        assertFalse(SwingRolePolicy.canCreateIssue(Role.ADMIN));
        assertFalse(SwingRolePolicy.canViewStatistics(Role.ADMIN));
    }

    @Test
    void projectLeadHasFullIssueManagementAndStatistics() {
        // Then: PL should be able to browse, create, view statistics, and freely filter assignees
        assertFalse(SwingRolePolicy.isAdmin(Role.PL));
        assertFalse(SwingRolePolicy.canManageProjects(Role.PL));
        assertFalse(SwingRolePolicy.canManageAccounts(Role.PL));
        assertTrue(SwingRolePolicy.canBrowseIssues(Role.PL));
        assertTrue(SwingRolePolicy.canCreateIssue(Role.PL));
        assertTrue(SwingRolePolicy.canViewStatistics(Role.PL));
        assertTrue(SwingRolePolicy.canFreelyFilterAssignee(Role.PL));
    }

    @Test
    void developerCanOnlyBrowseAndIsLockedToSelfAssigneeFilter() {
        // Then: DEV may browse only — no create, no stats, assignee filter locked to self
        assertTrue(SwingRolePolicy.canBrowseIssues(Role.DEV));
        assertFalse(SwingRolePolicy.canCreateIssue(Role.DEV));
        assertFalse(SwingRolePolicy.canViewStatistics(Role.DEV));
        assertFalse(SwingRolePolicy.canFreelyFilterAssignee(Role.DEV));
    }

    @Test
    void testerCanBrowseAndCreateButCannotViewStatistics() {
        // Then: TESTER may browse and create issues, but statistics belong to PL only
        assertTrue(SwingRolePolicy.canBrowseIssues(Role.TESTER));
        assertTrue(SwingRolePolicy.canCreateIssue(Role.TESTER));
        assertFalse(SwingRolePolicy.canViewStatistics(Role.TESTER));
        assertTrue(SwingRolePolicy.canFreelyFilterAssignee(Role.TESTER));
    }
}
