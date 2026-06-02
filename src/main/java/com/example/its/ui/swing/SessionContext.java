package com.example.its.ui.swing;

import com.example.its.shared.dto.account.AccountResponse;

public class SessionContext {

    private static AccountResponse currentAccount;
    private static Long currentProjectId;

    public static void setCurrentAccount(AccountResponse account) { currentAccount = account; }
    public static AccountResponse getCurrentAccount() { return currentAccount; }

    public static void setCurrentProjectId(Long projectId) { currentProjectId = projectId; }
    public static Long getCurrentProjectId() { return currentProjectId; }

    public static void clear() {
        currentAccount = null;
        currentProjectId = null;
    }
}
