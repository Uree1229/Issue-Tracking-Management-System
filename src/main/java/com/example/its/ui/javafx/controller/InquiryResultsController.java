package com.example.its.ui.javafx.controller;

import com.example.its.shared.dto.issue.IssueDetailResponse;
import com.example.its.shared.dto.issue.IssueSearchCondition;
import com.example.its.shared.dto.issue.IssueSummaryResponse;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.ui.javafx.model.InquiryQueryPayload;
import com.example.its.ui.javafx.model.IssueRowModel;
import com.example.its.ui.javafx.service.JavaFxBackendBridge;
import com.example.its.ui.javafx.support.UiAlertHelper;
import com.example.its.ui.javafx.support.UiModelMapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InquiryResultsController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ObservableList<IssueRowModel> inquiryResults = FXCollections.observableArrayList();
    private final Map<Long, String> projectNameById = new HashMap<>();
    private final Map<Long, IssueDetailResponse> issueDetailCache = new HashMap<>();

    private MainLayoutController mainLayoutController;
    private Stage windowStage;

    @FXML
    private Label resultHeaderLabel;

    @FXML
    private Label inquirySummaryLabel;

    @FXML
    private TableView<IssueRowModel> inquiryTable;

    @FXML
    private TableColumn<IssueRowModel, Number> idColumn;

    @FXML
    private TableColumn<IssueRowModel, String> titleColumn;

    @FXML
    private TableColumn<IssueRowModel, String> statusColumn;

    @FXML
    private TableColumn<IssueRowModel, String> reporterColumn;

    @FXML
    private Label detailIssueIdLabel;

    @FXML
    private Label detailTitleLabel;

    @FXML
    private Label detailStatusLabel;

    @FXML
    private Label detailPriorityLabel;

    @FXML
    private Label detailReporterLabel;

    @FXML
    private Label detailAssigneeLabel;

    @FXML
    private Label detailFixerLabel;

    @FXML
    private Label detailProjectLabel;

    @FXML
    private Label detailReportedAtLabel;

    @FXML
    private TextArea detailDescriptionArea;

    @FXML
    private ListView<String> commentHistoryListView;

    @FXML
    private void initialize() {
        configureTable();
        inquiryTable.setItems(inquiryResults);
        inquiryTable.getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> loadDetails(newValue));
        showDetails(null);
    }

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void setWindowStage(Stage windowStage) {
        this.windowStage = windowStage;
    }

    public void applyQuery(InquiryQueryPayload payload) {
        try {
            refreshProjectNames();
            issueDetailCache.clear();

            if (payload.recentOnly()) {
                loadRecentIssues();
                return;
            }

            Long issueId = payload.issueId();
            if (issueId != null) {
                IssueDetailResponse detail = backendBridge().getIssue(issueId);
                issueDetailCache.put(detail.getIssueId(), detail);
                inquiryResults.setAll(List.of(UiModelMapper.toIssueRowModel(detail)));
                resultHeaderLabel.setText("Inquiry Results - Exact Match");
                inquirySummaryLabel.setText("Showing 1 issue by exact ID lookup.");
            } else {
                IssueSearchCondition condition = new IssueSearchCondition();
                String keyword = payload.keyword() == null || payload.keyword().isBlank() ? null : payload.keyword();
                condition.setKeyword(keyword);
                List<IssueSummaryResponse> summaries = backendBridge().searchIssues(condition);
                inquiryResults.setAll(
                    summaries.stream()
                        .map(summary -> UiModelMapper.toIssueRowModel(summary, projectNameById))
                        .toList()
                );
                resultHeaderLabel.setText(keyword == null ? "Inquiry Results" : "Inquiry Results - " + keyword);
                inquirySummaryLabel.setText(inquiryResults.size() + " issue(s) matched the inquiry.");
            }

            if (inquiryResults.isEmpty()) {
                inquirySummaryLabel.setText("No issues matched the inquiry.");
                showDetails(null);
                return;
            }
            inquiryTable.getSelectionModel().selectFirst();
        } catch (Exception exception) {
            inquiryResults.clear();
            showDetails(null);
            resultHeaderLabel.setText("Inquiry Results");
            inquirySummaryLabel.setText(UiAlertHelper.extractRootCauseMessage(exception, "Inquiry could not be completed."));
        }
    }

    @FXML
    private void backToInquiry() {
        if (mainLayoutController != null) {
            mainLayoutController.showInquiry((javafx.event.ActionEvent) null);
        }
        if (windowStage != null) {
            windowStage.close();
        }
    }

    @FXML
    private void openCurrentIssueInBrowser() {
        IssueRowModel selectedIssue = inquiryTable.getSelectionModel().getSelectedItem();
        if (selectedIssue != null && mainLayoutController != null) {
            mainLayoutController.showIssue(selectedIssue.getIssueId());
        }
    }

    @FXML
    private void openCurrentIssueInSearch() {
        IssueRowModel selectedIssue = inquiryTable.getSelectionModel().getSelectedItem();
        if (selectedIssue != null && mainLayoutController != null) {
            mainLayoutController.showSearchWithKeyword(selectedIssue.getTitle());
        }
    }

    private void loadRecentIssues() {
        IssueSearchCondition condition = new IssueSearchCondition();
        List<IssueSummaryResponse> summaries = backendBridge().searchIssues(condition);
        inquiryResults.setAll(
            summaries.stream()
                .limit(10)
                .map(summary -> UiModelMapper.toIssueRowModel(summary, projectNameById))
                .toList()
        );
        resultHeaderLabel.setText("Inquiry Results - Recent Issues");
        inquirySummaryLabel.setText("Showing the " + inquiryResults.size() + " most recent issues.");

        if (inquiryResults.isEmpty()) {
            showDetails(null);
            return;
        }
        inquiryTable.getSelectionModel().selectFirst();
    }

    private void configureTable() {
        idColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getIssueId()));
        titleColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getTitle()));
        statusColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatusDisplayName()));
        reporterColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getReporterName()));
        inquiryTable.setPlaceholder(new Label("No issues to display."));
    }

    private void loadDetails(IssueRowModel issue) {
        if (issue == null) {
            showDetails(null);
            return;
        }

        try {
            IssueDetailResponse detail = issueDetailCache.computeIfAbsent(issue.getIssueId(), id -> backendBridge().getIssue(id));
            showDetails(detail);
        } catch (Exception exception) {
            showDetails(null);
        }
    }

    private void showDetails(IssueDetailResponse issue) {
        if (issue == null) {
            detailIssueIdLabel.setText("-");
            detailTitleLabel.setText("No issue selected");
            detailStatusLabel.setText("-");
            detailPriorityLabel.setText("-");
            detailReporterLabel.setText("-");
            detailAssigneeLabel.setText("-");
            detailFixerLabel.setText("-");
            detailProjectLabel.setText("-");
            detailReportedAtLabel.setText("-");
            detailDescriptionArea.clear();
            commentHistoryListView.setItems(FXCollections.observableArrayList("No inquiry selected."));
            return;
        }

        detailIssueIdLabel.setText("#" + issue.getIssueId());
        detailTitleLabel.setText(issue.getTitle());
        detailStatusLabel.setText(UiModelMapper.toUiIssueStatus(issue.getStatus()).displayName());
        detailPriorityLabel.setText(UiModelMapper.toUiPriority(issue.getPriority()).displayName());
        detailReporterLabel.setText(issue.getReporterName());
        detailAssigneeLabel.setText(issue.getAssigneeName() == null || issue.getAssigneeName().isBlank() ? "Unassigned" : issue.getAssigneeName());
        detailFixerLabel.setText(issue.getFixerName() == null || issue.getFixerName().isBlank() ? "-" : issue.getFixerName());
        detailProjectLabel.setText(issue.getProjectName());
        detailReportedAtLabel.setText(formatDateTime(issue.getReportedAt()));
        detailDescriptionArea.setText(issue.getDescription());
        commentHistoryListView.setItems(FXCollections.observableArrayList(UiModelMapper.toActivityTimeline(issue)));
    }

    private void refreshProjectNames() {
        projectNameById.clear();
        for (ProjectResponse project : backendBridge().getProjects()) {
            projectNameById.put(project.getProjectId(), project.getName());
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "-" : dateTime.format(DATE_TIME_FORMATTER);
    }

    private JavaFxBackendBridge backendBridge() {
        return JavaFxBackendBridge.getInstance();
    }
}
