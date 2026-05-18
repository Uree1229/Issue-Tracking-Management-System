package com.example.its.ui.javafx.controller;

import com.example.its.shared.dto.issue.IssueCreateRequest;
import com.example.its.shared.dto.issue.IssueDetailResponse;
import com.example.its.ui.javafx.model.AuthenticatedUser;
import com.example.its.ui.javafx.model.ProjectTagOption;
import com.example.its.ui.javafx.model.UiPriority;
import com.example.its.ui.javafx.service.JavaFxBackendBridge;
import com.example.its.ui.javafx.session.UserSession;
import com.example.its.ui.javafx.support.UiAlertHelper;
import com.example.its.ui.javafx.support.TagEditorDialog;
import com.example.its.ui.javafx.support.UiModelMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateIssueController {
    private final ObservableList<String> draftIssueTags = FXCollections.observableArrayList();

    private MainLayoutController mainLayoutController;

    @FXML
    private Label reporterValueLabel;

    @FXML
    private Label projectValueLabel;

    @FXML
    private Label statusValueLabel;

    @FXML
    private TextField titleField;

    @FXML
    private javafx.scene.control.ComboBox<UiPriority> priorityCombo;

    @FXML
    private Label selectedTagsLabel;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextArea noteArea;

    @FXML
    private Label formFeedbackLabel;

    @FXML
    private void initialize() {
        priorityCombo.setItems(FXCollections.observableArrayList(UiPriority.values()));
        priorityCombo.setValue(UiPriority.MAJOR);
        refreshContext();
    }

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void refreshContext() {
        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        String reporterDisplay = currentUser == null ? "-" : currentUser.displayName();
        reporterValueLabel.setText(reporterDisplay);
        String projectName = UserSession.getCurrentProjectName();
        projectValueLabel.setText(projectName == null || projectName.isBlank() ? "No Project Selected" : projectName);
        statusValueLabel.setText("NEW");
        refreshSelectedTagsLabel();
    }

    @FXML
    private void handleCreateIssue() {
        formFeedbackLabel.getStyleClass().setAll("form-feedback");

        String title = trimmed(titleField.getText());
        String description = trimmed(descriptionArea.getText());
        String note = trimmed(noteArea.getText());
        AuthenticatedUser currentUser = UserSession.getCurrentUser();

        if (currentUser == null) {
            formFeedbackLabel.getStyleClass().add("form-feedback-error");
            formFeedbackLabel.setText("You need to be signed in before creating a new issue.");
            return;
        }

        if (title.isBlank() || description.isBlank()) {
            formFeedbackLabel.getStyleClass().add("form-feedback-error");
            formFeedbackLabel.setText("Title and description are required.");
            return;
        }
        Long projectId = UserSession.getCurrentProjectId();
        if (projectId == null) {
            formFeedbackLabel.getStyleClass().add("form-feedback-error");
            formFeedbackLabel.setText("Create a project first. The current backend requires a project before issue registration.");
            return;
        }

        try {
            List<String> issueTagNames = List.copyOf(draftIssueTags);
            IssueCreateRequest request = new IssueCreateRequest();
            request.setTitle(title);
            request.setDescription(buildEffectiveDescription(description, note));
            request.setPriority(UiModelMapper.toBackendPriority(priorityCombo.getValue()));
            request.setReporterAccountId(currentUser.accountId());
            request.setProjectId(projectId);
            List<Long> tagIds = resolveTagIds(projectId, issueTagNames);
            if (!tagIds.isEmpty()) {
                request.setTagIds(tagIds);
            }

            IssueDetailResponse createdIssue = backendBridge().createIssue(request);
            formFeedbackLabel.getStyleClass().add("form-feedback-success");
            formFeedbackLabel.setText("Issue #" + createdIssue.getIssueId() + " was created successfully.");
            resetForm();

            if (mainLayoutController != null) {
                mainLayoutController.showIssue(createdIssue.getIssueId());
            }
        } catch (Exception exception) {
            formFeedbackLabel.getStyleClass().add("form-feedback-error");
            formFeedbackLabel.setText(UiAlertHelper.extractMessage(exception, "Issue creation failed."));
        }
    }

    @FXML
    private void handleReset() {
        resetForm();
    }

    @FXML
    private void handleEditTags() {
        Long projectId = UserSession.getCurrentProjectId();
        if (projectId == null) {
            formFeedbackLabel.getStyleClass().setAll("form-feedback", "form-feedback-error");
            formFeedbackLabel.setText("Select a project before editing tags.");
            return;
        }

        List<String> availableTags = backendBridge().getProjectTags(projectId).stream()
            .map(ProjectTagOption::name)
            .toList();

        TagEditorDialog.show(
            "Tags",
            "Choose the tags that should be attached when this issue is created.",
            draftIssueTags,
            availableTags,
            TagEditorDialog.issueLabels()
        ).ifPresent(updatedTags -> {
            draftIssueTags.setAll(updatedTags);
            refreshSelectedTagsLabel();
        });
    }

    private void resetForm() {
        titleField.clear();
        descriptionArea.clear();
        noteArea.clear();
        priorityCombo.setValue(UiPriority.MAJOR);
        draftIssueTags.clear();
        refreshSelectedTagsLabel();
        formFeedbackLabel.getStyleClass().setAll("form-feedback");
        formFeedbackLabel.setText("");
    }

    private String trimmed(String value) {
        return value == null ? "" : value.trim();
    }

    private String buildEffectiveDescription(String description, String note) {
        if (note.isBlank()) {
            return description;
        }
        return description + System.lineSeparator() + System.lineSeparator() + "[Initial Note]" + System.lineSeparator() + note;
    }

    private void refreshSelectedTagsLabel() {
        if (draftIssueTags.isEmpty()) {
            selectedTagsLabel.setText("No tags selected");
            return;
        }
        selectedTagsLabel.setText(String.join(", ", draftIssueTags));
    }

    private List<Long> resolveTagIds(Long projectId, List<String> requestedTagNames) {
        if (requestedTagNames == null || requestedTagNames.isEmpty()) {
            return List.of();
        }

        Map<String, ProjectTagOption> existingTags = backendBridge().getProjectTags(projectId).stream()
            .collect(Collectors.toMap(tag -> normalizeTag(tag.name()), tag -> tag, (left, right) -> left));

        Map<String, ProjectTagOption> initialExistingTags = existingTags;
        List<String> newProjectTags = requestedTagNames.stream()
            .map(this::trimmed)
            .filter(name -> !name.isBlank())
            .filter(name -> !initialExistingTags.containsKey(normalizeTag(name)))
            .distinct()
            .toList();

        if (!newProjectTags.isEmpty()) {
            backendBridge().updateProjectTags(projectId, newProjectTags, List.of());
            existingTags = backendBridge().getProjectTags(projectId).stream()
                .collect(Collectors.toMap(tag -> normalizeTag(tag.name()), tag -> tag, (left, right) -> left));
            if (mainLayoutController != null) {
                mainLayoutController.reloadProjectOptions();
            }
        }

        Map<String, ProjectTagOption> resolvedTags = existingTags;
        List<Long> tagIds = requestedTagNames.stream()
            .map(this::trimmed)
            .filter(name -> !name.isBlank())
            .map(name -> resolvedTags.get(normalizeTag(name)))
            .filter(java.util.Objects::nonNull)
            .map(ProjectTagOption::tagId)
            .distinct()
            .toList();

        if (tagIds.size() != requestedTagNames.stream().map(this::trimmed).filter(name -> !name.isBlank()).map(this::normalizeTag).distinct().count()) {
            throw new IllegalStateException("One or more tags could not be resolved.");
        }

        return tagIds;
    }

    private String normalizeTag(String tagName) {
        return trimmed(tagName).toLowerCase(Locale.ROOT);
    }

    private JavaFxBackendBridge backendBridge() {
        return JavaFxBackendBridge.getInstance();
    }
}
