package com.example.its.ui.javafx.controller;

import com.example.its.ItsApplication;
import com.example.its.shared.dto.account.AccountCreateRequest;
import com.example.its.shared.dto.account.AccountResponse;
import com.example.its.shared.dto.project.ProjectCreateRequest;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.ui.javafx.model.AdminProjectRowModel;
import com.example.its.ui.javafx.model.AdminUserRowModel;
import com.example.its.ui.javafx.model.AuthenticatedUser;
import com.example.its.ui.javafx.model.ProjectTagOption;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminController {

    private final ObservableList<AdminUserRowModel> users = FXCollections.observableArrayList();
    private final ObservableList<AdminProjectRowModel> projects = FXCollections.observableArrayList();
    private final ObservableList<AdminUserRowModel> assignableProjectMembers = FXCollections.observableArrayList();
    private final Set<Long> selectedProjectMemberIds = new LinkedHashSet<>();

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
    private TextField defaultTagField;

    @FXML
    private TextArea tagDescriptionArea;

    @FXML
    private Button assignMembersButton;

    @FXML
    private Label projectMembersSummaryLabel;

    @FXML
    private Label projectFormFeedbackLabel;

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

        userTable.setItems(users);
        projectTable.setItems(projects);
        refreshProjectMemberSummary();
        adminTabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> updateAdminPageCopy());
        updateAdminPageCopy();

        boolean isAdmin = currentUser != null && currentUser.role() == UiRole.ADMIN;
        permissionNoticeLabel.setVisible(!isAdmin);
        permissionNoticeLabel.setManaged(!isAdmin);

        if (!isAdmin) {
            permissionNoticeLabel.setText("Only ADMIN should create accounts. This page stays visible for UI review, but keep it read-only in the real integration.");
            disableForms();
        } else {
            permissionNoticeLabel.setText("");
        }

        loadAdminData();
    }

    @FXML
    private void handleActiveToggle() {
    }

    @FXML
    private void openHome() {
        if (mainLayoutController != null) {
            mainLayoutController.navigateHome();
        }
    }

    @FXML
    private void openNewIssue() {
        if (mainLayoutController != null) {
            mainLayoutController.showCreateIssue(null);
        }
    }

    @FXML
    private void openBrowse() {
        if (mainLayoutController != null) {
            mainLayoutController.showIssues(null);
        }
    }

    @FXML
    private void openSearch() {
        if (mainLayoutController != null) {
            mainLayoutController.showSearch(null);
        }
    }

    @FXML
    private void openProjectManagement() {
        selectProjectTab();
    }

    @FXML
    private void openAccountManagement() {
        selectUserTab();
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
        String defaultTag = trimmed(defaultTagField.getText());
        String tagDescription = trimmed(tagDescriptionArea.getText());
        List<Long> selectedMemberIds = getCheckedProjectMemberIds();

        if (projectName.isBlank() || description.isBlank() || version.isBlank() || defaultTag.isBlank()) {
            projectFormFeedbackLabel.getStyleClass().add("form-feedback-error");
            projectFormFeedbackLabel.setText("Project name, description, version, and at least one default tag are required.");
            return;
        }
        if (selectedMemberIds.isEmpty()) {
            projectFormFeedbackLabel.getStyleClass().add("form-feedback-error");
            projectFormFeedbackLabel.setText("Assign at least one PL, DEV, or TESTER account to the project.");
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
            request.setTagNames(List.of(defaultTag));

            ProjectResponse createdProject = backendBridge().createProject(request);
            backendBridge().assignProjectMembers(createdProject.getProjectId(), selectedMemberIds);
            ProjectTagOption createdTag = backendBridge().ensureProjectTag(
                createdProject.getProjectId(),
                defaultTag,
                tagDescription
            );

            upsertProjectRow(new AdminProjectRowModel(
                createdProject.getProjectId(),
                createdProject.getName(),
                createdProject.getDescription(),
                version,
                true,
                createdTag.name(),
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
                "Project '" + createdProject.getName() + "' was created, members were assigned, and default tag '" + createdTag.name() + "' was registered."
            );
            UiAlertHelper.showInfo(
                "Project Created",
                "Project registration completed.",
                "Project '" + createdProject.getName() + "' was created, members were assigned, and the default tag was registered."
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
        defaultTagField.clear();
        tagDescriptionArea.clear();
        clearProjectMemberSelections();
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

    @FXML
    private void openProjectMemberPicker() {
        if (assignableProjectMembers.isEmpty()) {
            UiAlertHelper.showInfo(
                "No Members Available",
                "There are no assignable members yet.",
                "Create PL, DEV, or TESTER accounts first, then open the member selector again."
            );
            return;
        }

        Stage pickerStage = new Stage();
        pickerStage.initModality(Modality.WINDOW_MODAL);
        if (ItsApplication.getPrimaryStage() != null) {
            pickerStage.initOwner(ItsApplication.getPrimaryStage());
        }
        pickerStage.setTitle("Assign Project Members");
        pickerStage.setMinWidth(520);
        pickerStage.setMinHeight(480);

        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("admin-member-picker-shell");

        Label titleLabel = new Label("Assign Project Members");
        titleLabel.getStyleClass().add("section-title");

        Label subtitleLabel = new Label("Choose the PL, DEV, and TESTER accounts that should belong to this project.");
        subtitleLabel.setWrapText(true);
        subtitleLabel.getStyleClass().add("landing-copy");

        List<CheckBox> memberCheckBoxes = new ArrayList<>();
        VBox checkboxContainer = new VBox(14);
        checkboxContainer.getChildren().addAll(
            buildMemberRoleSection("PL Members", "Select one or more project leads.", UiRole.PL, memberCheckBoxes),
            buildMemberRoleSection("DEV Members", "Select one or more developers who can be assigned to issues in this project.", UiRole.DEV, memberCheckBoxes),
            buildMemberRoleSection("TESTER Members", "Select one or more testers who can verify issue fixes in this project.", UiRole.TESTER, memberCheckBoxes)
        );

        ScrollPane scrollPane = new ScrollPane(checkboxContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(320);
        scrollPane.getStyleClass().add("admin-member-picker-scroll");

        Label footerLabel = new Label();
        footerLabel.getStyleClass().add("landing-copy");
        footerLabel.setWrapText(true);
        refreshMemberPickerFooter(memberCheckBoxes, footerLabel);
        for (CheckBox checkBox : memberCheckBoxes) {
            checkBox.selectedProperty().addListener((obs, oldValue, newValue) ->
                refreshMemberPickerFooter(memberCheckBoxes, footerLabel)
            );
        }

        Button clearButton = new Button("Clear");
        clearButton.getStyleClass().add("ghost-button");
        clearButton.setOnAction(event -> memberCheckBoxes.forEach(memberCheckBox -> memberCheckBox.setSelected(false)));

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("ghost-button");
        cancelButton.setOnAction(event -> pickerStage.close());

        Button applyButton = new Button("Apply Members");
        applyButton.getStyleClass().add("primary-button");
        applyButton.setOnAction(event -> {
            selectedProjectMemberIds.clear();
            for (CheckBox memberCheckBox : memberCheckBoxes) {
                if (memberCheckBox.isSelected()) {
                    AdminUserRowModel user = (AdminUserRowModel) memberCheckBox.getUserData();
                    selectedProjectMemberIds.add(user.getAccountId());
                }
            }
            refreshProjectMemberSummary();
            pickerStage.close();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionBar = new HBox(10, clearButton, spacer, cancelButton, applyButton);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(titleLabel, subtitleLabel, scrollPane, footerLabel, actionBar);

        Scene scene = new Scene(root, 520, 480);
        scene.getStylesheets().add(ItsApplication.class.getResource("/styles/app.css").toExternalForm());
        pickerStage.setScene(scene);
        pickerStage.showAndWait();
    }

    private void updateMemberPickerFooter(List<CheckBox> memberCheckBoxes, Label footerLabel) {
        long selectedCount = memberCheckBoxes.stream().filter(CheckBox::isSelected).count();
        if (selectedCount == 0) {
            footerLabel.setText("No members selected yet.");
            return;
        }
        long plCount = countSelectedInControls(memberCheckBoxes, UiRole.PL);
        long devCount = countSelectedInControls(memberCheckBoxes, UiRole.DEV);
        long testerCount = countSelectedInControls(memberCheckBoxes, UiRole.TESTER);
        footerLabel.setText(
            selectedCount + " member(s) selected. "
                + "PL " + plCount + " · DEV " + devCount + " · TESTER " + testerCount
                + ". Each project must have exactly one DEV and one TESTER."
        );
    }

    private void updateProjectMemberSummary() {
        if (projectMembersSummaryLabel == null) {
            return;
        }

        if (selectedProjectMemberIds.isEmpty()) {
            projectMembersSummaryLabel.setText("No members selected yet.");
            return;
        }

        long plCount = countSelectedByRole(UiRole.PL);
        long devCount = countSelectedByRole(UiRole.DEV);
        long testerCount = countSelectedByRole(UiRole.TESTER);
        projectMembersSummaryLabel.setText(
            selectedProjectMemberIds.size() + " member(s) selected · "
                + "PL " + plCount + " · DEV " + devCount + " · TESTER " + testerCount
        );
    }

    private void refreshMemberPickerFooter(List<CheckBox> memberCheckBoxes, Label footerLabel) {
        long selectedCount = memberCheckBoxes.stream().filter(CheckBox::isSelected).count();
        if (selectedCount == 0) {
            footerLabel.setText("No members selected yet.");
            return;
        }

        long plCount = countSelectedInControls(memberCheckBoxes, UiRole.PL);
        long devCount = countSelectedInControls(memberCheckBoxes, UiRole.DEV);
        long testerCount = countSelectedInControls(memberCheckBoxes, UiRole.TESTER);
        footerLabel.setText(
            selectedCount + " member(s) selected. "
                + "PL " + plCount + " | DEV " + devCount + " | TESTER " + testerCount
        );
    }

    private void refreshProjectMemberSummary() {
        if (projectMembersSummaryLabel == null) {
            return;
        }

        if (selectedProjectMemberIds.isEmpty()) {
            projectMembersSummaryLabel.setText("No members selected yet.");
            return;
        }

        long plCount = countSelectedByRole(UiRole.PL);
        long devCount = countSelectedByRole(UiRole.DEV);
        long testerCount = countSelectedByRole(UiRole.TESTER);
        projectMembersSummaryLabel.setText(
            selectedProjectMemberIds.size() + " member(s) selected | "
                + "PL " + plCount + " | DEV " + devCount + " | TESTER " + testerCount
        );
    }

    private VBox buildMemberRoleSection(
        String title,
        String description,
        UiRole role,
        List<CheckBox> memberCheckBoxes
    ) {
        VBox section = new VBox(8);
        section.getStyleClass().add("admin-member-picker-role-shell");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("landing-copy");
        descriptionLabel.setWrapText(true);

        VBox roleList = new VBox(8);
        roleList.getStyleClass().add("admin-member-picker-list");

        List<AdminUserRowModel> members = assignableProjectMembers.stream()
            .filter(user -> user.getRole() == role)
            .sorted(Comparator.comparing(AdminUserRowModel::getRealName, String.CASE_INSENSITIVE_ORDER))
            .toList();

        if (members.isEmpty()) {
            Label emptyLabel = new Label("No " + role.name() + " accounts are available yet.");
            emptyLabel.getStyleClass().add("landing-copy");
            roleList.getChildren().add(emptyLabel);
        } else {
            for (AdminUserRowModel user : members) {
                CheckBox checkBox = new CheckBox(user.getRealName() + " (" + user.getLoginId() + ")");
                checkBox.getStyleClass().add("admin-member-picker-check");
                checkBox.setSelected(selectedProjectMemberIds.contains(user.getAccountId()));
                checkBox.setUserData(user);
                memberCheckBoxes.add(checkBox);
                roleList.getChildren().add(checkBox);
            }
        }

        section.getChildren().addAll(titleLabel, descriptionLabel, roleList);
        return section;
    }

    private long countSelectedByRole(UiRole role) {
        return assignableProjectMembers.stream()
            .filter(user -> user.getRole() == role)
            .filter(user -> selectedProjectMemberIds.contains(user.getAccountId()))
            .count();
    }

    private long countSelectedInControls(List<CheckBox> checkBoxes, UiRole role) {
        return checkBoxes.stream()
            .filter(CheckBox::isSelected)
            .map(checkBox -> (AdminUserRowModel) checkBox.getUserData())
            .filter(user -> user.getRole() == role)
            .count();
    }

    private long countSelectedMembersByRole(List<Long> selectedMemberIds, UiRole role) {
        return assignableProjectMembers.stream()
            .filter(user -> user.getRole() == role)
            .filter(user -> selectedMemberIds.contains(user.getAccountId()))
            .count();
    }

    private void updateAdminPageCopy() {
        if (adminTabPane.getSelectionModel().getSelectedIndex() == 1) {
            adminPageTitleLabel.setText("Project Management");
            return;
        }
        adminPageTitleLabel.setText("Account Management");
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
        defaultTagField.setDisable(true);
        tagDescriptionArea.setDisable(true);
        assignMembersButton.setDisable(true);
    }

    private String trimmed(String value) {
        return value == null ? "" : value.trim();
    }

    private void refreshUsersFromBackend() {
        List<AdminUserRowModel> activeUsers = backendBridge().getActiveAccounts().stream()
            .map(UiModelMapper::toAdminUserRowModel)
            .toList();
        users.setAll(activeUsers);
        assignableProjectMembers.setAll(
            activeUsers.stream()
                .filter(user -> user.getRole() != UiRole.ADMIN)
                .toList()
        );

        Set<Long> validIds = assignableProjectMembers.stream()
            .map(AdminUserRowModel::getAccountId)
            .collect(Collectors.toSet());
        selectedProjectMemberIds.retainAll(validIds);
        refreshProjectMemberSummary();
    }

    private void refreshProjectsFromBackend() {
        projects.setAll(
            backendBridge().getProjects().stream()
                .map(UiModelMapper::toAdminProjectRowModel)
                .toList()
        );
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

    private List<Long> getCheckedProjectMemberIds() {
        return new ArrayList<>(selectedProjectMemberIds);
    }

    private void clearProjectMemberSelections() {
        selectedProjectMemberIds.clear();
        refreshProjectMemberSummary();
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

    private JavaFxBackendBridge backendBridge() {
        return JavaFxBackendBridge.getInstance();
    }
}
