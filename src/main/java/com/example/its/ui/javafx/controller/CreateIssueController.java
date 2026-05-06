package com.example.its.ui.javafx.controller;

import com.example.its.shared.dto.issue.IssueCreateRequest;
import com.example.its.shared.dto.issue.IssueDetailResponse;
import com.example.its.ui.javafx.model.AuthenticatedUser;
import com.example.its.ui.javafx.model.UiPriority;
import com.example.its.ui.javafx.service.JavaFxBackendBridge;
import com.example.its.ui.javafx.session.UserSession;
import com.example.its.ui.javafx.support.UiAlertHelper;
import com.example.its.ui.javafx.support.UiModelMapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CreateIssueController {

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
    private TextArea descriptionArea;

    @FXML
    private TextArea noteArea;

    @FXML
    private Label previewTitleLabel;

    @FXML
    private Label previewPriorityLabel;

    @FXML
    private Label previewReporterLabel;

    @FXML
    private Label previewDescriptionLabel;

    @FXML
    private Label formFeedbackLabel;

    @FXML
    private void initialize() {
        priorityCombo.setItems(FXCollections.observableArrayList(UiPriority.values()));
        priorityCombo.setValue(UiPriority.MAJOR);

        titleField.textProperty().addListener((observable, oldValue, newValue) -> updatePreview());
        descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> updatePreview());
        priorityCombo.valueProperty().addListener((observable, oldValue, newValue) -> updatePreview());

        refreshContext();
        updatePreview();
    }

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void refreshContext() {
        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        String reporterDisplay = currentUser == null ? "-" : currentUser.displayName();
        reporterValueLabel.setText(reporterDisplay);
        previewReporterLabel.setText(reporterDisplay);
        String projectName = UserSession.getCurrentProjectName();
        projectValueLabel.setText(projectName == null || projectName.isBlank() ? "No Project Selected" : projectName);
        statusValueLabel.setText("NEW");
        updatePreview();
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
        formFeedbackLabel.getStyleClass().setAll("form-feedback");
        formFeedbackLabel.setText("");
        updatePreview();
    }

    private void updatePreview() {
        String title = trimmed(titleField.getText());
        String description = trimmed(descriptionArea.getText());

        previewTitleLabel.setText(title.isBlank() ? "Waiting for title..." : title);
        previewPriorityLabel.setText(priorityCombo.getValue() == null ? "MAJOR" : priorityCombo.getValue().name());
        previewDescriptionLabel.setText(description.isBlank()
            ? "The issue description will appear here once the reporter adds a reproduction note."
            : description);
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

    private JavaFxBackendBridge backendBridge() {
        return JavaFxBackendBridge.getInstance();
    }
}
