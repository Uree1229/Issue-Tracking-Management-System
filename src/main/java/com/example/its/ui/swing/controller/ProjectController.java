package com.example.its.ui.swing.controller;

import com.example.its.ui.swing.view.MainFrame;
import com.example.its.ui.swing.view.ProjectListView;
import com.example.its.ui.swing.view.dialog.ProjectCreateDialog;

public class ProjectController {

    private final ProjectListView view;
    private final MainFrame mainFrame;
    // TODO: private final ProjectFacade projectFacade;
    // TODO: private final AccountFacade accountFacade;

    public ProjectController(ProjectListView view, MainFrame mainFrame) {
        this.view = view;
        this.mainFrame = mainFrame;
        initListeners();
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

        // TODO: 선택한 프로젝트 ID를 SessionContext 또는 IssueController에 전달
        // Long projectId = (Long) view.getValueAt(row, 0);
        // SessionContext.setCurrentProjectId(projectId);
        mainFrame.showIssueList();
    }

    private void handleCreateProject() {
        ProjectCreateDialog dialog = new ProjectCreateDialog(mainFrame);
        dialog.getCreateButton().addActionListener(e -> {
            String name = dialog.getProjectName();
            if (name.isEmpty()) { dialog.showError("프로젝트명을 입력하세요."); return; }

            // TODO: projectFacade.createProject(new ProjectCreateRequest(name, dialog.getDescription()));
            // loadProjects();
            dialog.dispose();
        });
        dialog.setVisible(true);
    }

    private void handleDeleteProject() {
        int row = view.getSelectedRow();
        if (row < 0) return;
        // TODO: Long projectId = (Long) view.getValueAt(row, 0);
        // projectFacade.deleteProject(projectId);
        // loadProjects();
    }

    private void handleLogout() {
        // TODO: accountFacade.logout(); SessionContext.clear();
        mainFrame.showLogin();
    }

    // TODO: BE 연결 후 실제 데이터 로딩
    public void loadProjects() {
        // List<ProjectResponse> projects = projectFacade.getAllProjects();
        // Object[][] data = projects.stream()
        //     .map(p -> new Object[]{p.getProjectId(), p.getName(), p.getDescription(), p.getCreatedAt()})
        //     .toArray(Object[][]::new);
        // view.setProjects(data);

        // 임시 더미 데이터
        view.setProjects(new Object[][]{
            {1L, "project1", "샘플 프로젝트", "2026-01-01"}
        });

        // TODO: 현재 로그인 계정이 ADMIN일 때만 관리 버튼 표시
        // boolean isAdmin = SessionContext.getCurrentAccount().getRole() == Role.ADMIN;
        // view.setAdminButtonsVisible(isAdmin);
    }
}
