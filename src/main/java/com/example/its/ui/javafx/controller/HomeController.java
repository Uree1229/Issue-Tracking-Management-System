package com.example.its.ui.javafx.controller;

import com.example.its.ui.javafx.model.AuthenticatedUser;
import com.example.its.ui.javafx.model.UiRole;
import com.example.its.ui.javafx.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class HomeController {

    private MainLayoutController mainLayoutController;

    @FXML
    private Label welcomeLabel;

    @FXML
    private HBox quickSearchShell;

    @FXML
    private HBox featureRow;

    @FXML
    private Button reportIssueButton;

    @FXML
    private Button searchIssuesButton;

    @FXML
    private Button analyticsButton;

    @FXML
    private Button adminButton;

    @FXML
    private Label reportIssueTitleLabel;

    @FXML
    private Label searchIssuesTitleLabel;

    @FXML
    private Label analyticsTitleLabel;

    @FXML
    private Label adminTitleLabel;

    @FXML
    private TextField quickSearchField;

    @FXML
    private void initialize() {
        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        welcomeLabel.setText("Welcome to ITS");

        if (currentUser == null) {
            configureGuestLayout();
            return;
        }

        switch (currentUser.role()) {
            case ADMIN -> configureAdminLayout();
            case DEV -> configureDeveloperLayout();
            case TESTER -> configureTesterLayout();
            case PL -> configureProjectLeadLayout();
        }
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
            String keyword = quickSearchField.getText() == null ? "" : quickSearchField.getText().trim();
            if (keyword.isBlank()) {
                mainLayoutController.showSearch(null);
                return;
            }
            mainLayoutController.showSearchWithKeyword(keyword);
        }
    }

    private void configureGuestLayout() {
        quickSearchShell.setVisible(false);
        quickSearchShell.setManaged(false);
        featureRow.setVisible(false);
        featureRow.setManaged(false);
    }

    private void configureAdminLayout() {
        quickSearchShell.setVisible(false);
        quickSearchShell.setManaged(false);

        reportIssueButton.setVisible(false);
        reportIssueButton.setManaged(false);
        searchIssuesButton.setVisible(false);
        searchIssuesButton.setManaged(false);

        adminTitleLabel.setText("Admin");
        analyticsTitleLabel.setText("Project");

        adminButton.setOnAction(event -> openAdmin());
        analyticsButton.setOnAction(this::openProjectManagementHome);

        adminButton.setVisible(true);
        adminButton.setManaged(true);
        analyticsButton.setVisible(true);
        analyticsButton.setManaged(true);
        featureRow.getChildren().setAll(adminButton, analyticsButton);
    }

    private void configureDeveloperLayout() {
        quickSearchShell.setVisible(true);
        quickSearchShell.setManaged(true);

        reportIssueTitleLabel.setText("Issues");
        searchIssuesTitleLabel.setText("Search");

        reportIssueButton.setOnAction(this::openIssueBrowser);
        searchIssuesButton.setOnAction(event -> openSearch());

        reportIssueButton.setVisible(true);
        reportIssueButton.setManaged(true);
        searchIssuesButton.setVisible(true);
        searchIssuesButton.setManaged(true);

        analyticsButton.setVisible(false);
        analyticsButton.setManaged(false);
        adminButton.setVisible(false);
        adminButton.setManaged(false);

        featureRow.getChildren().setAll(reportIssueButton, searchIssuesButton);
    }

    private void configureTesterLayout() {
        quickSearchShell.setVisible(true);
        quickSearchShell.setManaged(true);

        reportIssueTitleLabel.setText("Report Issue");
        searchIssuesTitleLabel.setText("Search");

        reportIssueButton.setOnAction(event -> openIssueRegistration());
        searchIssuesButton.setOnAction(event -> openSearch());

        reportIssueButton.setVisible(true);
        reportIssueButton.setManaged(true);
        searchIssuesButton.setVisible(true);
        searchIssuesButton.setManaged(true);

        analyticsButton.setVisible(false);
        analyticsButton.setManaged(false);
        adminButton.setVisible(false);
        adminButton.setManaged(false);

        featureRow.getChildren().setAll(reportIssueButton, searchIssuesButton);
    }

    private void configureProjectLeadLayout() {
        quickSearchShell.setVisible(true);
        quickSearchShell.setManaged(true);

        reportIssueTitleLabel.setText("Report Issue");
        searchIssuesTitleLabel.setText("Search");
        analyticsTitleLabel.setText("Analytics");

        reportIssueButton.setOnAction(event -> openIssueRegistration());
        searchIssuesButton.setOnAction(event -> openSearch());
        analyticsButton.setOnAction(event -> openAnalytics());

        reportIssueButton.setVisible(true);
        reportIssueButton.setManaged(true);
        searchIssuesButton.setVisible(true);
        searchIssuesButton.setManaged(true);
        analyticsButton.setVisible(true);
        analyticsButton.setManaged(true);

        adminButton.setVisible(false);
        adminButton.setManaged(false);

        featureRow.getChildren().setAll(reportIssueButton, searchIssuesButton, analyticsButton);
    }

    private void openIssueBrowser(ActionEvent event) {
        if (mainLayoutController != null) {
            mainLayoutController.showIssues(null);
        }
    }

    private void openProjectManagementHome(ActionEvent event) {
        if (mainLayoutController != null) {
            mainLayoutController.showAdminProjectTab();
        }
    }
}
