package com.example.its.ui.javafx.controller;

import com.example.its.ItsApplication;
import com.example.its.ui.javafx.model.AuthenticatedUser;
import com.example.its.ui.javafx.model.InquiryQueryPayload;
import com.example.its.ui.javafx.model.SearchQueryPayload;
import com.example.its.ui.javafx.model.UiRole;
import com.example.its.ui.javafx.session.UserSession;
import com.example.its.ui.javafx.support.UiAlertHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

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
    private Stage searchResultsStage;
    private Stage inquiryResultsStage;

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

    public void navigateHome() {
        showHome(null);
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
        if (searchView == null) {
            return;
        }
        contentContainer.getChildren().setAll(searchView);
        if (searchController != null) {
            searchController.refreshData();
        }
    }

    public void showSearchResults(SearchQueryPayload payload) {
        showSearchResultsWindow(payload);
    }

    @FXML
    public void showInquiry(ActionEvent event) {
        activateNav(inquiryNavButton);
        if (inquiryView == null) {
            loadInquiryView();
        }
        if (inquiryView == null) {
            return;
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
        if (createIssueView == null) {
            return;
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
        if (analyticsView == null) {
            return;
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
        if (adminView == null) {
            return;
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
        if (adminView == null) {
            return;
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
        showSearchResultsWindow(SearchQueryPayload.keywordOnly(keyword));
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
        showInquiryResultsWindow(new InquiryQueryPayload(issueId, "", false));
    }

    private void showIssueBrowser() {
        activateNav(issuesNavButton);
        if (issueBrowserView == null) {
            loadIssueBrowserView();
        }
        if (issueBrowserView == null) {
            return;
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
        } catch (Exception exception) {
            UiAlertHelper.showError("View Load Failed", "Home view could not be opened.", exception);
        }
    }

    private void loadIssueBrowserView() {
        try {
            FXMLLoader loader = new FXMLLoader(ItsApplication.class.getResource("/fxml/issue-browser.fxml"));
            Node view = loader.load();
            issueBrowserController = loader.getController();
            issueBrowserView = view;
        } catch (Exception exception) {
            UiAlertHelper.showError("View Load Failed", "Issues view could not be opened.", exception);
        }
    }

    private void loadSearchView() {
        try {
            FXMLLoader loader = new FXMLLoader(ItsApplication.class.getResource("/fxml/search-view.fxml"));
            searchView = loader.load();
            searchController = loader.getController();
            searchController.setMainLayoutController(this);
        } catch (Exception exception) {
            UiAlertHelper.showError("View Load Failed", "Search view could not be opened.", exception);
        }
    }

    public void showSearchResultsWindow(SearchQueryPayload payload) {
        try {
            if (searchResultsStage != null) {
                searchResultsStage.close();
            }

            FXMLLoader loader = new FXMLLoader(ItsApplication.class.getResource("/fxml/search-results-view.fxml"));
            Parent root = loader.load();
            SearchResultsController controller = loader.getController();
            controller.setMainLayoutController(this);

            Scene scene = new Scene(root, 1120, 720);
            scene.getStylesheets().add(ItsApplication.class.getResource("/styles/app.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Search Results");
            stage.setMinWidth(980);
            stage.setMinHeight(620);
            if (ItsApplication.getPrimaryStage() != null) {
                stage.initOwner(ItsApplication.getPrimaryStage());
            }
            stage.setScene(scene);
            controller.setWindowStage(stage);
            controller.applySearch(payload);
            stage.show();
            stage.toFront();
            searchResultsStage = stage;
        } catch (Exception exception) {
            UiAlertHelper.showError("View Load Failed", "Search results window could not be opened.", exception);
        }
    }

    public void showInquiryResultsWindow(InquiryQueryPayload payload) {
        try {
            if (inquiryResultsStage != null) {
                inquiryResultsStage.close();
            }

            FXMLLoader loader = new FXMLLoader(ItsApplication.class.getResource("/fxml/inquiry-results-view.fxml"));
            Parent root = loader.load();
            InquiryResultsController controller = loader.getController();
            controller.setMainLayoutController(this);

            Scene scene = new Scene(root, 1180, 760);
            scene.getStylesheets().add(ItsApplication.class.getResource("/styles/app.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Inquiry Results");
            stage.setMinWidth(1040);
            stage.setMinHeight(660);
            if (ItsApplication.getPrimaryStage() != null) {
                stage.initOwner(ItsApplication.getPrimaryStage());
            }
            stage.setScene(scene);
            controller.setWindowStage(stage);
            controller.applyQuery(payload);
            stage.show();
            stage.toFront();
            inquiryResultsStage = stage;
        } catch (Exception exception) {
            UiAlertHelper.showError("View Load Failed", "Inquiry results window could not be opened.", exception);
        }
    }

    private void loadSearchResultsView() {
        try {
            FXMLLoader loader = new FXMLLoader(ItsApplication.class.getResource("/fxml/search-results-view.fxml"));
            loader.load();
        } catch (Exception exception) {
            UiAlertHelper.showError("View Load Failed", "Search results view could not be opened.", exception);
        }
    }

    private void loadInquiryView() {
        try {
            FXMLLoader loader = new FXMLLoader(ItsApplication.class.getResource("/fxml/inquiry-view.fxml"));
            inquiryView = loader.load();
            inquiryController = loader.getController();
            inquiryController.setMainLayoutController(this);
        } catch (Exception exception) {
            UiAlertHelper.showError("View Load Failed", "Inquiry view could not be opened.", exception);
        }
    }

    private void loadCreateIssueView() {
        try {
            FXMLLoader loader = new FXMLLoader(ItsApplication.class.getResource("/fxml/create-issue-view.fxml"));
            createIssueView = loader.load();
            createIssueController = loader.getController();
            createIssueController.setMainLayoutController(this);
        } catch (Exception exception) {
            UiAlertHelper.showError("View Load Failed", "Create Issue view could not be opened.", exception);
        }
    }

    private void loadAnalyticsView() {
        try {
            FXMLLoader loader = new FXMLLoader(ItsApplication.class.getResource("/fxml/analytics-view.fxml"));
            analyticsView = loader.load();
            analyticsController = loader.getController();
        } catch (Exception exception) {
            UiAlertHelper.showError("View Load Failed", "Analytics view could not be opened.", exception);
        }
    }

    private void loadAdminView() {
        try {
            FXMLLoader loader = new FXMLLoader(ItsApplication.class.getResource("/fxml/admin-view.fxml"));
            adminView = loader.load();
            adminController = loader.getController();
            adminController.setMainLayoutController(this);
        } catch (Exception exception) {
            UiAlertHelper.showError("View Load Failed", "Admin view could not be opened.", exception);
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
