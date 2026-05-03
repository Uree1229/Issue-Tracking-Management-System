package com.example.its.ui.javafx.controller;

import com.example.its.ui.javafx.model.CommentItemModel;
import com.example.its.ui.javafx.model.IssueRowModel;
import com.example.its.ui.javafx.model.MockIssueDataProvider;
import com.example.its.ui.javafx.service.MockIssueQueryService;
import com.example.its.ui.javafx.support.IntegrationPointHelper;
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
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class InquiryController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ObservableList<IssueRowModel> inquiryResults = FXCollections.observableArrayList();

    private MainLayoutController mainLayoutController;

    @FXML
    private TextField issueIdField;

    @FXML
    private TextField keywordField;

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
            .addListener((observable, oldValue, newValue) -> showDetails(newValue));
        refreshData();
    }

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void refreshData() {
        inquiryResults.setAll(MockIssueDataProvider.createIssues());
        inquirySummaryLabel.setText("Showing preview issue data for inquiry layout review.");
        if (!inquiryResults.isEmpty()) {
            inquiryTable.getSelectionModel().selectFirst();
        } else {
            showDetails(null);
        }
    }

    public void showIssue(Long issueId) {
        issueIdField.setText(issueId == null ? "" : String.valueOf(issueId));
        keywordField.clear();
        refreshData();
        if (issueId == null) {
            return;
        }
        inquiryResults.stream()
            .filter(issue -> issue.getIssueId().equals(issueId))
            .findFirst()
            .ifPresent(issue -> inquiryTable.getSelectionModel().select(issue));
    }

    @FXML
    private void handleInquire() {
        IntegrationPointHelper.showPending(
            "Issue inquiry backend pending",
            "Connect InquiryController.handleInquire() to IssueFacade.getIssueDetail(...) or a dedicated inquiry use case. "
                + "Current table and detail pane are static preview content."
        );
    }

    @FXML
    private void showRecentIssues() {
        issueIdField.clear();
        keywordField.clear();
        refreshData();
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

    private void configureTable() {
        idColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getIssueId()));
        titleColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getTitle()));
        statusColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatusDisplayName()));
        reporterColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getReporterName()));
        inquiryTable.setPlaceholder(new Label("No issues to display."));
    }

    private void showDetails(IssueRowModel issue) {
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
        detailStatusLabel.setText(issue.getStatusDisplayName());
        detailPriorityLabel.setText(issue.getPriorityDisplayName());
        detailReporterLabel.setText(issue.getReporterName());
        detailAssigneeLabel.setText(issue.getAssigneeDisplayName());
        detailFixerLabel.setText(issue.getFixerDisplayName());
        detailProjectLabel.setText(issue.getProjectName());
        detailReportedAtLabel.setText(formatDateTime(issue.getReportedAt()));
        detailDescriptionArea.setText(issue.getDescription());
        commentHistoryListView.setItems(FXCollections.observableArrayList(
            issue.getComments().stream()
                .map(CommentItemModel::toTimelineText)
                .collect(Collectors.toList())
        ));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }
}
