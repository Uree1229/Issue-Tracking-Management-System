package com.example.its.ui.javafx.controller;

import com.example.its.shared.dto.account.AccountResponse;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.ui.javafx.model.SearchQueryPayload;
import com.example.its.ui.javafx.model.UiIssueStatus;
import com.example.its.ui.javafx.model.UiPriority;
import com.example.its.ui.javafx.service.JavaFxBackendBridge;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchController {

    private static final String ALL_OPTION = "All";

    private final Map<String, Long> reporterIdByOption = new HashMap<>();
    private final Map<String, Long> assigneeIdByOption = new HashMap<>();
    private final Map<String, Long> projectIdByOption = new HashMap<>();

    private MainLayoutController mainLayoutController;

    @FXML
    private TextField keywordField;

    @FXML
    private ComboBox<String> statusCombo;

    @FXML
    private ComboBox<String> reporterCombo;

    @FXML
    private ComboBox<String> priorityCombo;

    @FXML
    private TextField issueIdField;

    @FXML
    private ComboBox<String> assigneeCombo;

    @FXML
    private ComboBox<String> projectCombo;

    @FXML
    private CheckBox activeOnlyCheckBox;

    @FXML
    private CheckBox descriptionCheckBox;

    @FXML
    private void initialize() {
        populateStaticOptions();
        initializeFallbackOptions();
        safeRefreshData();
    }

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void refreshData() {
        safeRefreshData();
    }

    public void applyKeywordSearch(String keyword) {
        keywordField.setText(keyword == null ? "" : keyword.trim());
        descriptionCheckBox.setSelected(true);
        submitSearch();
    }

    @FXML
    private void handleSearch() {
        submitSearch();
    }

    @FXML
    private void handleReset() {
        keywordField.clear();
        issueIdField.clear();
        statusCombo.setValue(ALL_OPTION);
        reporterCombo.setValue(ALL_OPTION);
        priorityCombo.setValue(ALL_OPTION);
        assigneeCombo.setValue(ALL_OPTION);
        projectCombo.setValue(ALL_OPTION);
        activeOnlyCheckBox.setSelected(false);
        descriptionCheckBox.setSelected(true);
    }

    private void submitSearch() {
        if (mainLayoutController == null) {
            return;
        }

        SearchQueryPayload payload = buildPayload();
        mainLayoutController.showSearchResults(payload);
    }

    private SearchQueryPayload buildPayload() {
        return new SearchQueryPayload(
            parseIssueId(),
            trimmed(keywordField.getText()),
            UiIssueStatus.fromDisplayName(statusCombo.getValue()),
            UiPriority.fromDisplayName(priorityCombo.getValue()),
            reporterIdByOption.get(reporterCombo.getValue()),
            assigneeIdByOption.get(assigneeCombo.getValue()),
            projectIdByOption.get(projectCombo.getValue()),
            activeOnlyCheckBox.isSelected(),
            descriptionCheckBox.isSelected()
        );
    }

    private void populateStaticOptions() {
        statusCombo.setItems(FXCollections.observableArrayList(
            ALL_OPTION,
            UiIssueStatus.NEW.displayName(),
            UiIssueStatus.ASSIGNED.displayName(),
            UiIssueStatus.FIXED.displayName(),
            UiIssueStatus.RESOLVED.displayName(),
            UiIssueStatus.CLOSED.displayName(),
            UiIssueStatus.REOPENED.displayName()
        ));
        priorityCombo.setItems(FXCollections.observableArrayList(
            ALL_OPTION,
            UiPriority.BLOCKER.displayName(),
            UiPriority.CRITICAL.displayName(),
            UiPriority.MAJOR.displayName(),
            UiPriority.MINOR.displayName(),
            UiPriority.TRIVIAL.displayName()
        ));
        statusCombo.setValue(ALL_OPTION);
        priorityCombo.setValue(ALL_OPTION);
        descriptionCheckBox.setSelected(true);
    }

    private void initializeFallbackOptions() {
        ObservableList<String> allOnly = FXCollections.observableArrayList(ALL_OPTION);
        projectCombo.setItems(allOnly);
        projectCombo.setValue(ALL_OPTION);
        reporterCombo.setItems(FXCollections.observableArrayList(ALL_OPTION));
        reporterCombo.setValue(ALL_OPTION);
        assigneeCombo.setItems(FXCollections.observableArrayList(ALL_OPTION));
        assigneeCombo.setValue(ALL_OPTION);
    }

    private void safeRefreshData() {
        try {
            populateProjectOptions();
            populateAccountOptions();
        } catch (Exception exception) {
            // Keep the form usable even if supporting filter data is temporarily unavailable.
        }
    }

    private void populateProjectOptions() {
        List<ProjectResponse> projects = backendBridge().getProjects();
        projectIdByOption.clear();

        ObservableList<String> options = FXCollections.observableArrayList();
        options.add(ALL_OPTION);
        for (ProjectResponse project : projects) {
            options.add(project.getName());
            projectIdByOption.put(project.getName(), project.getProjectId());
        }
        projectCombo.setItems(options);
        if (projectCombo.getValue() == null || !options.contains(projectCombo.getValue())) {
            projectCombo.setValue(ALL_OPTION);
        }
    }

    private void populateAccountOptions() {
        List<AccountResponse> accounts = backendBridge().getActiveAccounts();
        reporterIdByOption.clear();
        assigneeIdByOption.clear();

        ObservableList<String> reporterOptions = FXCollections.observableArrayList();
        ObservableList<String> assigneeOptions = FXCollections.observableArrayList();
        reporterOptions.add(ALL_OPTION);
        assigneeOptions.add(ALL_OPTION);

        for (AccountResponse account : accounts) {
            String option = buildAccountOption(account);
            reporterOptions.add(option);
            reporterIdByOption.put(option, account.getAccountId());
            if (account.getRole() == com.example.its.persistence.entity.Role.DEV) {
                assigneeOptions.add(option);
                assigneeIdByOption.put(option, account.getAccountId());
            }
        }

        reporterCombo.setItems(reporterOptions);
        assigneeCombo.setItems(assigneeOptions);
        if (reporterCombo.getValue() == null || !reporterOptions.contains(reporterCombo.getValue())) {
            reporterCombo.setValue(ALL_OPTION);
        }
        if (assigneeCombo.getValue() == null || !assigneeOptions.contains(assigneeCombo.getValue())) {
            assigneeCombo.setValue(ALL_OPTION);
        }
    }

    private Long parseIssueId() {
        String value = trimmed(issueIdField.getText());
        if (value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String buildAccountOption(AccountResponse account) {
        return account.getName() + " (" + account.getLoginId() + ")";
    }

    private String trimmed(String value) {
        return value == null ? "" : value.trim();
    }

    private JavaFxBackendBridge backendBridge() {
        return JavaFxBackendBridge.getInstance();
    }
}
