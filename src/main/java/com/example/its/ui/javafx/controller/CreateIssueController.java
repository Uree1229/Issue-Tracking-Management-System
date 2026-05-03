package com.example.its.ui.javafx.controller;

import com.example.its.ui.javafx.model.AuthenticatedUser;
import com.example.its.ui.javafx.model.UiPriority;
import com.example.its.ui.javafx.session.UserSession;
import com.example.its.ui.javafx.support.IntegrationPointHelper;
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
        reporterValueLabel.setText(currentUser == null ? "-" : currentUser.loginId());
        previewReporterLabel.setText(currentUser == null ? "-" : currentUser.loginId());
        projectValueLabel.setText("project1");
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

        IntegrationPointHelper.showPending(
            "Issue creation backend pending",
            "Connect CreateIssueController.handleCreateIssue() to IssueFacade.createIssue(IssueCreateRequest). "
                + "Current form values stay in the UI preview only.\n\n"
                + "Title: " + title + "\n"
                + "Priority: " + (priorityCombo.getValue() == null ? UiPriority.MAJOR.name() : priorityCombo.getValue().name()) + "\n"
                + "Reporter note: " + (note.isBlank() ? "-" : note)
        );
        formFeedbackLabel.getStyleClass().add("form-feedback-success");
        formFeedbackLabel.setText("Preview only. Hook this button to IssueFacade.createIssue(...) later.");
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
}
