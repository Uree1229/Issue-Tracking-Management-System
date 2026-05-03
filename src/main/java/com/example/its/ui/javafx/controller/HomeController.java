package com.example.its.ui.javafx.controller;

import com.example.its.ui.javafx.model.AuthenticatedUser;
import com.example.its.ui.javafx.model.UiRole;
import com.example.its.ui.javafx.session.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class HomeController {

    private MainLayoutController mainLayoutController;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label roleHintLabel;

    @FXML
    private Button reportIssueButton;

    @FXML
    private Button analyticsButton;

    @FXML
    private Button adminButton;

    @FXML
    private TextField quickSearchField;

    @FXML
    private void initialize() {
        AuthenticatedUser currentUser = UserSession.getCurrentUser();

        if (currentUser == null) {
            welcomeLabel.setText("Welcome to ITS");
            roleHintLabel.setText("Sign in to continue.");
            return;
        }

        welcomeLabel.setText("Welcome to ITS");
        roleHintLabel.setText(buildRoleHint(currentUser));

        UiRole role = currentUser.role();
        adminButton.setVisible(role.isAdmin());
        adminButton.setManaged(role.isAdmin());

        boolean canReportIssue = role.canCreateIssue();
        reportIssueButton.setVisible(canReportIssue);
        reportIssueButton.setManaged(canReportIssue);

        boolean canViewAnalytics = role.canViewAnalytics();
        analyticsButton.setVisible(canViewAnalytics);
        analyticsButton.setManaged(canViewAnalytics);
    }

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    @FXML
    private void openIssueRegistration() {
        if (mainLayoutController != null) {
            mainLayoutController.showCreateIssue(null);
        }
    }

    @FXML
    private void openSearch() {
        if (mainLayoutController != null) {
            mainLayoutController.showSearch(null);
        }
    }

    @FXML
    private void openAnalytics() {
        if (mainLayoutController != null) {
            mainLayoutController.showAnalytics(null);
        }
    }

    @FXML
    private void openAdmin() {
        if (mainLayoutController != null) {
            mainLayoutController.showAdmin(null);
        }
    }

    @FXML
    private void quickSearch() {
        if (mainLayoutController != null) {
            mainLayoutController.showSearchWithKeyword(quickSearchField.getText());
        }
    }

    @FXML
    private void showNewIssues() {
        if (mainLayoutController != null) {
            mainLayoutController.showNewIssues();
        }
    }

    @FXML
    private void showAssignedToMe() {
        if (mainLayoutController != null) {
            mainLayoutController.showAssignedToCurrentUser();
        }
    }

    @FXML
    private void showReportedByMe() {
        if (mainLayoutController != null) {
            mainLayoutController.showReportedByCurrentUser();
        }
    }

    @FXML
    private void showRecentlyFixed() {
        if (mainLayoutController != null) {
            mainLayoutController.showFixedIssues();
        }
    }

    private String buildRoleHint(AuthenticatedUser currentUser) {
        return switch (currentUser.role()) {
            case ADMIN -> "Use the admin area to manage accounts and projects.";
            case PL -> "Search issues, assign developers, and review project analytics.";
            case DEV -> "Browse assigned issues and track progress through the workflow.";
            case TESTER -> "Report bugs, verify fixes, and reopen issues when needed.";
        };
    }
}
