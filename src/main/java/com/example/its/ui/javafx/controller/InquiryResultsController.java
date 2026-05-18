package com.example.its.ui.javafx.controller;

import com.example.its.shared.dto.issue.CommentCreateRequest;
import com.example.its.shared.dto.issue.IssueDetailResponse;
import com.example.its.shared.dto.issue.IssueSearchCondition;
import com.example.its.shared.dto.issue.IssueSummaryResponse;
import com.example.its.shared.dto.issue.RecommendationResponse;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.ui.javafx.model.InquiryQueryPayload;
import com.example.its.ui.javafx.model.IssueRowModel;
import com.example.its.ui.javafx.model.ProjectTagOption;
import com.example.its.ui.javafx.model.UiRole;
import com.example.its.ui.javafx.service.JavaFxBackendBridge;
import com.example.its.ui.javafx.session.UserSession;
import com.example.its.ui.javafx.support.TagEditorDialog;
import com.example.its.ui.javafx.support.UiAlertHelper;
import com.example.its.ui.javafx.support.UiModelMapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class InquiryResultsController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ObservableList<IssueRowModel> inquiryResults = FXCollections.observableArrayList();
    private final Map<Long, String> projectNameById = new HashMap<>();
    private final Map<Long, IssueDetailResponse> issueDetailCache = new HashMap<>();
    private final Map<Long, List<RecommendationResponse>> recommendationCache = new HashMap<>();

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
    private Label detailTagsLabel;

    @FXML
    private Button editTagsButton;

    @FXML
    private Label detailProjectLabel;

    @FXML
    private Label detailReportedAtLabel;

    @FXML
    private TextArea detailDescriptionArea;

    @FXML
    private ListView<String> commentHistoryListView;

    @FXML
    private VBox recommendationSection;

    @FXML
    private ListView<String> recommendationListView;

    @FXML
    private Label recommendationHintLabel;

    @FXML
    private TextArea commentInputArea;

    @FXML
    private Label commentFeedbackLabel;

    @FXML
    private void initialize() {
        configureTable();
        inquiryTable.setItems(inquiryResults);
        inquiryTable.getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> loadDetails(newValue));
        commentHistoryListView.setPlaceholder(new Label("No activity has been recorded for this issue yet."));
        recommendationListView.setPlaceholder(new Label("Choose an issue to see suggested developers."));
        recommendationHintLabel.setText("Suggestions are ranked using matching tags, recent experience, and current workload.");
        commentFeedbackLabel.getStyleClass().setAll("form-feedback");
        commentFeedbackLabel.setText("");
        configureRoleScopedSections();
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
            recommendationCache.clear();

            if (payload.recentOnly()) {
                loadRecentIssues();
                return;
            }

            Long issueId = payload.issueId();
            if (issueId != null) {
                IssueDetailResponse detail = backendBridge().getIssue(issueId);
                issueDetailCache.put(detail.getIssueId(), detail);
                inquiryResults.setAll(List.of(UiModelMapper.toIssueRowModel(detail)));
                resultHeaderLabel.setText("Issue Details - Exact Match");
                inquirySummaryLabel.setText("Showing 1 issue selected for detailed review.");
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
                resultHeaderLabel.setText(keyword == null ? "Issue Details" : "Issue Details - " + keyword);
                inquirySummaryLabel.setText(inquiryResults.size() + " issue(s) are available for detailed review.");
            }

            if (inquiryResults.isEmpty()) {
                inquirySummaryLabel.setText("No issues matched this search.");
                showDetails(null);
                return;
            }
            inquiryTable.getSelectionModel().selectFirst();
        } catch (Exception exception) {
            inquiryResults.clear();
            showDetails(null);
            resultHeaderLabel.setText("Issue Details");
            inquirySummaryLabel.setText(UiAlertHelper.extractRootCauseMessage(exception, "Issue details could not be opened."));
        }
    }

    @FXML
    private void backToInquiry() {
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

    @FXML
    private void handleAddComment() {
        IssueRowModel selectedIssue = inquiryTable.getSelectionModel().getSelectedItem();
        commentFeedbackLabel.getStyleClass().setAll("form-feedback");

        if (selectedIssue == null) {
            commentFeedbackLabel.getStyleClass().add("form-feedback-error");
            commentFeedbackLabel.setText("Choose an issue before adding a comment.");
            return;
        }

        if (UserSession.getCurrentUser() == null) {
            commentFeedbackLabel.getStyleClass().add("form-feedback-error");
            commentFeedbackLabel.setText("You need to be signed in to add a comment.");
            return;
        }

        String content = commentInputArea.getText() == null ? "" : commentInputArea.getText().trim();
        if (content.isBlank()) {
            commentFeedbackLabel.getStyleClass().add("form-feedback-error");
            commentFeedbackLabel.setText("Comment content cannot be empty.");
            return;
        }

        try {
            CommentCreateRequest request = new CommentCreateRequest(
                selectedIssue.getIssueId(),
                UserSession.getCurrentUser().accountId(),
                content
            );
            IssueDetailResponse updatedIssue = backendBridge().addComment(request);
            issueDetailCache.put(updatedIssue.getIssueId(), updatedIssue);
            refreshRowFromDetail(updatedIssue);
            showDetails(updatedIssue);
            commentInputArea.clear();
            commentFeedbackLabel.getStyleClass().add("form-feedback-success");
            commentFeedbackLabel.setText("Comment added successfully.");
        } catch (Exception exception) {
            commentFeedbackLabel.getStyleClass().add("form-feedback-error");
            commentFeedbackLabel.setText(UiAlertHelper.extractMessage(exception, "Comment could not be added."));
        }
    }

    @FXML
    private void handleEditTags() {
        IssueRowModel selectedIssue = inquiryTable.getSelectionModel().getSelectedItem();
        if (selectedIssue == null) {
            UiAlertHelper.showInfo("No Issue Selected", "Choose an issue first.", "Select an issue from the list before editing tags.");
            return;
        }

        try {
            IssueDetailResponse detail = issueDetailCache.computeIfAbsent(selectedIssue.getIssueId(), id -> backendBridge().getIssue(id));
            List<String> initialTagNames = detail.getTagNames() == null ? List.of() : detail.getTagNames();
            List<String> projectTagNames = backendBridge().getProjectTags(detail.getProjectId()).stream()
                .map(ProjectTagOption::name)
                .toList();

            TagEditorDialog.show(
                "Tags",
                "Edit the tags attached to issue #" + detail.getIssueId() + ".",
                initialTagNames,
                projectTagNames,
                TagEditorDialog.issueLabels()
            ).ifPresent(updatedTagNames -> applyIssueTagChanges(detail, updatedTagNames));
        } catch (Exception exception) {
            UiAlertHelper.showError("Issue Tag Update Failed", "Could not open issue tag editor.", exception);
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
        resultHeaderLabel.setText("Issue Details - Recent Issues");
        inquirySummaryLabel.setText("Showing the " + inquiryResults.size() + " most recent issues for detailed review.");

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
            detailTagsLabel.setText("-");
            detailProjectLabel.setText("-");
            detailReportedAtLabel.setText("-");
            detailDescriptionArea.clear();
            commentHistoryListView.setItems(FXCollections.observableArrayList("No inquiry selected."));
            recommendationListView.setItems(FXCollections.observableArrayList("Choose an issue to see suggested developers."));
            recommendationHintLabel.setText("Suggestions appear when a PL opens an issue with matching tags.");
            commentInputArea.clear();
            commentFeedbackLabel.getStyleClass().setAll("form-feedback");
            commentFeedbackLabel.setText("");
            updateDetailActionAvailability(null);
            return;
        }

        detailIssueIdLabel.setText("#" + issue.getIssueId());
        detailTitleLabel.setText(issue.getTitle());
        detailStatusLabel.setText(UiModelMapper.toUiIssueStatus(issue.getStatus()).displayName());
        detailPriorityLabel.setText(UiModelMapper.toUiPriority(issue.getPriority()).displayName());
        detailReporterLabel.setText(issue.getReporterName());
        detailAssigneeLabel.setText(issue.getAssigneeName() == null || issue.getAssigneeName().isBlank() ? "Unassigned" : issue.getAssigneeName());
        detailFixerLabel.setText(issue.getFixerName() == null || issue.getFixerName().isBlank() ? "-" : issue.getFixerName());
        detailTagsLabel.setText(formatTagNames(issue.getTagNames()));
        detailProjectLabel.setText(issue.getProjectName());
        detailReportedAtLabel.setText(formatDateTime(issue.getReportedAt()));
        detailDescriptionArea.setText(issue.getDescription());
        commentHistoryListView.setItems(FXCollections.observableArrayList(UiModelMapper.toActivityTimeline(issue)));
        recommendationListView.setItems(FXCollections.observableArrayList(buildRecommendationLines(issue)));
        recommendationHintLabel.setText(buildRecommendationHint(issue));
        commentFeedbackLabel.getStyleClass().setAll("form-feedback");
        commentFeedbackLabel.setText("");
        updateDetailActionAvailability(issue);
    }

    private void refreshRowFromDetail(IssueDetailResponse detail) {
        if (detail == null) {
            return;
        }

        for (int index = 0; index < inquiryResults.size(); index++) {
            if (detail.getIssueId().equals(inquiryResults.get(index).getIssueId())) {
                inquiryResults.set(index, UiModelMapper.toIssueRowModel(detail));
                break;
            }
        }
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

    private void configureRoleScopedSections() {
        UiRole role = UserSession.getCurrentUser() == null ? null : UserSession.getCurrentUser().role();
        boolean isPl = role == UiRole.PL;
        boolean canEditTags = role == UiRole.PL || role == UiRole.DEV || role == UiRole.TESTER;
        recommendationSection.setVisible(isPl);
        recommendationSection.setManaged(isPl);
        editTagsButton.setVisible(canEditTags);
        editTagsButton.setManaged(canEditTags);
    }

    private List<String> buildRecommendationLines(IssueDetailResponse issue) {
        if (issue == null) {
            return List.of("Choose an issue to see suggested developers.");
        }
        if (issue.getTagNames() == null || issue.getTagNames().isEmpty()) {
            return List.of("Add one or more tags to this issue to unlock suggestions.");
        }

        List<RecommendationResponse> recommendations = loadRecommendations(issue);
        if (recommendations.isEmpty()) {
            return List.of("No suitable developers were ranked for this issue yet.");
        }

        return recommendations.stream()
            .map(recommendation -> recommendation.getName()
                + " (" + recommendation.getLoginId() + ")"
                + " | score "
                + String.format(Locale.US, "%.1f", recommendation.getScore()))
            .toList();
    }

    private String buildRecommendationHint(IssueDetailResponse issue) {
        if (issue == null) {
            return "Suggestions appear when a PL opens an issue with matching tags.";
        }
        if (issue.getTagNames() == null || issue.getTagNames().isEmpty()) {
            return "This issue does not have any tags yet, so the recommendation engine has nothing to compare.";
        }
        return "Suggestions are ranked using matching tags, recent issue history, and current developer workload.";
    }

    private List<RecommendationResponse> loadRecommendations(IssueDetailResponse issue) {
        if (issue == null || issue.getProjectId() == null) {
            return List.of();
        }

        List<Long> tagIds = backendBridge().getProjectTags(issue.getProjectId()).stream()
            .filter(tag -> issue.getTagNames() != null && issue.getTagNames().contains(tag.name()))
            .map(ProjectTagOption::tagId)
            .toList();

        if (tagIds.isEmpty()) {
            return List.of();
        }

        return recommendationCache.computeIfAbsent(
            issue.getIssueId(),
            ignored -> backendBridge().recommendAssignees(issue.getProjectId(), tagIds)
        );
    }

    private void updateDetailActionAvailability(IssueDetailResponse issue) {
        UiRole role = UserSession.getCurrentUser() == null ? null : UserSession.getCurrentUser().role();
        boolean canEditTags = role == UiRole.PL || role == UiRole.DEV || role == UiRole.TESTER;
        editTagsButton.setDisable(issue == null || !canEditTags);
    }

    private void applyIssueTagChanges(IssueDetailResponse issue, List<String> updatedTagNames) {
        if (issue == null || issue.getProjectId() == null) {
            return;
        }

        Map<String, ProjectTagOption> currentProjectTags = projectTagMap(issue.getProjectId());
        List<String> initialTagNames = issue.getTagNames() == null ? List.of() : issue.getTagNames();

        Map<String, ProjectTagOption> initialProjectTags = currentProjectTags;
        List<String> newProjectTagNames = updatedTagNames.stream()
            .map(this::trimmed)
            .filter(name -> !name.isBlank())
            .filter(name -> !initialProjectTags.containsKey(normalizeTag(name)))
            .distinct()
            .toList();

        if (!newProjectTagNames.isEmpty()) {
            backendBridge().updateProjectTags(issue.getProjectId(), newProjectTagNames, List.of());
            currentProjectTags = projectTagMap(issue.getProjectId());
        }

        Map<String, ProjectTagOption> finalProjectTags = currentProjectTags;
        List<Long> tagIdsToAdd = updatedTagNames.stream()
            .filter(name -> initialTagNames.stream().noneMatch(existing -> normalizeTag(existing).equals(normalizeTag(name))))
            .map(this::normalizeTag)
            .map(finalProjectTags::get)
            .filter(java.util.Objects::nonNull)
            .map(ProjectTagOption::tagId)
            .distinct()
            .toList();

        List<Long> tagIdsToRemove = initialTagNames.stream()
            .filter(name -> updatedTagNames.stream().noneMatch(updated -> normalizeTag(updated).equals(normalizeTag(name))))
            .map(this::normalizeTag)
            .map(finalProjectTags::get)
            .filter(java.util.Objects::nonNull)
            .map(ProjectTagOption::tagId)
            .distinct()
            .toList();

        if (tagIdsToAdd.isEmpty() && tagIdsToRemove.isEmpty()) {
            return;
        }

        IssueDetailResponse updatedIssue = backendBridge().updateIssueTags(issue.getIssueId(), tagIdsToAdd, tagIdsToRemove);
        issueDetailCache.put(updatedIssue.getIssueId(), updatedIssue);
        recommendationCache.remove(updatedIssue.getIssueId());
        refreshRowFromDetail(updatedIssue);
        showDetails(updatedIssue);
    }

    private Map<String, ProjectTagOption> projectTagMap(Long projectId) {
        Map<String, ProjectTagOption> tagByName = new LinkedHashMap<>();
        for (ProjectTagOption tag : backendBridge().getProjectTags(projectId)) {
            tagByName.putIfAbsent(normalizeTag(tag.name()), tag);
        }
        return tagByName;
    }

    private String formatTagNames(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return "-";
        }
        return tagNames.stream()
            .filter(tagName -> tagName != null && !tagName.isBlank())
            .collect(Collectors.joining(", "));
    }

    private String normalizeTag(String tagName) {
        return trimmed(tagName).toLowerCase(Locale.ROOT);
    }

    private String trimmed(String value) {
        return value == null ? "" : value.trim();
    }

    private JavaFxBackendBridge backendBridge() {
        return JavaFxBackendBridge.getInstance();
    }
}
