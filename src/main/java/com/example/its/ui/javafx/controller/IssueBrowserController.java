package com.example.its.ui.javafx.controller;

import com.example.its.ui.javafx.model.CommentItemModel;
import com.example.its.ui.javafx.model.IssueRowModel;
import com.example.its.ui.javafx.model.MockIssueDataProvider;
import com.example.its.ui.javafx.model.UiIssueStatus;
import com.example.its.ui.javafx.model.UiPriority;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

public class IssueBrowserController {

    private static final String ALL_OPTION = "All";
    private static final String UNASSIGNED_OPTION = "Unassigned";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ObservableList<IssueRowModel> issues = MockIssueDataProvider.createIssues();
    private final FilteredList<IssueRowModel> filteredIssues = new FilteredList<>(issues);

    @FXML
    private TextField keywordField;

    @FXML
    private ComboBox<String> statusFilterCombo;

    @FXML
    private ComboBox<String> priorityFilterCombo;

    @FXML
    private ComboBox<String> reporterFilterCombo;

    @FXML
    private ComboBox<String> assigneeFilterCombo;

    @FXML
    private TableView<IssueRowModel> issueTable;

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
    private Label issueIdLabel;

    @FXML
    private Label issueTitleLabel;

    @FXML
    private Label statusBadgeLabel;

    @FXML
    private Label priorityBadgeLabel;

    @FXML
    private Label reporterValueLabel;

    @FXML
    private Label assigneeValueLabel;

    @FXML
    private Label fixerValueLabel;

    @FXML
    private Label projectValueLabel;

    @FXML
    private Label reportedAtValueLabel;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ListView<String> commentListView;

    @FXML
    private void initialize() {
        configureTable();
        configureFilters();
        refreshFilterOptions();
        bindTableData();
        showIssueDetails(null);

        issueTable.getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> showIssueDetails(newValue));

