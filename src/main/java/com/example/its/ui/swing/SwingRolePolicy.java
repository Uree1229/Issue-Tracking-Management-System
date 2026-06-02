package com.example.its.ui.swing;

import com.example.its.persistence.entity.Role;

public final class SwingRolePolicy {

    private SwingRolePolicy() {}

    public static boolean isAdmin(Role role) {
        return role == Role.ADMIN;
    }

    public static boolean canManageProjects(Role role) {
        return role == Role.ADMIN;
    }

    public static boolean canManageAccounts(Role role) {
        return role == Role.ADMIN;
    }

    public static boolean canBrowseIssues(Role role) {
        return role == Role.PL || role == Role.DEV || role == Role.TESTER;
    }

    public static boolean canCreateIssue(Role role) {
        return role == Role.PL || role == Role.TESTER;
    }

    public static boolean canViewStatistics(Role role) {
        return role == Role.PL;
    }

    public static boolean canFreelyFilterAssignee(Role role) {
        return role != Role.DEV;
    }
}
