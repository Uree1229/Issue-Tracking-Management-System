package com.example.its.ui.javafx.controller;

import com.example.its.ItsApplication;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.ui.javafx.model.AuthenticatedUser;
import com.example.its.ui.javafx.model.InquiryQueryPayload;
import com.example.its.ui.javafx.model.SearchQueryPayload;
import com.example.its.ui.javafx.model.UiRole;
import com.example.its.ui.javafx.service.JavaFxBackendBridge;
import com.example.its.ui.javafx.session.UserSession;
import com.example.its.ui.javafx.support.UiAlertHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.List;

public class MainLayoutController {

    @FXML
    private ComboBox<ProjectResponse> projectSwitcherCombo;

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
    private Button createIssueNavButton;

    @FXML
    private Button analyticsNavButton;

    @FXML
    private Button adminNavButton;

    private Node homeView;
    private Node issueBrowserView;
    private Node searchView;
    private Node createIssueView;
    private Node analyticsView;
    private Node adminView;
    private IssueBrowserController issueBrowserController;
    private SearchController searchController;
    private CreateIssueController createIssueController;
    private AnalyticsController analyticsController;
    private AdminController adminController;
    private Stage searchResultsStage;
    private Stage inquiryResultsStage;
    private boolean suppressProjectSwitch;

    @FXML
    private void initialize() {
        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        configureProjectSwitcher();
        loadAccessibleProjects();
        refreshCurrentContext();
        boolean isAdmin = currentUser != null && currentUser.role() == UiRole.ADMIN;
        UiRole role = currentUser == null ? null : currentUser.role();
        boolean canCreateIssue = role != null && role.canCreateIssue();
        boolean canViewAnalytics = role != null && role.canViewAnalytics();
        boolean canOpenIssues = role != null && role.canOpenIssueBrowser();
        boolean canOpenSearch = role != null && role.canOpenSearch();

        configureSidebarForRole(isAdmin, canOpenIssues, canOpenSearch, canViewAnalytics);
        loadHomeView();
        showHome(null);
    }

    public void refreshCurrentContext() {
        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        currentUserLabel.setText(currentUser == null ? "Guest" : currentUser.displayName());
        currentRoleLabel.setText(currentUser == null ? "UNAUTHENTICATED" : currentUser.role().name());
        syncProjectSwitcherSelection();
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
        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        if (currentUser != null && currentUser.role() == UiRole.ADMIN) {
            showAdminProjectTab();
            activateNav(issuesNavButton);
            return;
        }
        if (currentUser == null || !currentUser.role().canOpenIssueBrowser()) {
            UiAlertHelper.showInfo("Access Restricted", "Issue Browser is not available for this role.", "Sign in as PL, DEV, or TESTER to browse issues.");
            navigateHome();
            return;
        }
        showIssueBrowser();
    }

    @FXML
    public void showSearch(ActionEvent event) {
        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        if (currentUser != null && currentUser.role() == UiRole.ADMIN) {
            showAdmin(null);
            activateNav(searchNavButton);
            return;
        }
        if (currentUser == null || !currentUser.role().canOpenSearch()) {
            UiAlertHelper.showInfo("Access Restricted", "Search is not available for this role.", "Sign in as PL, DEV, or TESTER to use the issue search flow.");
            navigateHome();
            return;
        }
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
        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        if (currentUser == null || !currentUser.role().canOpenSearch()) {
            UiAlertHelper.showInfo("Access Restricted", "Search results are not available for this role.", "Sign in as PL, DEV, or TESTER to use the structured search flow.");
            return;
        }
        showSearchResultsWindow(payload);
    }

