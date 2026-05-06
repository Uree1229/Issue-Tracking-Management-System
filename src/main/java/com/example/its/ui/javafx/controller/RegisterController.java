package com.example.its.ui.javafx.controller;

import com.example.its.ItsApplication;
import com.example.its.shared.dto.account.AccountCreateRequest;
import com.example.its.ui.javafx.model.UiRole;
import com.example.its.ui.javafx.service.JavaFxBackendBridge;
import com.example.its.ui.javafx.session.AuthNavigationState;
import com.example.its.ui.javafx.support.UiAlertHelper;
import com.example.its.ui.javafx.support.UiModelMapper;
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

        try {
            AccountCreateRequest request = new AccountCreateRequest();
            request.setLoginId(loginId);
            request.setName(realName);
            request.setEmail(email);
            request.setPassword(password);
            request.setRole(UiModelMapper.toBackendRole(role));

            backendBridge().register(request);
            AuthNavigationState.prepareLoginPrefill(loginId, "Account created. Please sign in.");
            ItsApplication.showLoginView();
        } catch (Exception exception) {
            showErrorFeedback(UiAlertHelper.extractMessage(exception, "Account registration failed."));
        }
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

    private JavaFxBackendBridge backendBridge() {
        return JavaFxBackendBridge.getInstance();
    }
}