        if (!issues.isEmpty()) {
            issueTable.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void resetFilters() {
        resetFiltersInternal();
    }

    @FXML
    private void handlePlaceholderAction(ActionEvent event) {
        IssueRowModel selectedIssue = issueTable.getSelectionModel().getSelectedItem();
        Button source = (Button) event.getSource();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Action Placeholder");
        alert.setHeaderText(source.getText() + " is not connected yet.");
        alert.setContentText(
            selectedIssue == null
                ? "Select an issue first. Then wire this action to the issue facade or workflow service."
                : "Connect '" + source.getText() + "' for issue #" + selectedIssue.getIssueId()
                + " to the backend workflow once the facade contracts are ready."
        );
        alert.showAndWait();
    }

    private void configureTable() {
        idColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getIssueId()));
        titleColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getTitle()));
        statusColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatusDisplayName()));
        priorityColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getPriorityDisplayName()));
        reporterColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getReporterName()));
        assigneeColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getAssigneeDisplayName()));
        reportedAtColumn.setCellValueFactory(
            cellData -> new ReadOnlyStringWrapper(formatDateTime(cellData.getValue().getReportedAt()))
        );

        issueTable.setPlaceholder(new Label("No issues match the current filters."));
    }

    private void configureFilters() {
        // Intentionally left without live filtering.
        // Backend-connected filtering will be added by the integration owner later.
    }

    private void bindTableData() {
        SortedList<IssueRowModel> sortedIssues = new SortedList<>(filteredIssues);
        sortedIssues.comparatorProperty().bind(issueTable.comparatorProperty());
        issueTable.setItems(sortedIssues);
        applyFilters();
    }

    public void applyKeywordSearch(String keyword) {
        resetFiltersInternal();
        keywordField.setText(keyword == null ? "" : keyword.trim());
        applyFilters();
    }

    public void showNewIssues() {
        resetFiltersInternal();
        statusFilterCombo.setValue(UiIssueStatus.NEW.displayName());
        applyFilters();
    }

    public void showAssignedTo(String assigneeName) {
        resetFiltersInternal();
        ensureOptionPresent(assigneeFilterCombo, assigneeName);
        assigneeFilterCombo.setValue(assigneeName);
        applyFilters();
    }

    public void showReportedBy(String reporterName) {
        resetFiltersInternal();
        ensureOptionPresent(reporterFilterCombo, reporterName);
        reporterFilterCombo.setValue(reporterName);
        applyFilters();
    }

    public void showFixedIssues() {
        resetFiltersInternal();
        statusFilterCombo.setValue(UiIssueStatus.FIXED.displayName());
        applyFilters();
    }

    public void refreshData() {
        String selectedStatus = statusFilterCombo.getValue();
        String selectedPriority = priorityFilterCombo.getValue();
        String selectedReporter = reporterFilterCombo.getValue();
        String selectedAssignee = assigneeFilterCombo.getValue();

        refreshFilterOptions();

        restoreSelection(statusFilterCombo, selectedStatus);
        restoreSelection(priorityFilterCombo, selectedPriority);
        restoreSelection(reporterFilterCombo, selectedReporter);
        restoreSelection(assigneeFilterCombo, selectedAssignee);
        applyFilters();
    }

    public void showIssueById(Long issueId) {
        if (issueId == null) {
            return;
        }

        refreshData();
        resetFiltersInternal();
        issues.stream()
            .filter(issue -> issue.getIssueId().equals(issueId))
            .findFirst()
            .ifPresent(issue -> {
                issueTable.getSelectionModel().select(issue);
                issueTable.scrollTo(issue);
            });
    }

    private void applyFilters() {
        filteredIssues.setPredicate(issue -> true);
    }

    private void resetFiltersInternal() {
        keywordField.clear();
        statusFilterCombo.setValue(ALL_OPTION);
        priorityFilterCombo.setValue(ALL_OPTION);
        reporterFilterCombo.setValue(ALL_OPTION);
        assigneeFilterCombo.setValue(ALL_OPTION);
        applyFilters();
    }

    private void ensureOptionPresent(ComboBox<String> comboBox, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        if (!comboBox.getItems().contains(value)) {
            comboBox.getItems().add(value);
        }
    }

    private void refreshFilterOptions() {
        statusFilterCombo.setItems(FXCollections.observableArrayList(
            ALL_OPTION,
            UiIssueStatus.NEW.displayName(),
            UiIssueStatus.ASSIGNED.displayName(),
            UiIssueStatus.FIXED.displayName(),
            UiIssueStatus.RESOLVED.displayName(),
            UiIssueStatus.CLOSED.displayName(),
            UiIssueStatus.REOPENED.displayName()
        ));

        priorityFilterCombo.setItems(FXCollections.observableArrayList(
            ALL_OPTION,
            UiPriority.BLOCKER.displayName(),
            UiPriority.CRITICAL.displayName(),
            UiPriority.MAJOR.displayName(),
            UiPriority.MINOR.displayName(),
            UiPriority.TRIVIAL.displayName()
        ));

        reporterFilterCombo.setItems(FXCollections.observableArrayList());
        reporterFilterCombo.getItems().add(ALL_OPTION);
        reporterFilterCombo.getItems().addAll(
            issues.stream()
                .map(IssueRowModel::getReporterName)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList())
        );

        assigneeFilterCombo.setItems(FXCollections.observableArrayList());
        assigneeFilterCombo.getItems().add(ALL_OPTION);
        assigneeFilterCombo.getItems().addAll(
            issues.stream()
                .map(IssueRowModel::getAssigneeDisplayName)
                .distinct()
                .sorted(Comparator.comparing(String::toLowerCase))
                .collect(Collectors.toList())
        );

        if (statusFilterCombo.getValue() == null) {
            statusFilterCombo.setValue(ALL_OPTION);
        }
        if (priorityFilterCombo.getValue() == null) {
            priorityFilterCombo.setValue(ALL_OPTION);
        }
        if (reporterFilterCombo.getValue() == null) {
            reporterFilterCombo.setValue(ALL_OPTION);
        }
        if (assigneeFilterCombo.getValue() == null) {
            assigneeFilterCombo.setValue(ALL_OPTION);
        }
    }

    private void restoreSelection(ComboBox<String> comboBox, String previousSelection) {
        if (previousSelection == null || !comboBox.getItems().contains(previousSelection)) {
            comboBox.setValue(ALL_OPTION);
            return;
        }
        comboBox.setValue(previousSelection);
    }

    private void showIssueDetails(IssueRowModel issue) {
        if (issue == null) {
            issueIdLabel.setText("-");
            issueTitleLabel.setText("Select an issue");
            statusBadgeLabel.setText("-");
            priorityBadgeLabel.setText("-");
            reporterValueLabel.setText("-");
            assigneeValueLabel.setText("-");
            fixerValueLabel.setText("-");
            projectValueLabel.setText("-");
            reportedAtValueLabel.setText("-");
            descriptionArea.clear();
            commentListView.setItems(FXCollections.observableArrayList("No issue selected."));
            statusBadgeLabel.getStyleClass().setAll("label", "badge", "status-badge");
            priorityBadgeLabel.getStyleClass().setAll("label", "badge", "priority-badge");
            return;
        }

        issueIdLabel.setText("#" + issue.getIssueId());
        issueTitleLabel.setText(issue.getTitle());
        statusBadgeLabel.setText(issue.getStatusDisplayName());
        priorityBadgeLabel.setText(issue.getPriorityDisplayName());
        reporterValueLabel.setText(issue.getReporterName());
        assigneeValueLabel.setText(issue.getAssigneeDisplayName());
        fixerValueLabel.setText(issue.getFixerDisplayName());
        projectValueLabel.setText(issue.getProjectName());
        reportedAtValueLabel.setText(formatDateTime(issue.getReportedAt()));
        descriptionArea.setText(issue.getDescription());
        commentListView.setItems(FXCollections.observableArrayList(
            issue.getComments().stream()
                .map(CommentItemModel::toTimelineText)
                .collect(Collectors.toList())
        ));

        statusBadgeLabel.getStyleClass().setAll("label", "badge", "status-badge", "status-" + issue.getStatus().name().toLowerCase(Locale.ROOT));
        priorityBadgeLabel.getStyleClass().setAll("label", "badge", "priority-badge", "priority-" + issue.getPriority().name().toLowerCase(Locale.ROOT));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }

}
