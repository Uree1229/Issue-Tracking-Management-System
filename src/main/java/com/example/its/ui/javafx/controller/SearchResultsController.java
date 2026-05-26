package com.example.its.ui.javafx.controller;

import com.example.its.shared.dto.issue.IssueDetailResponse;
import com.example.its.shared.dto.issue.IssueSearchCondition;
import com.example.its.shared.dto.issue.IssueSummaryResponse;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.ui.javafx.model.IssueRowModel;
import com.example.its.ui.javafx.model.SearchQueryPayload;
import com.example.its.ui.javafx.model.UiIssueStatus;
import com.example.its.ui.javafx.service.JavaFxBackendBridge;
import com.example.its.ui.javafx.session.UserSession;
import com.example.its.ui.javafx.support.UiAlertHelper;
import com.example.its.ui.javafx.support.UiModelMapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SearchResultsController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ObservableList<IssueRowModel> searchResults = FXCollections.observableArrayList();
    private final Map<Long, String> projectNameById = new HashMap<>();
    private final Map<Long, IssueDetailResponse> issueDetailCache = new HashMap<>();

    private MainLayoutController mainLayoutController;
    private Stage windowStage;

    @FXML
    private Label resultHeaderLabel;

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
    private void initialize() {
        configureTable();
        resultTable.setItems(searchResults);
        resultTable.setRowFactory(table -> {
            TableRow<IssueRowModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openSelectedInInquiry();
                }
            });
            return row;
        });
    }

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void setWindowStage(Stage windowStage) {
        this.windowStage = windowStage;
    }

    public void applySearch(SearchQueryPayload payload) {
        refreshProjectNames();
        performSearch(payload);
    }

    @FXML
    private void backToSearch() {
        if (mainLayoutController != null) {
            mainLayoutController.showSearch(null);
        }
        if (windowStage != null) {
            windowStage.close();
        }
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

    private void performSearch(SearchQueryPayload payload) {
        try {
            issueDetailCache.clear();
            resultHeaderLabel.setText(buildHeader(payload));

            List<IssueRowModel> rows = searchByCriteria(payload);
            searchResults.setAll(rows);
            resultSummaryLabel.setText(rows.size() + " issue(s) matched the current search.");

            if (rows.isEmpty()) {
                resultSummaryLabel.setText("No issues matched this search.");
                return;
            }
            resultTable.getSelectionModel().selectFirst();
        } catch (Exception exception) {
            searchResults.clear();
            if (isMissingIssueError(exception)) {
                resultSummaryLabel.setText("No issues matched this search.");
                return;
            }

            resultSummaryLabel.setText("Search is temporarily unavailable.");
        }
    }

    private List<IssueRowModel> searchByCriteria(SearchQueryPayload payload) {
        String keyword = payload.keyword() == null ? "" : payload.keyword().trim();
        Long effectiveProjectId = payload.projectId() != null ? payload.projectId() : UserSession.getCurrentProjectId();

        if (payload.issueId() != null) {
            IssueDetailResponse detail = backendBridge().getIssue(payload.issueId());
            if (!matchesClientSideFilters(detail, payload, effectiveProjectId, keyword)) {
                return List.of();
            }
            issueDetailCache.put(detail.getIssueId(), detail);
            return List.of(UiModelMapper.toIssueRowModel(detail));
        }

        IssueSearchCondition condition = new IssueSearchCondition();
        condition.setProjectId(effectiveProjectId);
        condition.setStatus(UiModelMapper.toBackendStatus(payload.status()));
        condition.setPriority(UiModelMapper.toBackendPriority(payload.priority()));
        condition.setReporterAccountId(payload.reporterAccountId());
        condition.setAssigneeAccountId(payload.assigneeAccountId());
        condition.setKeyword(keyword.isBlank() ? null : keyword);

        List<IssueSummaryResponse> summaries = backendBridge().searchIssues(condition);
        List<IssueRowModel> rows = new ArrayList<>();
        for (IssueSummaryResponse summary : summaries) {
            if (!matchesClientSideFilters(summary, payload, effectiveProjectId, keyword)) {
                continue;
            }
            rows.add(UiModelMapper.toIssueRowModel(summary, projectNameById));
        }
        return rows;
    }

    private boolean matchesClientSideFilters(IssueSummaryResponse summary, SearchQueryPayload payload, Long effectiveProjectId, String keyword) {
        if (effectiveProjectId != null && !effectiveProjectId.equals(summary.getProjectId())) {
            return false;
        }
        if (payload.activeOnly()) {
            UiIssueStatus status = UiModelMapper.toUiIssueStatus(summary.getStatus());
            if (!status.isActiveWorkflowStatus()) {
                return false;
            }
        }

        if (!payload.includeDescription() && !keyword.isBlank()) {
            String title = summary.getTitle() == null ? "" : summary.getTitle();
            return title.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
        }
        return true;
    }

    private boolean matchesClientSideFilters(IssueDetailResponse detail, SearchQueryPayload payload, Long effectiveProjectId, String keyword) {
        if (payload.status() != null && (detail.getStatus() == null || !payload.status().name().equals(detail.getStatus().name()))) {
            return false;
        }
        if (payload.priority() != null && (detail.getPriority() == null || !payload.priority().name().equals(detail.getPriority().name()))) {
            return false;
        }
        if (effectiveProjectId != null && !effectiveProjectId.equals(detail.getProjectId())) {
            return false;
        }
        if (payload.reporterAccountId() != null && !payload.reporterAccountId().equals(detail.getReporterAccountId())) {
            return false;
        }
        if (payload.assigneeAccountId() != null && !payload.assigneeAccountId().equals(detail.getAssigneeAccountId())) {
            return false;
        }
        if (payload.activeOnly() && !UiModelMapper.toUiIssueStatus(detail.getStatus()).isActiveWorkflowStatus()) {
            return false;
        }
        if (keyword.isBlank()) {
            return true;
        }

        String loweredKeyword = keyword.toLowerCase(Locale.ROOT);
        String title = detail.getTitle() == null ? "" : detail.getTitle().toLowerCase(Locale.ROOT);
        if (!payload.includeDescription()) {
            return title.contains(loweredKeyword);
        }

        String description = detail.getDescription() == null ? "" : detail.getDescription().toLowerCase(Locale.ROOT);
        return title.contains(loweredKeyword) || description.contains(loweredKeyword);
    }

    private void configureTable() {
        idColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getIssueId()));
        titleColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getTitle()));
        statusColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatusDisplayName()));
        priorityColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getPriorityDisplayName()));
        reporterColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getReporterName()));
        assigneeColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getAssigneeDisplayName()));
        reportedAtColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(formatDateTime(cellData.getValue().getReportedAt())));
        resultTable.setPlaceholder(new Label("No issues matched this search."));
    }

    private void refreshProjectNames() {
        projectNameById.clear();
        for (ProjectResponse project : backendBridge().getProjects()) {
            projectNameById.put(project.getProjectId(), project.getName());
        }
    }

    private boolean isMissingIssueError(Exception exception) {
        String message = UiAlertHelper.extractRootCauseMessage(exception, "").toLowerCase(Locale.ROOT);
        return message.contains("issue not found")
            || message.contains("no issue")
            || message.contains("not found");
    }

    private String buildHeader(SearchQueryPayload payload) {
        List<String> chips = new ArrayList<>();
        if (payload.issueId() != null) {
            chips.add("ID #" + payload.issueId());
        }
        if (payload.keyword() != null && !payload.keyword().isBlank()) {
            chips.add("Keyword: " + payload.keyword());
        }
        if (payload.status() != null) {
            chips.add("Status: " + payload.status().displayName());
        }
        if (payload.priority() != null) {
            chips.add("Priority: " + payload.priority().displayName());
        }
        if (payload.activeOnly()) {
            chips.add("Active only");
        }
        return chips.isEmpty() ? "Matched Issues" : "Matched Issues - " + String.join(" | ", chips);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "-" : dateTime.format(DATE_TIME_FORMATTER);
    }

    private JavaFxBackendBridge backendBridge() {
        return JavaFxBackendBridge.getInstance();
    }
}
