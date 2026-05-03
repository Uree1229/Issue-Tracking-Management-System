package com.example.its.ui.javafx.controller;

import com.example.its.ItsApplication;
import com.example.its.ui.javafx.model.AuthenticatedUser;
import com.example.its.ui.javafx.model.UiRole;
import com.example.its.ui.javafx.session.AuthNavigationState;
import com.example.its.ui.javafx.session.UserSession;
import com.example.its.ui.javafx.support.IntegrationPointHelper;
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

        if (loginId.isBlank()) {
            showErrorFeedback("Preview mode needs a login ID so we can label the current user.");
            return;
        }

        IntegrationPointHelper.showPending(
            "Login backend pending",
            "Connect LoginController.handleLogin() to AccountFacade.authenticate(LoginRequest). "
                + "For now, this button only opens the JavaFX preview shell as a TESTER role."
        );
        UserSession.setCurrentUser(createPreviewUser(loginId, UiRole.TESTER));
        showNeutralFeedback("");
        ItsApplication.showMainView();
    }

    @FXML
    private void openRegister() {
        showNeutralFeedback("");
        ItsApplication.showRegisterView();
    }

    @FXML
    private void loginAsAdmin() {
        openPreviewAs("admin", UiRole.ADMIN);
    }

    @FXML
    private void loginAsPl() {
        openPreviewAs("pl1", UiRole.PL);
    }

    @FXML
    private void loginAsDev() {
        openPreviewAs("dev1", UiRole.DEV);
    }

    @FXML
    private void loginAsTester() {
        openPreviewAs("tester1", UiRole.TESTER);
    }

    private void openPreviewAs(String loginId, UiRole role) {
        loginIdField.setText(loginId);
        passwordField.clear();
        IntegrationPointHelper.showPending(
            "Role preview only",
            "This quick button is for JavaFX page review. Replace it with real authentication through "
                + "AccountFacade.authenticate(LoginRequest) when the backend integration starts."
        );
        UserSession.setCurrentUser(createPreviewUser(loginId, role));
        showNeutralFeedback("");
        ItsApplication.showMainView();
    }

    private AuthenticatedUser createPreviewUser(String loginId, UiRole role) {
        return new AuthenticatedUser(0L, loginId, "Preview " + role.name(), role);
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
}
