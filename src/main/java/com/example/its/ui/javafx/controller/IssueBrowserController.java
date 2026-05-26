package com.example.its.ui.javafx.controller;

import com.example.its.shared.dto.account.AccountResponse;
import com.example.its.shared.dto.issue.CommentCreateRequest;
import com.example.its.shared.dto.issue.IssueAssignRequest;
import com.example.its.shared.dto.issue.IssueCloseRequest;
import com.example.its.shared.dto.issue.IssueDetailResponse;
import com.example.its.shared.dto.issue.IssueFailRequest;
import com.example.its.shared.dto.issue.IssueFixRequest;
import com.example.its.shared.dto.issue.IssueReopenRequest;
import com.example.its.shared.dto.issue.IssueResolveRequest;
import com.example.its.shared.dto.issue.IssueSearchCondition;
import com.example.its.shared.dto.issue.IssueSummaryResponse;
import com.example.its.shared.dto.issue.RecommendationResponse;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.shared.dto.tag.TagResponse;
import com.example.its.ui.javafx.model.AuthenticatedUser;
import com.example.its.ui.javafx.model.IssueRowModel;
import com.example.its.ui.javafx.model.ProjectTagOption;
import com.example.its.ui.javafx.model.UiIssueStatus;
import com.example.its.ui.javafx.model.UiPriority;
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
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextInputDialog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class IssueBrowserController {

    private static final String ALL_OPTION = "All";
    private static final String UNASSIGNED_OPTION = "Unassigned";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ObservableList<IssueRowModel> issues = FXCollections.observableArrayList();
    private final FilteredList<IssueRowModel> filteredIssues = new FilteredList<>(issues);
    private final Map<Long, String> projectNameById = new HashMap<>();
    private final Map<Long, IssueDetailResponse> issueDetailCache = new HashMap<>();
    private final Map<Long, List<RecommendationResponse>> recommendationCache = new HashMap<>();
    private MainLayoutController mainLayoutController;

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
    private Button createIssueButton;

    @FXML
    private TableView<IssueRowModel> issueTable;

    @FXML
    private TableColumn<IssueRowModel, Number> idColumn;

    @FXML
    private TableColumn<IssueRowModel, String> titleColumn;

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
    private Label tagsValueLabel;

    @FXML
    private Button editTagsButton;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ListView<String> commentListView;

    @FXML
    private ListView<String> recommendationListView;

    @FXML
    private VBox recommendationSection;

    @FXML
    private VBox commentSection;

    @FXML
    private TextArea commentInputArea;

    @FXML
    private Label commentFeedbackLabel;

    @FXML
    private Button saveCommentButton;

    @FXML
    private HBox workflowButtonBar;

    @FXML
    private Button assignButton;

    @FXML
    private Button markFixedButton;

    @FXML
    private Button resolveButton;

    @FXML
    private Button failButton;

    @FXML
    private Button closeButton;

    @FXML
    private Button reopenButton;

    @FXML
    private void initialize() {
        configureTable();
        configureFilters();
        bindTableData();
        configureSupportPanels();
        showIssueDetails(null);
        configureRoleScopedActions();

        issueTable.getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> loadDetails(newValue));

        refreshData();
    }

    @FXML
    private void resetFilters() {
        resetFiltersInternal();
    }

    @FXML
    private void openCreateIssue() {
        if (mainLayoutController != null) {
            mainLayoutController.showCreateIssue(null);
        }
    }

    @FXML
    private void handleWorkflowAction(ActionEvent event) {
        IssueRowModel selectedIssue = issueTable.getSelectionModel().getSelectedItem();
        if (selectedIssue == null) {
            UiAlertHelper.showInfo("No Issue Selected", "Choose an issue first.", "Select an issue from the list before running a workflow action.");
            return;
        }

        String action = ((Button) event.getSource()).getText();
        try {
            IssueDetailResponse updatedIssue = switch (action) {
                case "Assign" -> handleAssign(selectedIssue);
                case "Mark Fixed" -> handleMarkFixed(selectedIssue);
                case "Resolve" -> handleResolve(selectedIssue);
                case "Fail" -> handleFail(selectedIssue);
                case "Close" -> handleClose(selectedIssue);
                case "Reopen" -> handleReopen(selectedIssue);
                default -> throw new IllegalArgumentException("Unsupported action: " + action);
            };

            issueDetailCache.put(updatedIssue.getIssueId(), updatedIssue);
            refreshData();
            showIssueById(updatedIssue.getIssueId());
        } catch (Exception exception) {
            UiAlertHelper.showError("Workflow Action Failed", action + " could not be completed.", exception);
        }
    }

    @FXML
    private void handleAddComment() {
        IssueRowModel selectedIssue = issueTable.getSelectionModel().getSelectedItem();
        AuthenticatedUser currentUser = UserSession.getCurrentUser();

        commentFeedbackLabel.getStyleClass().setAll("form-feedback");

        if (selectedIssue == null) {
            commentFeedbackLabel.getStyleClass().add("form-feedback-error");
            commentFeedbackLabel.setText("Select an issue before adding a comment.");
            return;
        }

        if (currentUser == null) {
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
                currentUser.accountId(),
                content
            );
            IssueDetailResponse updatedIssue = backendBridge().addComment(request);
            issueDetailCache.put(updatedIssue.getIssueId(), updatedIssue);
            refreshRowFromDetail(updatedIssue);
            showIssueDetails(updatedIssue);
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
        IssueRowModel selectedIssue = issueTable.getSelectionModel().getSelectedItem();
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

    private IssueDetailResponse handleAssign(IssueRowModel selectedIssue) {
        IssueDetailResponse detail = issueDetailCache.computeIfAbsent(selectedIssue.getIssueId(), id -> backendBridge().getIssue(id));
        Map<Long, Double> recommendationScores = loadRecommendations(detail).stream()
            .collect(Collectors.toMap(RecommendationResponse::getAccountId, RecommendationResponse::getScore, (left, right) -> left));

        List<AccountChoice> developerChoices = backendBridge().getDeveloperAccounts().stream()
            .map(account -> {
                Double score = recommendationScores.get(account.getAccountId());
                return new AccountChoice(
                    account.getAccountId(),
                    account.getName() + " (" + account.getLoginId() + ")",
                    score
                );
            })
            .sorted(Comparator
                .comparing(AccountChoice::isRecommended).reversed()
                .thenComparing(AccountChoice::scoreOrZero, Comparator.reverseOrder())
                .thenComparing(AccountChoice::label, String.CASE_INSENSITIVE_ORDER))
            .toList();

        if (developerChoices.isEmpty()) {
            throw new IllegalStateException("No active developer accounts are available for assignment.");
        }

        ChoiceDialog<AccountChoice> dialog = new ChoiceDialog<>(developerChoices.get(0), developerChoices);
        dialog.setTitle("Assign Issue");
        dialog.setHeaderText("Select a developer for issue #" + selectedIssue.getIssueId());
        dialog.setContentText("Assignee:");

        Optional<AccountChoice> choice = dialog.showAndWait();
        if (choice.isEmpty()) {
            throw new IllegalStateException("Assignment was cancelled.");
        }

        IssueAssignRequest request = new IssueAssignRequest();
        request.setIssueId(selectedIssue.getIssueId());
        request.setPlAccountId(UserSession.getCurrentUser().accountId());
        request.setAssigneeAccountId(choice.get().accountId());
        return backendBridge().assign(request);
    }

    private IssueDetailResponse handleMarkFixed(IssueRowModel selectedIssue) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mark Fixed");
        dialog.setHeaderText("Add a required fix note for issue #" + selectedIssue.getIssueId());
        dialog.setContentText("Fix note:");
        Optional<String> note = dialog.showAndWait();
        if (note.isEmpty() || note.get().trim().isBlank()) {
            throw new IllegalArgumentException("A fix note is required before marking the issue as fixed.");
        }

        IssueFixRequest request = new IssueFixRequest();
        request.setIssueId(selectedIssue.getIssueId());
        request.setDevAccountId(UserSession.getCurrentUser().accountId());
        request.setComment(note.get().trim());
        return backendBridge().fix(request);
    }

    private IssueDetailResponse handleResolve(IssueRowModel selectedIssue) {
        IssueResolveRequest request = new IssueResolveRequest();
        request.setIssueId(selectedIssue.getIssueId());
        request.setTesterAccountId(UserSession.getCurrentUser().accountId());
        return backendBridge().resolve(request);
    }

    private IssueDetailResponse handleFail(IssueRowModel selectedIssue) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Fail Verification");
        dialog.setHeaderText("Explain why issue #" + selectedIssue.getIssueId() + " should be reopened.");
        dialog.setContentText("Failure reason:");
        Optional<String> reason = dialog.showAndWait();
        if (reason.isEmpty() || reason.get().trim().isBlank()) {
            throw new IllegalArgumentException("A failure reason is required before reopening the issue.");
        }

        IssueFailRequest request = new IssueFailRequest();
        request.setIssueId(selectedIssue.getIssueId());
        request.setTesterAccountId(UserSession.getCurrentUser().accountId());
        request.setReason(reason.get().trim());
        return backendBridge().fail(request);
    }

    private IssueDetailResponse handleClose(IssueRowModel selectedIssue) {
        IssueCloseRequest request = new IssueCloseRequest();
        request.setIssueId(selectedIssue.getIssueId());
        request.setPlAccountId(UserSession.getCurrentUser().accountId());
        return backendBridge().close(request);
    }

    private IssueDetailResponse handleReopen(IssueRowModel selectedIssue) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reopen Issue");
        dialog.setHeaderText("Add a reason for reopening issue #" + selectedIssue.getIssueId() + ".");
        dialog.setContentText("Reopen reason:");
        Optional<String> reason = dialog.showAndWait();
        if (reason.isEmpty() || reason.get().trim().isBlank()) {
            throw new IllegalArgumentException("A reopen reason is required.");
        }

        IssueReopenRequest request = new IssueReopenRequest();
        request.setIssueId(selectedIssue.getIssueId());
        request.setReopenAccountId(UserSession.getCurrentUser().accountId());
        request.setReason(reason.get().trim());
        return backendBridge().reopen(request);
    }

    private void configureTable() {
        idColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getIssueId()));
        titleColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getTitle()));

        issueTable.setPlaceholder(new Label("No issues match the current filters."));
    }

    private void configureSupportPanels() {
        recommendationListView.setPlaceholder(new Label("Choose an issue to see suggested developers."));
        commentListView.setPlaceholder(new Label("No activity has been recorded for this issue yet."));
        commentFeedbackLabel.getStyleClass().setAll("form-feedback");
        commentFeedbackLabel.setText("");
    }

    private void configureRoleScopedActions() {
        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        UiRole role = currentUser == null ? null : currentUser.role();

        boolean isPl = role == UiRole.PL;
        boolean isDev = role == UiRole.DEV;
        boolean isTester = role == UiRole.TESTER;
        boolean canReopenClosedIssue = isPl || isTester;
        boolean canComment = role == UiRole.PL || role == UiRole.DEV || role == UiRole.TESTER;
        boolean canEditTags = canComment;
        boolean canCreateIssue = role != null && role.canCreateIssue();

        setActionVisibility(assignButton, isPl);
        setActionVisibility(closeButton, isPl);
        setActionVisibility(markFixedButton, isDev);
        setActionVisibility(reopenButton, canReopenClosedIssue);
        setActionVisibility(resolveButton, isTester);
        setActionVisibility(failButton, isTester);

        recommendationSection.setVisible(isPl);
        recommendationSection.setManaged(isPl);
        workflowButtonBar.setVisible(isPl || isDev || isTester);
        workflowButtonBar.setManaged(isPl || isDev || isTester);
        editTagsButton.setVisible(canEditTags);
        editTagsButton.setManaged(canEditTags);
        commentSection.setVisible(canComment);
        commentSection.setManaged(canComment);
        saveCommentButton.setDisable(!canComment);
        createIssueButton.setVisible(canCreateIssue);
        createIssueButton.setManaged(canCreateIssue);
    }

    private void configureFilters() {
        keywordField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        statusFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        priorityFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        reporterFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        assigneeFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void bindTableData() {
        SortedList<IssueRowModel> sortedIssues = new SortedList<>(filteredIssues);
        sortedIssues.comparatorProperty().bind(issueTable.comparatorProperty());
        issueTable.setItems(sortedIssues);
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
        try {
            refreshProjectNames();
            issueDetailCache.clear();
            recommendationCache.clear();

            IssueSearchCondition condition = new IssueSearchCondition();
            condition.setProjectId(UserSession.getCurrentProjectId());
            AuthenticatedUser currentUser = UserSession.getCurrentUser();
            if (currentUser != null && currentUser.role() == UiRole.DEV) {
                condition.setAssigneeAccountId(currentUser.accountId());
            }
            List<IssueSummaryResponse> summaries = backendBridge().searchIssues(condition);
            issues.setAll(
                summaries.stream()
                    .map(summary -> UiModelMapper.toIssueRowModel(summary, projectNameById))
                    .toList()
            );
            refreshFilterOptions();
            applyFilters();

            if (!filteredIssues.isEmpty()) {
                issueTable.getSelectionModel().selectFirst();
            } else {
                showIssueDetails(null);
            }
        } catch (Exception exception) {
            issues.clear();
            showIssueDetails(null);
            UiAlertHelper.showError("Issue Browser Failed", "Could not load the issue list.", exception);
        }
    }

    public void showIssueById(Long issueId) {
        if (issueId == null) {
            return;
        }

        refreshData();
        issues.stream()
            .filter(issue -> issue.getIssueId().equals(issueId))
            .findFirst()
            .ifPresentOrElse(issue -> {
                issueTable.getSelectionModel().select(issue);
                issueTable.scrollTo(issue);
            }, () -> loadIssueAcrossProjects(issueId));
    }

    private void loadIssueAcrossProjects(Long issueId) {
        try {
            IssueSearchCondition condition = new IssueSearchCondition();
            AuthenticatedUser currentUser = UserSession.getCurrentUser();
            if (currentUser != null && currentUser.role() == UiRole.DEV) {
                condition.setAssigneeAccountId(currentUser.accountId());
            }
            List<IssueSummaryResponse> summaries = backendBridge().searchIssues(condition);
            issues.setAll(
                summaries.stream()
                    .map(summary -> UiModelMapper.toIssueRowModel(summary, projectNameById))
                    .toList()
            );
            refreshFilterOptions();
            applyFilters();
            issues.stream()
                .filter(issue -> issue.getIssueId().equals(issueId))
                .findFirst()
                .ifPresent(issue -> {
                    issueTable.getSelectionModel().select(issue);
                    issueTable.scrollTo(issue);
                });
        } catch (Exception exception) {
            UiAlertHelper.showError("Issue Browser Failed", "Could not load issues across projects.", exception);
        }
    }

    private void applyFilters() {
        String keyword = keywordField.getText() == null ? "" : keywordField.getText().trim().toLowerCase(Locale.ROOT);
        String selectedStatus = statusFilterCombo.getValue();
        String selectedPriority = priorityFilterCombo.getValue();
        String selectedReporter = reporterFilterCombo.getValue();
        String selectedAssignee = assigneeFilterCombo.getValue();

        filteredIssues.setPredicate(issue -> matchesFilters(issue, keyword, selectedStatus, selectedPriority, selectedReporter, selectedAssignee));

        if (filteredIssues.isEmpty()) {
            showIssueDetails(null);
        } else if (issueTable.getSelectionModel().getSelectedItem() == null || !filteredIssues.contains(issueTable.getSelectionModel().getSelectedItem())) {
            issueTable.getSelectionModel().selectFirst();
        }
    }

    private boolean matchesFilters(IssueRowModel issue, String keyword, String selectedStatus, String selectedPriority, String selectedReporter, String selectedAssignee) {
        if (selectedStatus != null && !ALL_OPTION.equals(selectedStatus) && !issue.getStatusDisplayName().equalsIgnoreCase(selectedStatus)) {
            return false;
        }
        if (selectedPriority != null && !ALL_OPTION.equals(selectedPriority) && !issue.getPriorityDisplayName().equalsIgnoreCase(selectedPriority)) {
            return false;
        }
        if (selectedReporter != null && !ALL_OPTION.equals(selectedReporter) && !issue.getReporterName().equalsIgnoreCase(selectedReporter)) {
            return false;
        }
        if (selectedAssignee != null && !ALL_OPTION.equals(selectedAssignee) && !issue.getAssigneeDisplayName().equalsIgnoreCase(selectedAssignee)) {
            return false;
        }
        if (keyword.isBlank()) {
            return true;
        }

        if (issue.getTitle().toLowerCase(Locale.ROOT).contains(keyword)
            || issue.getReporterName().toLowerCase(Locale.ROOT).contains(keyword)
            || issue.getAssigneeDisplayName().toLowerCase(Locale.ROOT).contains(keyword)
            || issue.getProjectName().toLowerCase(Locale.ROOT).contains(keyword)) {
            return true;
        }

        try {
            IssueDetailResponse detail = issueDetailCache.computeIfAbsent(issue.getIssueId(), id -> backendBridge().getIssue(id));
            String description = detail.getDescription() == null ? "" : detail.getDescription().toLowerCase(Locale.ROOT);
            return description.contains(keyword);
        } catch (Exception exception) {
            return false;
        }
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

        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        if (currentUser != null && currentUser.role() == UiRole.DEV) {
            String option = currentUser.name();
            ensureOptionPresent(assigneeFilterCombo, option);
            assigneeFilterCombo.setValue(option);
            assigneeFilterCombo.setDisable(true);
        } else {
            assigneeFilterCombo.setDisable(false);
        }
    }

    private void refreshProjectNames() {
        projectNameById.clear();
        for (ProjectResponse project : backendBridge().getProjects()) {
            projectNameById.put(project.getProjectId(), project.getName());
        }
    }

    private void loadDetails(IssueRowModel issue) {
        if (issue == null) {
            showIssueDetails(null);
            return;
        }

        try {
            IssueDetailResponse detail = issueDetailCache.computeIfAbsent(issue.getIssueId(), id -> backendBridge().getIssue(id));
            showIssueDetails(detail);
        } catch (Exception exception) {
            UiAlertHelper.showError("Issue Detail Failed", "Could not load the selected issue.", exception);
            showIssueDetails(null);
        }
    }

    private void showIssueDetails(IssueDetailResponse issue) {
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
            tagsValueLabel.setText("-");
            descriptionArea.clear();
            commentListView.setItems(FXCollections.observableArrayList("No issue selected."));
            recommendationListView.setItems(FXCollections.observableArrayList("Choose an issue to see suggested developers."));
            commentInputArea.clear();
            commentFeedbackLabel.getStyleClass().setAll("form-feedback");
            commentFeedbackLabel.setText("");
            statusBadgeLabel.getStyleClass().setAll("label", "badge", "status-badge");
            priorityBadgeLabel.getStyleClass().setAll("label", "badge", "priority-badge");
            updateWorkflowAvailability(null);
            return;
        }

        UiIssueStatus uiStatus = UiModelMapper.toUiIssueStatus(issue.getStatus());
        UiPriority uiPriority = UiModelMapper.toUiPriority(issue.getPriority());

        issueIdLabel.setText("#" + issue.getIssueId());
        issueTitleLabel.setText(issue.getTitle());
        statusBadgeLabel.setText(uiStatus.displayName());
        priorityBadgeLabel.setText(uiPriority.displayName());
        reporterValueLabel.setText(issue.getReporterName());
        assigneeValueLabel.setText(issue.getAssigneeName() == null || issue.getAssigneeName().isBlank() ? UNASSIGNED_OPTION : issue.getAssigneeName());
        fixerValueLabel.setText(issue.getFixerName() == null || issue.getFixerName().isBlank() ? "-" : issue.getFixerName());
        projectValueLabel.setText(issue.getProjectName());
        reportedAtValueLabel.setText(formatDateTime(issue.getReportedAt()));
        tagsValueLabel.setText(formatTagNames(issue.getTagNames()));
        descriptionArea.setText(issue.getDescription());
        commentListView.setItems(FXCollections.observableArrayList(UiModelMapper.toActivityTimeline(issue)));
        recommendationListView.setItems(FXCollections.observableArrayList(buildRecommendationLines(issue)));
        commentFeedbackLabel.getStyleClass().setAll("form-feedback");
        commentFeedbackLabel.setText("");

        statusBadgeLabel.getStyleClass().setAll("label", "badge", "status-badge", "status-" + uiStatus.name().toLowerCase(Locale.ROOT));
        priorityBadgeLabel.getStyleClass().setAll("label", "badge", "priority-badge", "priority-" + uiPriority.name().toLowerCase(Locale.ROOT));
        updateWorkflowAvailability(issue);
    }

    private void updateWorkflowAvailability(IssueDetailResponse issue) {
        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        UiRole role = currentUser == null ? null : currentUser.role();

        boolean hasIssue = issue != null;
        UiIssueStatus status = hasIssue ? UiModelMapper.toUiIssueStatus(issue.getStatus()) : null;
        boolean assignedToCurrentDev = hasIssue
            && currentUser != null
            && issue.getAssigneeAccountId() != null
            && issue.getAssigneeAccountId().equals(currentUser.accountId());

        boolean canActAsPl = role == UiRole.PL;
        boolean canEditTags = role == UiRole.PL || role == UiRole.DEV || role == UiRole.TESTER;

        assignButton.setDisable(!hasIssue || !canActAsPl || (status != UiIssueStatus.NEW && status != UiIssueStatus.REOPENED));
        closeButton.setDisable(!hasIssue || !canActAsPl || status != UiIssueStatus.RESOLVED);
        markFixedButton.setDisable(!hasIssue || role != UiRole.DEV || status != UiIssueStatus.ASSIGNED || !assignedToCurrentDev);
        reopenButton.setDisable(!hasIssue || (role != UiRole.PL && role != UiRole.TESTER) || status != UiIssueStatus.CLOSED);
        resolveButton.setDisable(!hasIssue || role != UiRole.TESTER || status != UiIssueStatus.FIXED);
        failButton.setDisable(!hasIssue || role != UiRole.TESTER || status != UiIssueStatus.FIXED);
        editTagsButton.setDisable(!hasIssue || !canEditTags);
    }

    private void setActionVisibility(Button button, boolean visible) {
        button.setVisible(visible);
        button.setManaged(visible);
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

    private List<String> buildRecommendationLines(IssueDetailResponse issue) {
        if (issue == null) {
            return List.of("Choose an issue to see suggested developers.");
        }
        if (issue.getTagNames() == null || issue.getTagNames().isEmpty()) {
            return List.of("Add one or more tags to this issue to unlock suggestions.");
        }
        return formatRecommendations(loadRecommendations(issue));
    }

    private List<String> formatRecommendations(List<RecommendationResponse> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            return List.of("No suitable developers were ranked for this issue yet.");
        }

        return recommendations.stream()
            .map(recommendation -> recommendation.getName()
                + " (" + recommendation.getLoginId() + ")"
                + " | score "
                + String.format(Locale.US, "%.1f", recommendation.getScore()))
            .toList();
    }

    private void refreshRowFromDetail(IssueDetailResponse detail) {
        if (detail == null) {
            return;
        }

        for (int index = 0; index < issues.size(); index++) {
            if (detail.getIssueId().equals(issues.get(index).getIssueId())) {
                issues.set(index, UiModelMapper.toIssueRowModel(detail));
                break;
            }
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "-" : dateTime.format(DATE_TIME_FORMATTER);
    }

    private String formatTagNames(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return "-";
        }
        return tagNames.stream()
            .filter(tagName -> tagName != null && !tagName.isBlank())
            .collect(Collectors.joining(", "));
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
        showIssueDetails(updatedIssue);
    }

    private Map<String, ProjectTagOption> projectTagMap(Long projectId) {
        Map<String, ProjectTagOption> tagByName = new LinkedHashMap<>();
        for (ProjectTagOption tag : backendBridge().getProjectTags(projectId)) {
            tagByName.putIfAbsent(normalizeTag(tag.name()), tag);
        }
        return tagByName;
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

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    private record AccountChoice(Long accountId, String label, Double recommendationScore) {
        private boolean isRecommended() {
            return recommendationScore != null;
        }

        private double scoreOrZero() {
            return recommendationScore == null ? 0.0 : recommendationScore;
        }

        @Override
        public String toString() {
            if (recommendationScore == null) {
                return label;
            }
            return label + " | suggested " + String.format(Locale.US, "%.1f", recommendationScore);
        }
    }
}
