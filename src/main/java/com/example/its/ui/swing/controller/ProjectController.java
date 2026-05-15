package com.example.its.ui.swing.controller;

import com.example.its.application.facade.ProjectFacade;
import com.example.its.persistence.entity.Role;
import com.example.its.shared.dto.project.ProjectCreateRequest;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.shared.dto.project.ProjectTagUpdateRequest;
import com.example.its.ui.swing.SessionContext;
import com.example.its.ui.swing.view.MainFrame;
import com.example.its.ui.swing.view.ProjectListView;
import com.example.its.ui.swing.view.dialog.ProjectCreateDialog;
import com.example.its.ui.swing.view.dialog.ProjectTagDialog;

import javax.swing.JOptionPane;
import java.util.Collections;
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
        view.getTagButton().addActionListener(e -> handleManageTags());
        view.getAccountButton().addActionListener(e -> mainFrame.showAccountManage());
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
                List<String> tagNames = dialog.getTagNames();
                projectFacade.createProject(new ProjectCreateRequest(name, dialog.getDescription(), creatorId, tagNames));
                loadProjects();
                dialog.dispose();
            } catch (Exception ex) {
                dialog.showError("생성 실패: " + ex.getMessage());
            }
        });
        dialog.setVisible(true);
    }

    private void handleManageTags() {
        int row = view.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainFrame, "태그를 추가할 프로젝트를 선택하세요.");
            return;
        }
        Long projectId = (Long) view.getValueAt(row, 0);
        ProjectTagDialog dialog = new ProjectTagDialog(mainFrame);
        dialog.getApplyButton().addActionListener(e -> {
            List<String> tagNames = dialog.getTagNames();
            if (tagNames.isEmpty()) { dialog.showError("태그명을 입력하세요."); return; }
            try {
                projectFacade.updateProjectTags(new ProjectTagUpdateRequest(projectId, tagNames, Collections.emptyList()));
                dialog.dispose();
                JOptionPane.showMessageDialog(mainFrame, "태그가 추가됐습니다.");
            } catch (Exception ex) {
                dialog.showError("태그 추가 실패: " + ex.getMessage());
            }
        });
        dialog.setVisible(true);
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
