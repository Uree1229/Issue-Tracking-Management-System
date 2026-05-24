package com.example.its.ui.javafx.controller;

import com.example.its.ItsApplication;
import com.example.its.shared.dto.account.AccountResponse;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.ui.javafx.model.AuthenticatedUser;
import com.example.its.ui.javafx.model.UiRole;
import com.example.its.ui.javafx.service.JavaFxBackendBridge;
import com.example.its.ui.javafx.session.AuthNavigationState;
import com.example.its.ui.javafx.session.UserSession;
import com.example.its.ui.javafx.support.UiAlertHelper;
import com.example.its.ui.javafx.support.UiModelMapper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField loginIdField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void initialize() {
        String pendingLoginId = AuthNavigationState.consumePendingLoginId();
        String pendingMessage = AuthNavigationState.consumePendingMessage();

        loginIdField.setText(pendingLoginId == null ? "" : pendingLoginId);
        passwordField.clear();
        showNeutralFeedback("");

        if (pendingMessage != null && !pendingMessage.isBlank()) {
            showSuccessFeedback(pendingMessage);
        }
    }

    @FXML
    private void handleLogin() {
        String loginId = loginIdField.getText() == null ? "" : loginIdField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (loginId.isBlank()) {
            showErrorFeedback("Login ID is required.");
            return;
        }
        if (password.isBlank()) {
            showErrorFeedback("Password is required.");
            return;
        }

        performLogin(loginId, password);
    }

    @FXML
    private void openRegister() {
        showNeutralFeedback("");
        ItsApplication.showRegisterView();
    }

    @FXML
    private void loginAsAdmin() {
        loginWithSeededCredentials("admin", "admin");
    }

    @FXML
    private void loginAsPl() {
        loginWithSeededCredentials("pl1", "pl1");
    }

    @FXML
    private void loginAsDev() {
        loginWithSeededCredentials("dev1", "dev1");
    }

    @FXML
    private void loginAsTester() {
        loginWithSeededCredentials("tester1", "tester1");
    }

    private void loginWithSeededCredentials(String loginId, String password) {
        loginIdField.setText(loginId);
        passwordField.setText(password);
        performLogin(loginId, password);
    }

    private void performLogin(String loginId, String password) {
        try {
            AccountResponse response = backendBridge().login(loginId, password);
            AuthenticatedUser authenticatedUser = UiModelMapper.toAuthenticatedUser(response);
            UserSession.setCurrentUser(authenticatedUser);
            initializeProjectContext();
            showNeutralFeedback("");
            ItsApplication.showMainView();
        } catch (Exception exception) {
            showErrorFeedback(UiAlertHelper.extractMessage(
                exception,
                "Login failed. If this account does not exist yet, create it first."
            ));
        }
    }

    private void initializeProjectContext() {
        ProjectResponse preferredProject = backendBridge().findPreferredProject(UserSession.getCurrentUser()).orElse(null);
        if (preferredProject == null) {
            UserSession.setCurrentProject(null, null);
            return;
        }
        UserSession.setCurrentProject(preferredProject.getProjectId(), preferredProject.getName());
    }

    private void showNeutralFeedback(String message) {
        errorLabel.getStyleClass().setAll("feedback-label");
        errorLabel.setText(message);
    }

    private void showErrorFeedback(String message) {
        errorLabel.getStyleClass().setAll("feedback-label", "feedback-label-error");
        errorLabel.setText(message);
    }

    private void showSuccessFeedback(String message) {
        errorLabel.getStyleClass().setAll("feedback-label", "feedback-label-success");
        errorLabel.setText(message);
    }

    private JavaFxBackendBridge backendBridge() {
        return JavaFxBackendBridge.getInstance();
    }
}
