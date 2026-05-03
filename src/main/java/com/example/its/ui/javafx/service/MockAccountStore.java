package com.example.its.ui.javafx.service;

import com.example.its.ui.javafx.model.AdminUserRowModel;
import com.example.its.ui.javafx.model.MockAccountEntry;
import com.example.its.ui.javafx.model.UiRole;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.stream.Collectors;

public final class MockAccountStore {

    private static final ObservableList<MockAccountEntry> ACCOUNTS = FXCollections.observableArrayList(
        new MockAccountEntry(1L, "admin", "admin", "Administrator", "admin@its.local", UiRole.ADMIN, true, ""),
        new MockAccountEntry(2L, "pl1", "pl1", "Project Lead 1", "pl1@its.local", UiRole.PL, true, ""),
        new MockAccountEntry(3L, "dev1", "dev1", "Developer 1", "dev1@its.local", UiRole.DEV, true, ""),
        new MockAccountEntry(4L, "tester1", "tester1", "Tester 1", "tester1@its.local", UiRole.TESTER, true, "")
    );

    private MockAccountStore() {
    }

    public static synchronized List<AdminUserRowModel> getAdminRows() {
        return ACCOUNTS.stream()
            .map(MockAccountEntry::toAdminRowModel)
            .collect(Collectors.toList());
    }

    public static synchronized List<MockAccountEntry> getAccounts() {
        return List.copyOf(ACCOUNTS);
    }
}
