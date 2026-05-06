package com.example.its.ui.javafx.controller;

import com.example.its.shared.dto.account.AccountResponse;
import com.example.its.shared.dto.issue.IssueDetailResponse;
import com.example.its.shared.dto.issue.IssueSearchCondition;
import com.example.its.shared.dto.issue.IssueSummaryResponse;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.ui.javafx.model.IssueRowModel;
import com.example.its.ui.javafx.model.UiIssueStatus;
import com.example.its.ui.javafx.model.UiPriority;
import com.example.its.ui.javafx.service.JavaFxBackendBridge;
import com.example.its.ui.javafx.support.UiAlertHelper;
import com.example.its.ui.javafx.support.UiModelMapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchController {

    private static final String ALL_OPTION = "All";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ObservableList<IssueRowModel> searchResults = FXCollections.observableArrayList();
    private final Map<Long, String> projectNameById = new HashMap<>();
    private final Map<String, Long> reporterIdByOption = new HashMap<>();
    private final Map<String, Long> assigneeIdByOption = new HashMap<>();
    private final Map<String, Long> projectIdByOption = new HashMap<>();
    private final Map<Long, IssueDetailResponse> issueDetailCache = new HashMap<>();

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
    private Label resultSummaryLabel;

    @FXML
    private TableView<IssueRowModel> resultTable;

    @FXML
    private TableColumn<IssueRowModel, Number> idColumn;

    @FXML
    private TableColumn<IssueRowModel, String> titleColumn;

    @FXML
    private TableColumn<IssueRowModel, String> statusColumn;

    @FXML
    private TableColumn<IssueRowModel, String> priorityColumn;

    @FXML
    private TableColumn<IssueRowModel, String> reporterColumn;

    @FXML
    private TableColumn<IssueRowModel, String> assigneeColumn;

    @FXML
    private TableColumn<IssueRowModel, String> reportedAtColumn;

    @FXML
    private Label previewIssueIdLabel;

    @FXML
    private Label previewTitleLabel;

    @FXML
    private Label previewStatusLabel;

    @FXML
    private Label previewPriorityLabel;

    @FXML
    private Label previewReporterLabel;

    @FXML
    private Label previewAssigneeLabel;

    @FXML
    private Label previewProjectLabel;

    @FXML
    private Label previewReportedAtLabel;

    @FXML
    private TextArea previewDescriptionArea;

    @FXML
    private ListView<String> previewCommentListView;

    @FXML
    private void initialize() {
        configureTable();
        populateStaticOptions();
        populateDynamicOptions();
        resultTable.setItems(searchResults);
        resultTable.getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> loadPreview(newValue));
        refreshData();
    }

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void refreshData() {
        populateDynamicOptions();
        performSearch();
    }

    public void applyKeywordSearch(String keyword) {
        keywordField.setText(keyword == null ? "" : keyword.trim());
        descriptionCheckBox.setSelected(true);
        performSearch();
    }

    @FXML
    private void handleSearch() {
        performSearch();
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
        issueDetailCache.clear();
        performSearch();
    }

    @FXML
    private void openSelectedInInquiry() {
        IssueRowModel selectedIssue = resultTable.getSelectionModel().getSelectedItem();
        if (selectedIssue != null && mainLayoutController != null) {
            mainLayoutController.showInquiry(selectedIssue.getIssueId());
        }
    }

    @FXML
    private void openSelectedInBrowser() {
        IssueRowModel selectedIssue = resultTable.getSelectionModel().getSelectedItem();
        if (selectedIssue != null && mainLayoutController != null) {
            mainLayoutController.showIssue(selectedIssue.getIssueId());
        }
    }

    private void performSearch() {
        try {
            issueDetailCache.clear();
            List<IssueRowModel> rows = searchByCriteria();
            searchResults.setAll(rows);
            resultSummaryLabel.setText(rows.size() + " issue(s) matched the current search.");

            if (rows.isEmpty()) {
                showPreview(null);
                return;
            }
            resultTable.getSelectionModel().selectFirst();
        } catch (Exception exception) {
            searchResults.clear();
            showPreview(null);
            resultSummaryLabel.setText("Search failed.");
            UiAlertHelper.showError("Search Failed", "Could not complete the issue search.", exception);
        }
    }

    private List<IssueRowModel> searchByCriteria() {
        Long issueId = parseIssueId();
        String keyword = trimmed(keywordField.getText());

        if (issueId != null) {
            IssueDetailResponse detail = backendBridge().getIssue(issueId);
            if (!matchesClientSideFilters(detail, keyword)) {
                return List.of();
            }
            issueDetailCache.put(detail.getIssueId(), detail);
            return List.of(UiModelMapper.toIssueRowModel(detail));
        }

        IssueSearchCondition condition = new IssueSearchCondition();
        condition.setProjectId(projectIdByOption.get(projectCombo.getValue()));
        condition.setStatus(UiModelMapper.toBackendStatus(UiIssueStatus.fromDisplayName(statusCombo.getValue())));
        condition.setPriority(UiModelMapper.toBackendPriority(UiPriority.fromDisplayName(priorityCombo.getValue())));
        condition.setReporterAccountId(reporterIdByOption.get(reporterCombo.getValue()));
        condition.setAssigneeAccountId(assigneeIdByOption.get(assigneeCombo.getValue()));
        condition.setKeyword(keyword.isBlank() ? null : keyword);

        List<IssueSummaryResponse> summaries = backendBridge().searchIssues(condition);
        List<IssueRowModel> rows = new ArrayList<>();
        for (IssueSummaryResponse summary : summaries) {
            if (!matchesClientSideFilters(summary, keyword)) {
                continue;
            }
            rows.add(UiModelMapper.toIssueRowModel(summary, projectNameById));
        }
        return rows;
    }

    private boolean matchesClientSideFilters(IssueSummaryResponse summary, String keyword) {
        if (activeOnlyCheckBox.isSelected()) {
            UiIssueStatus status = UiModelMapper.toUiIssueStatus(summary.getStatus());
            if (!status.isActiveWorkflowStatus()) {
                return false;
            }
        }

        if (!descriptionCheckBox.isSelected() && !keyword.isBlank()) {
            String title = summary.getTitle() == null ? "" : summary.getTitle();
            return title.toLowerCase().contains(keyword.toLowerCase());
        }
        return true;
    }

    private boolean matchesClientSideFilters(IssueDetailResponse detail, String keyword) {
        if (!matchesOptionStatus(detail.getStatus())) {
            return false;
        }
        if (!matchesOptionPriority(detail.getPriority())) {
            return false;
        }
        if (!matchesOptionProject(detail.getProjectId())) {
            return false;
        }
        if (!matchesOptionReporter(detail.getReporterAccountId())) {
            return false;
        }
        if (!matchesOptionAssignee(detail.getAssigneeAccountId())) {
            return false;
        }
        if (activeOnlyCheckBox.isSelected() && !UiModelMapper.toUiIssueStatus(detail.getStatus()).isActiveWorkflowStatus()) {
            return false;
        }
        if (keyword.isBlank()) {
            return true;
        }

        String title = detail.getTitle() == null ? "" : detail.getTitle().toLowerCase();
        if (!descriptionCheckBox.isSelected()) {
            return title.contains(keyword.toLowerCase());
        }

        String description = detail.getDescription() == null ? "" : detail.getDescription().toLowerCase();
        String loweredKeyword = keyword.toLowerCase();
        return title.contains(loweredKeyword) || description.contains(loweredKeyword);
    }

    private boolean matchesOptionStatus(com.example.its.persistence.entity.IssueStatus status) {
        UiIssueStatus selected = UiIssueStatus.fromDisplayName(statusCombo.getValue());
        return selected == null || status == null || selected.name().equals(status.name());
    }

    private boolean matchesOptionPriority(com.example.its.persistence.entity.Priority priority) {
        UiPriority selected = UiPriority.fromDisplayName(priorityCombo.getValue());
        return selected == null || priority == null || selected.name().equals(priority.name());
    }

    private boolean matchesOptionProject(Long projectId) {
        Long selectedProjectId = projectIdByOption.get(projectCombo.getValue());
        return selectedProjectId == null || selectedProjectId.equals(projectId);
    }

    private boolean matchesOptionReporter(Long reporterAccountId) {
        Long selectedReporterId = reporterIdByOption.get(reporterCombo.getValue());
        return selectedReporterId == null || selectedReporterId.equals(reporterAccountId);
    }

    private boolean matchesOptionAssignee(Long assigneeAccountId) {
        Long selectedAssigneeId = assigneeIdByOption.get(assigneeCombo.getValue());
        return selectedAssigneeId == null || selectedAssigneeId.equals(assigneeAccountId);
    }

    private void configureTable() {
        idColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getIssueId()));
        titleColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getTitle()));
        statusColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatusDisplayName()));
        priorityColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getPriorityDisplayName()));
        reporterColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getReporterName()));
        assigneeColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getAssigneeDisplayName()));
        reportedAtColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(formatDateTime(cellData.getValue().getReportedAt())));
        resultTable.setPlaceholder(new Label("Run a search to populate results."));
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

    private void populateDynamicOptions() {
        populateProjectOptions();
        populateAccountOptions();
    }

    private void populateProjectOptions() {
        List<ProjectResponse> projects = backendBridge().getProjects();
        projectNameById.clear();
        projectIdByOption.clear();

        ObservableList<String> options = FXCollections.observableArrayList();
        options.add(ALL_OPTION);
        for (ProjectResponse project : projects) {
            projectNameById.put(project.getProjectId(), project.getName());
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

    private void loadPreview(IssueRowModel issue) {
        if (issue == null) {
            showPreview(null);
            return;
        }

        try {
            IssueDetailResponse detail = issueDetailCache.computeIfAbsent(issue.getIssueId(), id -> backendBridge().getIssue(id));
            showPreview(detail);
        } catch (Exception exception) {
            UiAlertHelper.showError("Issue Preview Failed", "Could not load the selected issue.", exception);
            showPreview(null);
        }
    }

    private void showPreview(IssueDetailResponse issue) {
        if (issue == null) {
            previewIssueIdLabel.setText("-");
            previewTitleLabel.setText("No issue selected");
            previewStatusLabel.setText("-");
            previewPriorityLabel.setText("-");
            previewReporterLabel.setText("-");
            previewAssigneeLabel.setText("-");
            previewProjectLabel.setText("-");
            previewReportedAtLabel.setText("-");
            previewDescriptionArea.clear();
            previewCommentListView.setItems(FXCollections.observableArrayList("No issue selected."));
            return;
        }

        previewIssueIdLabel.setText("#" + issue.getIssueId());
        previewTitleLabel.setText(issue.getTitle());
        previewStatusLabel.setText(UiModelMapper.toUiIssueStatus(issue.getStatus()).displayName());
        previewPriorityLabel.setText(UiModelMapper.toUiPriority(issue.getPriority()).displayName());
        previewReporterLabel.setText(issue.getReporterName());
        previewAssigneeLabel.setText(issue.getAssigneeName() == null || issue.getAssigneeName().isBlank() ? "Unassigned" : issue.getAssigneeName());
        previewProjectLabel.setText(issue.getProjectName());
        previewReportedAtLabel.setText(formatDateTime(issue.getReportedAt()));
        previewDescriptionArea.setText(issue.getDescription());
        previewCommentListView.setItems(FXCollections.observableArrayList(UiModelMapper.toActivityTimeline(issue)));
    }

    private Long parseIssueId() {
        String value = trimmed(issueIdField.getText());
        if (value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Issue ID must be numeric.");
        }
    }

    private String buildAccountOption(AccountResponse account) {
        return account.getName() + " (" + account.getLoginId() + ")";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "-" : dateTime.format(DATE_TIME_FORMATTER);
    }

    private String trimmed(String value) {
        return value == null ? "" : value.trim();
    }

    private JavaFxBackendBridge backendBridge() {
        return JavaFxBackendBridge.getInstance();
    }
}
