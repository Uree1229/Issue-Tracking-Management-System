package com.example.its.ui.javafx.controller;

import com.example.its.ItsApplication;
import com.example.its.ui.javafx.model.UiRole;
import com.example.its.ui.javafx.session.AuthNavigationState;
import com.example.its.ui.javafx.support.IntegrationPointHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField loginIdField;

    @FXML
    private TextField realNameField;

    @FXML
    private TextField emailField;

    @FXML
    private ComboBox<UiRole> roleCombo;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label feedbackLabel;

    @FXML
    private void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList(UiRole.PL, UiRole.DEV, UiRole.TESTER));
        roleCombo.setValue(UiRole.TESTER);
        showNeutralFeedback("");
    }

    @FXML
    private void handleRegister() {
        String loginId = trimmed(loginIdField.getText());
        String realName = trimmed(realNameField.getText());
        String email = trimmed(emailField.getText());
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        String confirmPassword = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();
        UiRole role = roleCombo.getValue();

        if (loginId.isBlank() || realName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() || role == null) {
            showErrorFeedback("Login ID, name, email, password, and role are required.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showErrorFeedback("Password confirmation does not match.");
            return;
        }

        IntegrationPointHelper.showPending(
            "Register backend pending",
            "Connect RegisterController.handleRegister() to AccountFacade.createAccount(AccountCreateRequest). "
                + "This page currently validates input only and returns to the login screen."
        );
        AuthNavigationState.prepareLoginPrefill(loginId, "Preview flow only. Connect account creation on the backend later.");
        ItsApplication.showLoginView();
    }

    @FXML
    private void goBackToLogin() {
        showNeutralFeedback("");
        ItsApplication.showLoginView();
    }

    private void showNeutralFeedback(String message) {
        feedbackLabel.getStyleClass().setAll("feedback-label");
        feedbackLabel.setText(message);
    }

    private void showErrorFeedback(String message) {
        feedbackLabel.getStyleClass().setAll("feedback-label", "feedback-label-error");
        feedbackLabel.setText(message);
    }

    private String trimmed(String value) {
        return value == null ? "" : value.trim();
    }
}
