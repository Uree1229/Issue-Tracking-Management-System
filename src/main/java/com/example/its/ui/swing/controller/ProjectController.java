package com.example.its.ui.swing.controller;

import com.example.its.application.facade.ProjectFacade;
import com.example.its.persistence.entity.Role;
import com.example.its.shared.dto.project.ProjectCreateRequest;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.ui.swing.SessionContext;
import com.example.its.ui.swing.view.MainFrame;
import com.example.its.ui.swing.view.ProjectListView;
import com.example.its.ui.swing.view.dialog.ProjectCreateDialog;

import java.util.List;

public class ProjectController {

    private final ProjectListView view;
    private final MainFrame mainFrame;
    private final ProjectFacade projectFacade;
    private IssueController issueController;

    public ProjectController(ProjectListView view, MainFrame mainFrame, ProjectFacade projectFacade) {
        this.view = view;
        this.mainFrame = mainFrame;
        this.projectFacade = projectFacade;
        initListeners();
    }

    public void setIssueController(IssueController issueController) {
        this.issueController = issueController;
    }

    private void initListeners() {
        view.getSelectButton().addActionListener(e -> handleSelectProject());
        view.getCreateButton().addActionListener(e -> handleCreateProject());
        view.getDeleteButton().addActionListener(e -> handleDeleteProject());
        view.getLogoutButton().addActionListener(e -> handleLogout());
    }

    private void handleSelectProject() {
        int row = view.getSelectedRow();
        if (row < 0) return;

        Long projectId = (Long) view.getValueAt(row, 0);
        SessionContext.setCurrentProjectId(projectId);
        if (issueController != null) {
            issueController.setCurrentProjectId(projectId);
            issueController.loadIssues();
        }
        mainFrame.showIssueList();
    }

    private void handleCreateProject() {
        ProjectCreateDialog dialog = new ProjectCreateDialog(mainFrame);
        dialog.getCreateButton().addActionListener(e -> {
            String name = dialog.getProjectName();
            if (name.isEmpty()) { dialog.showError("프로젝트명을 입력하세요."); return; }

            try {
                Long creatorId = SessionContext.getCurrentAccount().getAccountId();
                projectFacade.createProject(new ProjectCreateRequest(name, dialog.getDescription(), creatorId));
                loadProjects();
                dialog.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                dialog.showError("생성 실패: " + ex.getMessage());
            }
        });
        dialog.setVisible(true);
    }

    private void handleDeleteProject() {
        // TODO: ProjectFacade에 deleteProject 노출 필요
    }

    private void handleLogout() {
        SessionContext.clear();
        mainFrame.showLogin();
    }

    public void loadProjects() {
        try {
            List<ProjectResponse> projects = projectFacade.getAllProjects();
            Object[][] data = projects.stream()
                .map(p -> new Object[]{
                    p.getProjectId(), p.getName(), p.getDescription(),
                    p.getCreatedAt() != null ? p.getCreatedAt().toLocalDate().toString() : ""
                })
                .toArray(Object[][]::new);
            view.setProjects(data);
        } catch (Exception ex) {
            view.setProjects(new Object[0][]);
        }

        if (SessionContext.getCurrentAccount() != null) {
            boolean isAdmin = SessionContext.getCurrentAccount().getRole() == Role.ADMIN;
            view.setAdminButtonsVisible(isAdmin);
        }
    }
}
