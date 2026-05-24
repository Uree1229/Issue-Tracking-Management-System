package com.example.its.ui.javafx.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiRoleTest {

    @Test
    void adminOnlyHasAdministrativeAccess() {
        // Then: admin should stay on the administrative path only
        assertTrue(UiRole.ADMIN.isAdmin());
        assertFalse(UiRole.ADMIN.canCreateIssue());
        assertFalse(UiRole.ADMIN.canOpenIssueBrowser());
        assertFalse(UiRole.ADMIN.canOpenSearch());
        assertFalse(UiRole.ADMIN.canViewAnalytics());
    }

    @Test
    void projectLeadHasFullIssueManagementCapabilities() {
        // Then: PL should be able to create, browse, search, and analyze issues
        assertFalse(UiRole.PL.isAdmin());
        assertTrue(UiRole.PL.canCreateIssue());
        assertTrue(UiRole.PL.canOpenIssueBrowser());
        assertTrue(UiRole.PL.canOpenSearch());
        assertTrue(UiRole.PL.canViewAnalytics());
    }

    @Test
    void developerAndTesterPermissionsMatchUseCaseBoundaries() {
        // Then: DEV can browse/search only, while TESTER can also create issues
        assertFalse(UiRole.DEV.canCreateIssue());
        assertTrue(UiRole.DEV.canOpenIssueBrowser());
        assertTrue(UiRole.DEV.canOpenSearch());
        assertFalse(UiRole.DEV.canViewAnalytics());

        assertTrue(UiRole.TESTER.canCreateIssue());
        assertTrue(UiRole.TESTER.canOpenIssueBrowser());
        assertTrue(UiRole.TESTER.canOpenSearch());
        assertFalse(UiRole.TESTER.canViewAnalytics());
    }
}
