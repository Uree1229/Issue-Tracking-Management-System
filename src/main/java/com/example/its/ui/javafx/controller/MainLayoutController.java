package com.example.its.ui.javafx.controller;

import com.example.its.ItsApplication;
import com.example.its.ui.javafx.model.AuthenticatedUser;
import com.example.its.ui.javafx.model.UiRole;
import com.example.its.ui.javafx.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainLayoutController {

    @FXML
    private Label currentProjectLabel;

    @FXML
    private Label currentUserLabel;

    @FXML
    private Label currentRoleLabel;

    @FXML
    private StackPane contentContainer;

    @FXML
    private Button homeNavButton;

    @FXML
    private Button issuesNavButton;

    @FXML
    private Button searchNavButton;

    @FXML
    private Button inquiryNavButton;

    @FXML
    private Button createIssueNavButton;

    @FXML
    private Button analyticsNavButton;

    @FXML
    private Button adminNavButton;

    private Node homeView;
    private Node issueBrowserView;
    private Node searchView;
    private Node inquiryView;
    private Node createIssueView;
    private Node analyticsView;
    private Node adminView;
    private IssueBrowserController issueBrowserController;
    private SearchController searchController;
    private InquiryController inquiryController;
    private CreateIssueController createIssueController;
    private AnalyticsController analyticsController;
    private AdminController adminController;

    @FXML
    private void initialize() {
        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        refreshCurrentContext();
        boolean isAdmin = currentUser != null && currentUser.role() == UiRole.ADMIN;
        boolean canCreateIssue = currentUser != null && currentUser.role().canCreateIssue();
        boolean canViewAnalytics = currentUser != null && currentUser.role().canViewAnalytics();

        adminNavButton.setVisible(isAdmin);
        adminNavButton.setManaged(isAdmin);
        createIssueNavButton.setVisible(canCreateIssue);
        createIssueNavButton.setManaged(canCreateIssue);
        analyticsNavButton.setVisible(canViewAnalytics);
        analyticsNavButton.setManaged(canViewAnalytics);
        loadHomeView();
        showHome(null);
    }

    public void refreshCurrentContext() {
        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        String projectName = UserSession.getCurrentProjectName();
        currentProjectLabel.setText(projectName == null || projectName.isBlank() ? "No Project" : projectName);
        currentUserLabel.setText(currentUser == null ? "Guest" : currentUser.displayName());
        currentRoleLabel.setText(currentUser == null ? "UNAUTHENTICATED" : currentUser.role().name());
    }

    @FXML
    private void showHome(ActionEvent event) {
        activateNav(homeNavButton);
        contentContainer.getChildren().setAll(homeView);
    }

    @FXML
    public void showIssues(ActionEvent event) {
        showIssueBrowser();
    }

    @FXML
    public void showSearch(ActionEvent event) {
        activateNav(searchNavButton);
        if (searchView == null) {
            loadSearchView();
        }
        contentContainer.getChildren().setAll(searchView);
        if (searchController != null) {
            searchController.refreshData();
        }
    }

    @FXML
    public void showInquiry(ActionEvent event) {
        activateNav(inquiryNavButton);
        if (inquiryView == null) {
            loadInquiryView();
        }
        contentContainer.getChildren().setAll(inquiryView);
        if (inquiryController != null) {
            inquiryController.refreshData();
        }
    }

    @FXML
    public void showCreateIssue(ActionEvent event) {
        activateNav(createIssueNavButton);
        if (createIssueView == null) {
            loadCreateIssueView();
        }
        contentContainer.getChildren().setAll(createIssueView);
        if (createIssueController != null) {
            createIssueController.refreshContext();
        }
    }

    @FXML
    public void showAnalytics(ActionEvent event) {
        activateNav(analyticsNavButton);
        if (analyticsView == null) {
            loadAnalyticsView();
        }
        contentContainer.getChildren().setAll(analyticsView);
        if (analyticsController != null) {
            analyticsController.refreshMetrics();
        }
    }

    @FXML
    public void showAdmin(ActionEvent event) {
        activateNav(adminNavButton);
        if (adminView == null) {
            loadAdminView();
        }
        contentContainer.getChildren().setAll(adminView);
        if (adminController != null) {
            adminController.selectUserTab();
        }
    }

    public void showAdminProjectTab() {
        activateNav(adminNavButton);
        if (adminView == null) {
            loadAdminView();
        }
        contentContainer.getChildren().setAll(adminView);
        if (adminController != null) {
            adminController.selectProjectTab();
        }
    }

    @FXML
    private void handleLogout() {
        UserSession.clear();
        ItsApplication.showLoginView();
    }

    public void showIssuesWithKeyword(String keyword) {
        showIssueBrowser();
        issueBrowserController.applyKeywordSearch(keyword);
    }

    public void showSearchWithKeyword(String keyword) {
        activateNav(searchNavButton);
        if (searchView == null) {
            loadSearchView();
        }
        contentContainer.getChildren().setAll(searchView);
        searchController.applyKeywordSearch(keyword);
    }

    public void showNewIssues() {
        showIssueBrowser();
        issueBrowserController.showNewIssues();
    }

    public void showAssignedToCurrentUser() {
        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            showIssueBrowser();
            return;
        }

        showIssueBrowser();
        issueBrowserController.showAssignedTo(currentUser.name());
    }

    public void showReportedByCurrentUser() {
        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            showIssueBrowser();
            return;
        }

        showIssueBrowser();
        issueBrowserController.showReportedBy(currentUser.name());
    }

    public void showFixedIssues() {
        showIssueBrowser();
        issueBrowserController.showFixedIssues();
    }

    public void showIssue(Long issueId) {
        showIssueBrowser();
        issueBrowserController.showIssueById(issueId);
    }

    public void showInquiry(Long issueId) {
        activateNav(inquiryNavButton);
        if (inquiryView == null) {
            loadInquiryView();
        }
        contentContainer.getChildren().setAll(inquiryView);
        inquiryController.showIssue(issueId);
    }

    private void showIssueBrowser() {
        activateNav(issuesNavButton);
        if (issueBrowserView == null) {
            loadIssueBrowserView();
        }
        contentContainer.getChildren().setAll(issueBrowserView);
        issueBrowserController.refreshData();
    }

    private void loadHomeView() {
        try {
            FXMLLoader loader = new FXMLLoader(ItsApplication.class.getResource("/fxml/home-view.fxml"));
            Node view = loader.load();
            HomeController controller = loader.getController();
            controller.setMainLayoutController(this);
            homeView = view;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load home view.", exception);
        }
    }

    private void loadIssueBrowserView() {
        try {
            FXMLLoader loader = new FXMLLoader(ItsApplication.class.getResource("/fxml/issue-browser.fxml"));
            Node view = loader.load();
            issueBrowserController = loader.getController();
            issueBrowserView = view;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load issue browser view.", exception);
        }
    }

    private void loadSearchView() {
        try {
            FXMLLoader loader = new FXMLLoader(ItsApplication.class.getResource("/fxml/search-view.fxml"));
            searchView = loader.load();
            searchController = loader.getController();
            searchController.setMainLayoutController(this);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load search view.", exception);
        }
    }

    private void loadInquiryView() {
        try {
            FXMLLoader loader = new FXMLLoader(ItsApplication.class.getResource("/fxml/inquiry-view.fxml"));
            inquiryView = loader.load();
            inquiryController = loader.getController();
            inquiryController.setMainLayoutController(this);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load inquiry view.", exception);
        }
    }

    private void loadCreateIssueView() {
        try {
            FXMLLoader loader = new FXMLLoader(ItsApplication.class.getResource("/fxml/create-issue-view.fxml"));
            createIssueView = loader.load();
            createIssueController = loader.getController();
            createIssueController.setMainLayoutController(this);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load create issue view.", exception);
        }
    }

    private void loadAnalyticsView() {
        try {
            FXMLLoader loader = new FXMLLoader(ItsApplication.class.getResource("/fxml/analytics-view.fxml"));
            analyticsView = loader.load();
            analyticsController = loader.getController();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load analytics view.", exception);
        }
    }

    private void loadAdminView() {
        try {
            FXMLLoader loader = new FXMLLoader(ItsApplication.class.getResource("/fxml/admin-view.fxml"));
            adminView = loader.load();
            adminController = loader.getController();
            adminController.setMainLayoutController(this);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load admin view.", exception);
        }
    }

    private void activateNav(Button activeButton) {
        Button[] buttons = {homeNavButton, issuesNavButton, searchNavButton, inquiryNavButton, createIssueNavButton, analyticsNavButton, adminNavButton};
        for (Button button : buttons) {
            button.getStyleClass().remove("active");
        }
        if (!activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }

}
