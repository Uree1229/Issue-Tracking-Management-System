package com.example.its.ui.javafx.controller;

import com.example.its.ui.javafx.model.AdminProjectRowModel;
import com.example.its.ui.javafx.model.AdminUserRowModel;
import com.example.its.ui.javafx.model.AuthenticatedUser;
import com.example.its.ui.javafx.model.UiRole;
import com.example.its.ui.javafx.service.MockAccountStore;
import com.example.its.ui.javafx.session.UserSession;
import com.example.its.ui.javafx.support.IntegrationPointHelper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AdminController {

    private final ObservableList<AdminUserRowModel> users = FXCollections.observableArrayList();
    private final ObservableList<AdminProjectRowModel> projects = FXCollections.observableArrayList(
        new AdminProjectRowModel(1L, "Simple Login", "Lightweight authentication and issue tracking demo project.", "1.0.0", true, "auth", "dev1"),
        new AdminProjectRowModel(2L, "Project1", "Primary seeded project used in the software engineering term demo.", "1.2.0", true, "workflow", "dev2")
    );

    @FXML
    private Label adminPageTitleLabel;

    @FXML
    private Label adminSubtitleLabel;

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
    private CheckBox activeCheckBox;

    @FXML
    private TextArea disableReasonArea;

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
    private CheckBox openForIssueEntryCheckBox;

    @FXML
    private CheckBox analyticsDatasetCheckBox;

    @FXML
    private TextField versionField;

    @FXML
    private TextField defaultTagField;

    @FXML
    private TextArea tagDescriptionArea;

    @FXML
    private TextField defaultAssigneeField;

    @FXML
    private TextField defaultCcField;

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
        adminPageTitleLabel.setText("Bugzilla-inspired Add User");
        adminSubtitleLabel.setText("Create ITS accounts for admin, PL, dev, and tester roles.");

        roleCombo.setItems(FXCollections.observableArrayList(UiRole.values()));
        roleCombo.setValue(UiRole.DEV);
        activeCheckBox.setSelected(true);
        openForIssueEntryCheckBox.setSelected(true);
        analyticsDatasetCheckBox.setSelected(true);
        versionField.setText("1.0.0");
        configureUserTable();
        configureProjectTable();
        refreshUsersFromStore();
        userTable.setItems(users);
        projectTable.setItems(projects);
        updateDisableReasonState();

        boolean isAdmin = currentUser != null && currentUser.role() == UiRole.ADMIN;
        permissionNoticeLabel.setVisible(!isAdmin);
        permissionNoticeLabel.setManaged(!isAdmin);

        if (!isAdmin) {
            permissionNoticeLabel.setText("Only ADMIN should create accounts. This page stays visible for UI review, but keep it read-only in the real integration.");
            disableForms();
        } else {
            permissionNoticeLabel.setText("");
        }
    }

    @FXML
    private void handleActiveToggle() {
        updateDisableReasonState();
    }

    @FXML
    private void handleAddUser() {
        formFeedbackLabel.getStyleClass().setAll("form-feedback");

        String loginId = trimmed(loginIdField.getText());
        String realName = trimmed(realNameField.getText());
        String email = trimmed(emailField.getText());
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        UiRole role = roleCombo.getValue();
        boolean isActive = activeCheckBox.isSelected();
        String disableReason = trimmed(disableReasonArea.getText());

        if (loginId.isBlank() || realName.isBlank() || email.isBlank() || password.isBlank() || role == null) {
            formFeedbackLabel.getStyleClass().add("form-feedback-error");
            formFeedbackLabel.setText("Login ID, real name, email, password, and role are required.");
            return;
        }

        if (!isActive && disableReason.isBlank()) {
            formFeedbackLabel.getStyleClass().add("form-feedback-error");
            formFeedbackLabel.setText("If the account starts disabled, add a short reason.");
            return;
        }

        IntegrationPointHelper.showPending(
            "Account creation backend pending",
            "Connect AdminController.handleAddUser() to AccountFacade.createAccount(AccountCreateRequest). "
                + "This page stays as a JavaFX form preview for now."
        );
        formFeedbackLabel.getStyleClass().add("form-feedback-success");
        formFeedbackLabel.setText("Preview only. Connect this form to AccountFacade.createAccount(...) later.");
    }

    @FXML
    private void handleAddProject() {
        projectFormFeedbackLabel.getStyleClass().setAll("form-feedback");

        String projectName = trimmed(projectNameField.getText());
        String description = trimmed(projectDescriptionArea.getText());
        String version = trimmed(versionField.getText());
        String defaultTag = trimmed(defaultTagField.getText());
        String tagDescription = trimmed(tagDescriptionArea.getText());
        String defaultAssignee = trimmed(defaultAssigneeField.getText());

        if (projectName.isBlank() || description.isBlank() || version.isBlank() || defaultTag.isBlank()) {
            projectFormFeedbackLabel.getStyleClass().add("form-feedback-error");
            projectFormFeedbackLabel.setText("Project name, description, version, and at least one default tag are required.");
            return;
        }

        IntegrationPointHelper.showPending(
            "Project creation backend pending",
            "Connect AdminController.handleAddProject() to ProjectFacade.createProject(ProjectCreateRequest). "
                + "Default assignee, tag, and version inputs are kept here as UI placeholders."
        );
        projectFormFeedbackLabel.getStyleClass().add("form-feedback-success");
        projectFormFeedbackLabel.setText("Preview only. Connect this form to ProjectFacade.createProject(...) later.");
    }

    @FXML
    private void clearForm() {
        loginIdField.clear();
        realNameField.clear();
        emailField.clear();
        passwordField.clear();
        roleCombo.setValue(UiRole.DEV);
        activeCheckBox.setSelected(true);
        disableReasonArea.clear();
        updateDisableReasonState();
    }

    @FXML
    private void clearProjectForm() {
        projectNameField.clear();
        projectDescriptionArea.clear();
        openForIssueEntryCheckBox.setSelected(true);
        analyticsDatasetCheckBox.setSelected(true);
        versionField.setText("1.0.0");
        defaultTagField.clear();
        tagDescriptionArea.clear();
        defaultAssigneeField.clear();
        defaultCcField.clear();
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

    private void updateDisableReasonState() {
        boolean disabled = !activeCheckBox.isSelected();
        disableReasonArea.setDisable(!disabled);
        if (!disabled) {
            disableReasonArea.clear();
        }
    }

    private void disableForms() {
        loginIdField.setDisable(true);
        realNameField.setDisable(true);
        emailField.setDisable(true);
        passwordField.setDisable(true);
        roleCombo.setDisable(true);
        activeCheckBox.setDisable(true);
        disableReasonArea.setDisable(true);
        projectNameField.setDisable(true);
        projectDescriptionArea.setDisable(true);
        openForIssueEntryCheckBox.setDisable(true);
        analyticsDatasetCheckBox.setDisable(true);
        versionField.setDisable(true);
        defaultTagField.setDisable(true);
        tagDescriptionArea.setDisable(true);
        defaultAssigneeField.setDisable(true);
        defaultCcField.setDisable(true);
    }

    private String trimmed(String value) {
        return value == null ? "" : value.trim();
    }

    private void refreshUsersFromStore() {
        users.setAll(MockAccountStore.getAdminRows());
    }

    public void selectUserTab() {
        adminTabPane.getSelectionModel().select(0);
    }

    public void selectProjectTab() {
        adminTabPane.getSelectionModel().select(1);
    }
}
