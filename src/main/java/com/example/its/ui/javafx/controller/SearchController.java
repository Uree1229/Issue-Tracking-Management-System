package com.example.its.ui.javafx.controller;

import com.example.its.ui.javafx.model.CommentItemModel;
import com.example.its.ui.javafx.model.IssueRowModel;
import com.example.its.ui.javafx.model.MockIssueDataProvider;
import com.example.its.ui.javafx.model.UiIssueStatus;
import com.example.its.ui.javafx.model.UiPriority;
import com.example.its.ui.javafx.support.IntegrationPointHelper;
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
import java.util.Comparator;
import java.util.stream.Collectors;

public class SearchController {

    private static final String ALL_OPTION = "All";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ObservableList<IssueRowModel> searchResults = FXCollections.observableArrayList();

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
        populateFilterOptions();
        resultTable.setItems(searchResults);
        resultTable.getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> showPreview(newValue));
        loadPreviewResults();
    }

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void refreshData() {
        populateFilterOptions();
        loadPreviewResults();
    }

    public void applyKeywordSearch(String keyword) {
        keywordField.setText(keyword == null ? "" : keyword.trim());
        descriptionCheckBox.setSelected(true);
        loadPreviewResults();
    }

    @FXML
    private void handleSearch() {
        IntegrationPointHelper.showPending(
            "Issue search backend pending",
            "Connect SearchController.handleSearch() to IssueFacade.searchIssues(IssueSearchCondition). "
                + "The table below is currently static preview data only."
        );
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
        loadPreviewResults();
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

    private void loadPreviewResults() {
        searchResults.setAll(MockIssueDataProvider.createIssues());
        resultSummaryLabel.setText(searchResults.size() + " preview issues are shown here.");

        if (searchResults.isEmpty()) {
            showPreview(null);
            return;
        }

        resultTable.getSelectionModel().selectFirst();
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

    private void populateFilterOptions() {
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

        reporterCombo.setItems(FXCollections.observableArrayList());
        reporterCombo.getItems().add(ALL_OPTION);
        reporterCombo.getItems().addAll(
            MockIssueDataProvider.createIssues().stream()
                .map(IssueRowModel::getReporterName)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList())
        );

        assigneeCombo.setItems(FXCollections.observableArrayList());
        assigneeCombo.getItems().add(ALL_OPTION);
        assigneeCombo.getItems().addAll(
            MockIssueDataProvider.createIssues().stream()
                .map(IssueRowModel::getAssigneeDisplayName)
                .distinct()
                .sorted(Comparator.comparing(String::toLowerCase))
                .collect(Collectors.toList())
        );

        projectCombo.setItems(FXCollections.observableArrayList());
        projectCombo.getItems().add(ALL_OPTION);
        projectCombo.getItems().addAll(
            MockIssueDataProvider.createIssues().stream()
                .map(IssueRowModel::getProjectName)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList())
        );

        if (statusCombo.getValue() == null) {
            statusCombo.setValue(ALL_OPTION);
        }
        if (priorityCombo.getValue() == null) {
            priorityCombo.setValue(ALL_OPTION);
        }
        if (reporterCombo.getValue() == null) {
            reporterCombo.setValue(ALL_OPTION);
        }
        if (assigneeCombo.getValue() == null) {
            assigneeCombo.setValue(ALL_OPTION);
        }
        if (projectCombo.getValue() == null) {
            projectCombo.setValue(ALL_OPTION);
        }
        if (!descriptionCheckBox.isSelected()) {
            descriptionCheckBox.setSelected(true);
        }
    }

    private void showPreview(IssueRowModel issue) {
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
        previewStatusLabel.setText(issue.getStatusDisplayName());
        previewPriorityLabel.setText(issue.getPriorityDisplayName());
        previewReporterLabel.setText(issue.getReporterName());
        previewAssigneeLabel.setText(issue.getAssigneeDisplayName());
        previewProjectLabel.setText(issue.getProjectName());
        previewReportedAtLabel.setText(formatDateTime(issue.getReportedAt()));
        previewDescriptionArea.setText(issue.getDescription());
        previewCommentListView.setItems(FXCollections.observableArrayList(
            issue.getComments().stream()
                .map(CommentItemModel::toTimelineText)
                .collect(Collectors.toList())
        ));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }
}
