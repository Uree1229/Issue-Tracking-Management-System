package com.example.its.ui.javafx.controller;

import com.example.its.shared.dto.issue.IssueCreateRequest;
import com.example.its.shared.dto.issue.IssueDetailResponse;
import com.example.its.ui.javafx.model.AuthenticatedUser;
import com.example.its.ui.javafx.model.ProjectTagOption;
import com.example.its.ui.javafx.model.UiPriority;
import com.example.its.ui.javafx.service.JavaFxBackendBridge;
import com.example.its.ui.javafx.session.UserSession;
import com.example.its.ui.javafx.support.UiAlertHelper;
import com.example.its.ui.javafx.support.UiModelMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CreateIssueController {
    private static final String NO_TAG_OPTION = "No Tag";

    private final ObservableList<String> tagOptions = FXCollections.observableArrayList(NO_TAG_OPTION);
    private final java.util.Map<String, Long> tagIdByOption = new java.util.HashMap<>();

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
    private ComboBox<UiPriority> priorityCombo;

    @FXML
    private ComboBox<String> tagCombo;

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
        tagCombo.setItems(tagOptions);
        tagCombo.setValue(NO_TAG_OPTION);

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
        refreshTagOptions();
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
            IssueCreateRequest request = new IssueCreateRequest();
            request.setTitle(title);
            request.setDescription(buildEffectiveDescription(description, note));
            request.setPriority(UiModelMapper.toBackendPriority(priorityCombo.getValue()));
            request.setReporterAccountId(currentUser.accountId());
            request.setProjectId(projectId);
            Long selectedTagId = tagIdByOption.get(tagCombo.getValue());
            if (selectedTagId != null) {
                request.setTagIds(java.util.List.of(selectedTagId));
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

    private void resetForm() {
        titleField.clear();
        descriptionArea.clear();
        noteArea.clear();
        priorityCombo.setValue(UiPriority.MAJOR);
        tagCombo.setValue(NO_TAG_OPTION);
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

    private void refreshTagOptions() {
        tagIdByOption.clear();
        tagOptions.setAll(NO_TAG_OPTION);

        Long projectId = UserSession.getCurrentProjectId();
        if (projectId == null) {
            tagCombo.setValue(NO_TAG_OPTION);
            return;
        }

        try {
            for (ProjectTagOption tag : backendBridge().getProjectTags(projectId)) {
                tagOptions.add(tag.name());
                tagIdByOption.put(tag.name(), tag.tagId());
            }
        } catch (Exception ignored) {
            // Keep issue creation available even if tag data cannot be loaded.
        }

        if (tagCombo.getValue() == null || !tagOptions.contains(tagCombo.getValue())) {
            tagCombo.setValue(NO_TAG_OPTION);
        }
    }

    private JavaFxBackendBridge backendBridge() {
        return JavaFxBackendBridge.getInstance();
    }
}
