package com.example.its.ui.javafx.controller;

import com.example.its.shared.dto.account.AccountCreateRequest;
import com.example.its.shared.dto.account.AccountResponse;
import com.example.its.shared.dto.project.ProjectCreateRequest;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.shared.dto.tag.TagResponse;
import com.example.its.ui.javafx.model.AdminProjectRowModel;
import com.example.its.ui.javafx.model.AdminUserRowModel;
import com.example.its.ui.javafx.model.AuthenticatedUser;
import com.example.its.ui.javafx.model.UiRole;
import com.example.its.ui.javafx.service.JavaFxBackendBridge;
import com.example.its.ui.javafx.session.UserSession;
import com.example.its.ui.javafx.support.UiAlertHelper;
import com.example.its.ui.javafx.support.UiModelMapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AdminController {

    private final ObservableList<AdminUserRowModel> users = FXCollections.observableArrayList();
    private final ObservableList<AdminProjectRowModel> projects = FXCollections.observableArrayList();
    private final ObservableList<String> draftProjectTags = FXCollections.observableArrayList();

    private MainLayoutController mainLayoutController;

    @FXML
    private Label adminPageTitleLabel;

    @FXML
    private Label permissionNoticeLabel;

    @FXML
    private TabPane adminTabPane;

    @FXML
    private TextField loginIdField;

    @FXML
    private TextField realNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<UiRole> roleCombo;

    @FXML
    private Label formFeedbackLabel;

    @FXML
    private TableView<AdminUserRowModel> userTable;

    @FXML
    private TableColumn<AdminUserRowModel, Number> idColumn;

    @FXML
    private TableColumn<AdminUserRowModel, String> loginIdColumn;

    @FXML
    private TableColumn<AdminUserRowModel, String> realNameColumn;

    @FXML
    private TableColumn<AdminUserRowModel, String> emailColumn;

    @FXML
    private TableColumn<AdminUserRowModel, String> roleColumn;

    @FXML
    private TableColumn<AdminUserRowModel, String> activeColumn;

    @FXML
    private TextField projectNameField;

    @FXML
    private TextArea projectDescriptionArea;

    @FXML
    private TextField versionField;

    @FXML
    private TextField tagNameField;

    @FXML
    private ListView<String> projectTagListView;

    @FXML
    private Label projectFormFeedbackLabel;

    @FXML
    private TextField existingProjectTagField;

    @FXML
    private ListView<String> existingProjectTagsListView;

    @FXML
    private TableView<AdminProjectRowModel> projectTable;

    @FXML
    private TableColumn<AdminProjectRowModel, Number> projectIdColumn;

    @FXML
    private TableColumn<AdminProjectRowModel, String> projectNameColumn;

    @FXML
    private TableColumn<AdminProjectRowModel, String> versionColumn;

    @FXML
    private TableColumn<AdminProjectRowModel, String> entryColumn;

    @FXML
    private TableColumn<AdminProjectRowModel, String> tagColumn;

    @FXML
    private TableColumn<AdminProjectRowModel, String> assigneeColumn;

    @FXML
    private void initialize() {
        AuthenticatedUser currentUser = UserSession.getCurrentUser();

        roleCombo.setItems(FXCollections.observableArrayList(UiRole.values()));
        roleCombo.setValue(UiRole.DEV);
        versionField.setText("1.0.0");

        configureUserTable();
        configureProjectTable();
        projectTagListView.setItems(draftProjectTags);
        existingProjectTagsListView.setItems(FXCollections.observableArrayList());

        userTable.setItems(users);
        projectTable.setItems(projects);
        projectTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> loadSelectedProjectTags(newValue));
        adminTabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> updateAdminPageCopy());
        updateAdminPageCopy();

        boolean isAdmin = currentUser != null && currentUser.role() == UiRole.ADMIN;
        permissionNoticeLabel.setVisible(!isAdmin);
        permissionNoticeLabel.setManaged(!isAdmin);

        if (!isAdmin) {
            permissionNoticeLabel.setText("Only ADMIN should create accounts and projects. This page stays visible for UI review, but keep it read-only in the real integration.");
            disableForms();
        } else {
            permissionNoticeLabel.setText("");
        }

        loadAdminData();
    }

    @FXML
    private void handleAddUser() {
        formFeedbackLabel.getStyleClass().setAll("form-feedback");

        String loginId = trimmed(loginIdField.getText());
        String realName = trimmed(realNameField.getText());
        String email = trimmed(emailField.getText());
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        UiRole role = roleCombo.getValue();

        if (loginId.isBlank() || realName.isBlank() || email.isBlank() || password.isBlank() || role == null) {
            formFeedbackLabel.getStyleClass().add("form-feedback-error");
            formFeedbackLabel.setText("Login ID, real name, email, password, and role are required.");
            return;
        }

        try {
            AccountCreateRequest request = new AccountCreateRequest();
            request.setLoginId(loginId);
            request.setName(realName);
            request.setEmail(email);
            request.setPassword(password);
            request.setRole(UiModelMapper.toBackendRole(role));

            AccountResponse createdAccount = backendBridge().register(request);
            users.add(UiModelMapper.toAdminUserRowModel(createdAccount));
            users.sort(Comparator.comparing(AdminUserRowModel::getRealName, String.CASE_INSENSITIVE_ORDER));
            clearForm();
            refreshUsersFromBackend();
            formFeedbackLabel.getStyleClass().add("form-feedback-success");
            formFeedbackLabel.setText("Account '" + createdAccount.getLoginId() + "' was created successfully.");
        } catch (Exception exception) {
            formFeedbackLabel.getStyleClass().add("form-feedback-error");
            formFeedbackLabel.setText(UiAlertHelper.extractMessage(exception, "Account creation failed."));
        }
    }

    @FXML
    private void handleAddProject() {
        projectFormFeedbackLabel.getStyleClass().setAll("form-feedback");

        String projectName = trimmed(projectNameField.getText());
        String description = trimmed(projectDescriptionArea.getText());
        String version = trimmed(versionField.getText());
        List<String> projectTags = draftProjectTags.stream()
            .map(String::trim)
            .filter(tag -> !tag.isBlank())
            .toList();

        if (projectName.isBlank() || description.isBlank() || version.isBlank()) {
            projectFormFeedbackLabel.getStyleClass().add("form-feedback-error");
            projectFormFeedbackLabel.setText("Project name, description, and version are required.");
            return;
        }
        if (projectTags.isEmpty()) {
            projectFormFeedbackLabel.getStyleClass().add("form-feedback-error");
            projectFormFeedbackLabel.setText("Add at least one project tag before creating the project.");
            return;
        }

        AuthenticatedUser currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            projectFormFeedbackLabel.getStyleClass().add("form-feedback-error");
            projectFormFeedbackLabel.setText("You need to be signed in as ADMIN before creating a project.");
            return;
        }

        try {
            ProjectCreateRequest request = new ProjectCreateRequest();
            request.setName(projectName);
            request.setDescription(description);
            request.setCreatedByAccountId(currentUser.accountId());
            request.setTagNames(List.of());

            ProjectResponse createdProject = backendBridge().createProject(request);
            for (String projectTag : projectTags) {
                createdProject = backendBridge().updateProjectTags(createdProject.getProjectId(), List.of(projectTag), List.of());
            }

            upsertProjectRow(new AdminProjectRowModel(
                createdProject.getProjectId(),
                createdProject.getName(),
                createdProject.getDescription(),
                version,
                true,
                summarizeTags(projectTags),
                currentUser.loginId()
            ));

            try {
                refreshProjectsFromBackend();
            } catch (Exception ignored) {
                projectTable.refresh();
            }

            adminTabPane.getSelectionModel().select(1);

            if (UserSession.getCurrentProjectId() == null) {
                UserSession.setCurrentProject(createdProject.getProjectId(), createdProject.getName());
            }
            if (mainLayoutController != null) {
                mainLayoutController.reloadProjectOptions();
            }

            clearProjectForm();
            projectFormFeedbackLabel.getStyleClass().add("form-feedback-success");
            projectFormFeedbackLabel.setText(
                "Project '" + createdProject.getName() + "' was created and " + projectTags.size() + " tag(s) were saved."
            );
            UiAlertHelper.showInfo(
                "Project Created",
                "Project registration completed.",
                "Project '" + createdProject.getName() + "' was created and its tags are now available for issues."
            );
        } catch (Exception exception) {
            String detailedMessage = UiAlertHelper.extractRootCauseMessage(exception, "Project creation failed.");
            projectFormFeedbackLabel.getStyleClass().add("form-feedback-error");
            projectFormFeedbackLabel.setText(detailedMessage);
            UiAlertHelper.showError("Project Creation Failed", "Could not create the project.", detailedMessage);
        }
    }

    @FXML
    private void clearForm() {
        loginIdField.clear();
        realNameField.clear();
        emailField.clear();
        passwordField.clear();
        roleCombo.setValue(UiRole.DEV);
    }

    @FXML
    private void clearProjectForm() {
        projectNameField.clear();
        projectDescriptionArea.clear();
        versionField.setText("1.0.0");
        tagNameField.clear();
        draftProjectTags.clear();
    }

    private void configureUserTable() {
        idColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getAccountId()));
        loginIdColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getLoginId()));
        realNameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getRealName()));
        emailColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getEmail()));
        roleColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getRole().name()));
        activeColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().isActive() ? "Active" : "Disabled"));
    }

    private void configureProjectTable() {
        projectIdColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getProjectId()));
        projectNameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getName()));
        versionColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getVersion()));
        entryColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().isOpenForIssueEntry() ? "Open" : "Closed"));
        tagColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getDefaultTag()));
        assigneeColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getDefaultAssignee()));
    }

    private void updateAdminPageCopy() {
        if (adminTabPane.getSelectionModel().getSelectedIndex() == 1) {
            adminPageTitleLabel.setText("Project Management");
            return;
        }
        adminPageTitleLabel.setText("Account Management");
    }

    @FXML
    private void handleAddProjectTag() {
        String name = trimmed(tagNameField.getText());

        projectFormFeedbackLabel.getStyleClass().setAll("form-feedback");

        if (name.isBlank()) {
            projectFormFeedbackLabel.getStyleClass().add("form-feedback-error");
            projectFormFeedbackLabel.setText("Enter a tag name before adding it to the project.");
            return;
        }
        boolean duplicate = draftProjectTags.stream()
            .anyMatch(tag -> tag.equalsIgnoreCase(name));
        if (duplicate) {
            projectFormFeedbackLabel.getStyleClass().add("form-feedback-error");
            projectFormFeedbackLabel.setText("The tag '" + name + "' is already in the tag list.");
            return;
        }

        draftProjectTags.add(name);
        draftProjectTags.sort(String.CASE_INSENSITIVE_ORDER);
        tagNameField.clear();
        projectFormFeedbackLabel.setText("");
    }

    @FXML
    private void handleRemoveSelectedProjectTag() {
        int selectedIndex = projectTagListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            draftProjectTags.remove(selectedIndex);
        }
    }

    @FXML
    private void handleAddSelectedProjectTag() {
        AdminProjectRowModel selectedProject = projectTable.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            UiAlertHelper.showInfo("No Project Selected", "Choose a project first.", "Select a project from the table before adding tags.");
            return;
        }

        String newTagName = trimmed(existingProjectTagField.getText());
        if (newTagName.isBlank()) {
            projectFormFeedbackLabel.getStyleClass().setAll("form-feedback", "form-feedback-error");
            projectFormFeedbackLabel.setText("Enter a tag name before adding it to the selected project.");
            return;
        }

        try {
            ProjectResponse project = backendBridge().getProject(selectedProject.getProjectId());
            boolean duplicate = project.getTags() != null && project.getTags().stream()
                .map(TagResponse::getName)
                .filter(name -> name != null && !name.isBlank())
                .anyMatch(name -> name.equalsIgnoreCase(newTagName));
            if (duplicate) {
                projectFormFeedbackLabel.getStyleClass().setAll("form-feedback", "form-feedback-error");
                projectFormFeedbackLabel.setText("The selected project already contains the tag '" + newTagName + "'.");
                return;
            }

            ProjectResponse updatedProject = backendBridge().updateProjectTags(project.getProjectId(), List.of(newTagName), List.of());
            existingProjectTagField.clear();
            refreshProjectRows(updatedProject);
            projectFormFeedbackLabel.getStyleClass().setAll("form-feedback", "form-feedback-success");
            projectFormFeedbackLabel.setText("The tag '" + newTagName + "' was added to project '" + updatedProject.getName() + "'.");
        } catch (Exception exception) {
            projectFormFeedbackLabel.getStyleClass().setAll("form-feedback", "form-feedback-error");
            projectFormFeedbackLabel.setText(UiAlertHelper.extractMessage(exception, "Could not add the tag to the selected project."));
        }
    }

    @FXML
    private void handleDeleteSelectedProjectTag() {
        AdminProjectRowModel selectedProject = projectTable.getSelectionModel().getSelectedItem();
        String selectedTagName = existingProjectTagsListView.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            UiAlertHelper.showInfo("No Project Selected", "Choose a project first.", "Select a project from the table before deleting tags.");
            return;
        }
        if (selectedTagName == null || selectedTagName.isBlank()) {
            UiAlertHelper.showInfo("No Tag Selected", "Choose a tag first.", "Select a tag from the Available Tags list before deleting it.");
            return;
        }

        try {
            ProjectResponse project = backendBridge().getProject(selectedProject.getProjectId());
            List<Long> tagIdsToRemove = project.getTags() == null ? List.of() : project.getTags().stream()
                .filter(tag -> selectedTagName.equalsIgnoreCase(tag.getName()))
                .map(TagResponse::getTagId)
                .toList();

            if (tagIdsToRemove.isEmpty()) {
                projectFormFeedbackLabel.getStyleClass().setAll("form-feedback", "form-feedback-error");
                projectFormFeedbackLabel.setText("The selected tag could not be found in the latest project data.");
                return;
            }

            ProjectResponse updatedProject = backendBridge().updateProjectTags(project.getProjectId(), List.of(), tagIdsToRemove);
            refreshProjectRows(updatedProject);
            projectFormFeedbackLabel.getStyleClass().setAll("form-feedback", "form-feedback-success");
            projectFormFeedbackLabel.setText("The tag '" + selectedTagName + "' was deleted from project '" + updatedProject.getName() + "'.");
        } catch (Exception exception) {
            projectFormFeedbackLabel.getStyleClass().setAll("form-feedback", "form-feedback-error");
            projectFormFeedbackLabel.setText(UiAlertHelper.extractMessage(exception, "Could not delete the selected tag."));
        }
    }

    private void disableForms() {
        loginIdField.setDisable(true);
        realNameField.setDisable(true);
        emailField.setDisable(true);
        passwordField.setDisable(true);
        roleCombo.setDisable(true);
        projectNameField.setDisable(true);
        projectDescriptionArea.setDisable(true);
        versionField.setDisable(true);
        tagNameField.setDisable(true);
        projectTagListView.setDisable(true);
        existingProjectTagField.setDisable(true);
        existingProjectTagsListView.setDisable(true);
    }

    private String trimmed(String value) {
        return value == null ? "" : value.trim();
    }

    private void refreshUsersFromBackend() {
        users.setAll(
            backendBridge().getActiveAccounts().stream()
                .map(UiModelMapper::toAdminUserRowModel)
                .toList()
        );
    }

    private void refreshProjectsFromBackend() {
        projects.setAll(
            backendBridge().getProjects().stream()
                .map(UiModelMapper::toAdminProjectRowModel)
                .toList()
        );
        syncProjectTagSelection();
    }

    private void loadAdminData() {
        StringBuilder warning = new StringBuilder();

        try {
            refreshUsersFromBackend();
        } catch (Exception exception) {
            warning.append("User list could not be loaded yet. Please check the backend query or data configuration.")
                .append('\n');
        }

        try {
            refreshProjectsFromBackend();
        } catch (Exception exception) {
            warning.append("Project list could not be loaded yet. Please check the backend query or data configuration.");
        }

        if (warning.length() > 0) {
            permissionNoticeLabel.setVisible(true);
            permissionNoticeLabel.setManaged(true);
            permissionNoticeLabel.setText(warning.toString().trim());
            return;
        }

        if (permissionNoticeLabel.getText() != null && permissionNoticeLabel.getText().contains("Only ADMIN")) {
            return;
        }

        permissionNoticeLabel.setVisible(false);
        permissionNoticeLabel.setManaged(false);
        permissionNoticeLabel.setText("");
    }

    private void upsertProjectRow(AdminProjectRowModel row) {
        projects.removeIf(existing -> java.util.Objects.equals(existing.getProjectId(), row.getProjectId()));
        projects.add(row);
        projects.sort(Comparator.comparing(AdminProjectRowModel::getName, String.CASE_INSENSITIVE_ORDER));
    }

    private String summarizeTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return "-";
        }
        return tagNames.stream()
            .filter(tagName -> tagName != null && !tagName.isBlank())
            .collect(Collectors.joining(", "));
    }

    private String normalizeTag(String tagName) {
        return trimmed(tagName).toLowerCase(Locale.ROOT);
    }

    private void loadSelectedProjectTags(AdminProjectRowModel selectedProject) {
        if (selectedProject == null) {
            existingProjectTagsListView.getItems().setAll(List.of());
            return;
        }
        try {
            ProjectResponse project = backendBridge().getProject(selectedProject.getProjectId());
            existingProjectTagsListView.getItems().setAll(
                project.getTags() == null
                    ? List.of()
                    : project.getTags().stream()
                        .map(TagResponse::getName)
                        .filter(name -> name != null && !name.isBlank())
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .toList()
            );
        } catch (Exception exception) {
            existingProjectTagsListView.getItems().setAll(List.of());
        }
    }

    private void refreshProjectRows(ProjectResponse updatedProject) {
        Long selectedProjectId = updatedProject.getProjectId();
        upsertProjectRow(UiModelMapper.toAdminProjectRowModel(updatedProject));
        projectTable.refresh();
        AdminProjectRowModel matchingProject = projects.stream()
            .filter(project -> java.util.Objects.equals(project.getProjectId(), selectedProjectId))
            .findFirst()
            .orElse(null);
        projectTable.getSelectionModel().select(matchingProject);
        loadSelectedProjectTags(matchingProject);
    }

    private void syncProjectTagSelection() {
        loadSelectedProjectTags(projectTable.getSelectionModel().getSelectedItem());
    }

    public void selectUserTab() {
        adminTabPane.getSelectionModel().select(0);
    }

    public void selectProjectTab() {
        adminTabPane.getSelectionModel().select(1);
    }

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void refreshData() {
        loadAdminData();
    }

    private JavaFxBackendBridge backendBridge() {
        return JavaFxBackendBridge.getInstance();
    }
}