    @FXML
    public void showCreateIssue(ActionEvent event) {
        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        if (currentUser == null || !currentUser.role().canCreateIssue()) {
            UiAlertHelper.showInfo("Access Restricted", "Issue registration is not available for this role.", "Only PL and TESTER accounts can create new issues in this UI.");
            navigateHome();
            return;
        }
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
        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        if (currentUser == null || !currentUser.role().canViewAnalytics()) {
            UiAlertHelper.showInfo("Access Restricted", "Analytics are only available for PL accounts.", "Use the admin area for project setup, or sign in as PL to view project metrics.");
            navigateHome();
            return;
        }
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
        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        activateNav(currentUser != null && currentUser.role() == UiRole.ADMIN ? searchNavButton : adminNavButton);
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
        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        activateNav(currentUser != null && currentUser.role() == UiRole.ADMIN ? issuesNavButton : adminNavButton);
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

    @FXML
    private void handleProjectSwitch() {
        if (suppressProjectSwitch) {
            return;
        }

        ProjectResponse selectedProject = projectSwitcherCombo.getValue();
        UserSession.setCurrentProject(
            selectedProject == null ? null : selectedProject.getProjectId(),
            selectedProject == null ? null : selectedProject.getName()
        );
        refreshCurrentContext();
        refreshActiveViewForProjectChange();
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

    public void reloadProjectOptions() {
        loadAccessibleProjects();
        refreshCurrentContext();
    }

    private void loadIssueBrowserView() {
        try {
            FXMLLoader loader = new FXMLLoader(ItsApplication.class.getResource("/fxml/issue-browser.fxml"));
            Node view = loader.load();
            issueBrowserController = loader.getController();
            issueBrowserController.setMainLayoutController(this);
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
            stage.setTitle("Issue Details");
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
            UiAlertHelper.showError("View Load Failed", "Issue details window could not be opened.", exception);
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
        Button[] buttons = {homeNavButton, issuesNavButton, searchNavButton, createIssueNavButton, analyticsNavButton, adminNavButton};
        for (Button button : buttons) {
            button.getStyleClass().remove("active");
        }
        if (!activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }

    private void configureProjectSwitcher() {
        projectSwitcherCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(ProjectResponse project) {
                return project == null ? "No Project" : project.getName();
            }

            @Override
            public ProjectResponse fromString(String string) {
                return null;
            }
        });
        projectSwitcherCombo.setPromptText("No Project");
    }

    private void loadAccessibleProjects() {
        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        List<ProjectResponse> projects = backendBridge().getAccessibleProjects(currentUser);

        suppressProjectSwitch = true;
        projectSwitcherCombo.getItems().setAll(projects);

        ProjectResponse selectedProject = null;
        Long currentProjectId = UserSession.getCurrentProjectId();
        if (currentProjectId != null) {
            selectedProject = projects.stream()
                .filter(project -> currentProjectId.equals(project.getProjectId()))
                .findFirst()
                .orElse(null);
        }
        if (selectedProject == null) {
            selectedProject = backendBridge().findPreferredProject(currentUser).orElse(null);
        }

        projectSwitcherCombo.setDisable(projects.isEmpty());
        projectSwitcherCombo.setValue(selectedProject);
        UserSession.setCurrentProject(
            selectedProject == null ? null : selectedProject.getProjectId(),
            selectedProject == null ? null : selectedProject.getName()
        );
        suppressProjectSwitch = false;
    }

    private void syncProjectSwitcherSelection() {
        Long currentProjectId = UserSession.getCurrentProjectId();
        ProjectResponse selectedProject = projectSwitcherCombo.getItems().stream()
            .filter(project -> currentProjectId != null && currentProjectId.equals(project.getProjectId()))
            .findFirst()
            .orElse(null);

        suppressProjectSwitch = true;
        projectSwitcherCombo.setValue(selectedProject);
        suppressProjectSwitch = false;
    }

    private void refreshActiveViewForProjectChange() {
        if (contentContainer.getChildren().isEmpty()) {
            return;
        }

        Node activeView = contentContainer.getChildren().get(0);
        if (activeView == issueBrowserView && issueBrowserController != null) {
            issueBrowserController.refreshData();
            return;
        }
        if (activeView == searchView && searchController != null) {
            searchController.refreshData();
            return;
        }
        if (activeView == createIssueView && createIssueController != null) {
            createIssueController.refreshContext();
            return;
        }
        if (activeView == analyticsView && analyticsController != null) {
            analyticsController.refreshMetrics();
        }
    }

    private JavaFxBackendBridge backendBridge() {
        return JavaFxBackendBridge.getInstance();
    }

    private void configureSidebarForRole(boolean isAdmin, boolean canOpenIssues, boolean canOpenSearch, boolean canViewAnalytics) {
        if (isAdmin) {
            issuesNavButton.setText("Project");
            searchNavButton.setText("Account Management");
            issuesNavButton.setVisible(true);
            issuesNavButton.setManaged(true);
            searchNavButton.setVisible(true);
            searchNavButton.setManaged(true);
            adminNavButton.setVisible(false);
            adminNavButton.setManaged(false);
            createIssueNavButton.setVisible(false);
            createIssueNavButton.setManaged(false);
            analyticsNavButton.setVisible(false);
            analyticsNavButton.setManaged(false);
            return;
        }

        issuesNavButton.setText("Issues");
        searchNavButton.setText("Search");
        issuesNavButton.setVisible(canOpenIssues);
        issuesNavButton.setManaged(canOpenIssues);
        searchNavButton.setVisible(canOpenSearch);
        searchNavButton.setManaged(canOpenSearch);
        createIssueNavButton.setVisible(false);
        createIssueNavButton.setManaged(false);
        analyticsNavButton.setVisible(canViewAnalytics);
        analyticsNavButton.setManaged(canViewAnalytics);
        adminNavButton.setVisible(false);
        adminNavButton.setManaged(false);
    }

}
