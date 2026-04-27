package com.example.its.ui.swing.controller;

import com.example.its.ui.swing.view.IssueCreateView;
import com.example.its.ui.swing.view.IssueDetailView;
import com.example.its.ui.swing.view.IssueListView;
import com.example.its.ui.swing.view.MainFrame;
import com.example.its.ui.swing.view.dialog.AssigneeDialog;

public class IssueController {

    private final IssueListView listView;
    private final IssueDetailView detailView;
    private final IssueCreateView createView;
    private final MainFrame mainFrame;
    // TODO: private final IssueFacade issueFacade;
    // TODO: private final AccountFacade accountFacade;

    private Long currentProjectId;
    private Long currentIssueId;

    public IssueController(IssueListView listView, IssueDetailView detailView,
                           IssueCreateView createView, MainFrame mainFrame) {
        this.listView   = listView;
        this.detailView = detailView;
        this.createView = createView;
        this.mainFrame  = mainFrame;
        initListeners();
    }

    private void initListeners() {
        // 이슈 목록
        listView.getSearchButton().addActionListener(e -> loadIssues());
        listView.getResetButton().addActionListener(e -> { listView.resetFilters(); loadIssues(); });
        listView.getCreateButton().addActionListener(e -> showCreateView());
        listView.getStatisticsButton().addActionListener(e -> mainFrame.showStatistics());
        listView.getBackButton().addActionListener(e -> mainFrame.showProjectList());
        listView.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showIssueDetail();
        });

        // 이슈 상세
        detailView.getBackButton().addActionListener(e -> mainFrame.showIssueList());
        detailView.getAddCommentButton().addActionListener(e -> handleAddComment());
        detailView.getAssignButton().addActionListener(e -> handleAssign());
        detailView.getFixButton().addActionListener(e -> handleFix());
        detailView.getResolveButton().addActionListener(e -> handleResolve());
        detailView.getReopenButton().addActionListener(e -> handleReopen());
        detailView.getCloseButton().addActionListener(e -> handleClose());

        // 이슈 등록
        createView.getSubmitButton().addActionListener(e -> handleCreateIssue());
        createView.getCancelButton().addActionListener(e -> mainFrame.showIssueList());
    }

    public void setCurrentProjectId(Long projectId) {
        this.currentProjectId = projectId;
    }

    // ── 이슈 목록 ──────────────────────────────────────────────

    public void loadIssues() {
        // TODO: IssueSearchCondition 조건 조합 후 issueFacade.searchIssues(currentProjectId, condition)
        // List<IssueSummaryResponse> issues = issueFacade.searchIssues(currentProjectId, condition);
        // listView.getIssueTableModel().setRows(...);

        // 임시 더미 데이터
        java.util.List<Object[]> dummy = new java.util.ArrayList<>();
        dummy.add(new Object[]{1L, "로그인 버튼 클릭 시 에러", "NEW", "MAJOR", "tester1", "", "2026-04-27"});
        listView.getIssueTableModel().setRows(dummy);
    }

    // ── 이슈 상세 ──────────────────────────────────────────────

    private void showIssueDetail() {
        int row = listView.getSelectedRow();
        if (row < 0) return;

        currentIssueId = listView.getIssueTableModel().getIssueIdAt(row);
        // TODO: IssueDetailResponse detail = issueFacade.getIssue(currentIssueId);
        // bindDetailView(detail);
        // configureActionButtons(detail.getStatus(), SessionContext.getCurrentAccount().getRole());

        // 임시 바인딩
        detailView.setTitle("로그인 버튼 클릭 시 에러");
        detailView.setStatus("NEW");
        detailView.setPriority("MAJOR");
        detailView.setReporter("tester1");
        detailView.setAssignee("-");
        detailView.setFixer("-");
        detailView.setReportedAt("2026-04-27");
        detailView.setLastModifiedAt("2026-04-27");
        detailView.setDescription("로그인 화면에서 버튼 클릭 시 NPE 발생");
        detailView.setTags("");
        detailView.setCommentHistory("");

        detailView.hideAllActionButtons();
        // TODO: 역할+상태 기반으로 버튼 표시
        detailView.setAssignButtonVisible(true);

        mainFrame.showIssueDetail();
    }

    // ── 상태 변경 핸들러 ──────────────────────────────────────

    private void handleAssign() {
        AssigneeDialog dialog = new AssigneeDialog(mainFrame);
        // TODO: AccountResponse 리스트 로딩
        // List<AccountResponse> devs = accountFacade.getDevAccounts();
        dialog.setDevelopers(new String[]{"dev1 (추천 1위)", "dev2 (추천 2위)", "dev3"});
        dialog.getConfirmButton().addActionListener(e -> {
            int idx = dialog.getSelectedIndex();
            if (idx < 0) return;
            // TODO: issueFacade.assignIssue(currentIssueId, selectedAccountId);
            dialog.dispose();
            refreshDetail();
        });
        dialog.setVisible(true);
    }

    private void handleFix() {
        // TODO: issueFacade.fixIssue(currentIssueId);
        refreshDetail();
    }

    private void handleResolve() {
        // TODO: issueFacade.resolveIssue(currentIssueId);
        refreshDetail();
    }

    private void handleReopen() {
        // TODO: issueFacade.reopenIssue(currentIssueId);
        refreshDetail();
    }

    private void handleClose() {
        // TODO: issueFacade.closeIssue(currentIssueId);
        refreshDetail();
    }

    private void handleAddComment() {
        String content = detailView.getCommentInput();
        if (content.isEmpty()) return;
        // TODO: issueFacade.addComment(currentIssueId, new CommentCreateRequest(content));
        detailView.clearCommentInput();
        refreshDetail();
    }

    private void refreshDetail() {
        // TODO: 상세 화면 재로딩
    }

    // ── 이슈 등록 ──────────────────────────────────────────────

    private void showCreateView() {
        // TODO: SessionContext에서 현재 계정명과 현재 시각 설정
        createView.setReporter("현재계정");
        createView.setReportedAt(java.time.LocalDateTime.now().toString());
        createView.clearForm();
        mainFrame.showIssueCreate();
    }

    private void handleCreateIssue() {
        String title = createView.getTitle();
        String desc  = createView.getDescription();

        if (title.isEmpty() || desc.isEmpty()) return;

        // TODO: issueFacade.createIssue(currentProjectId, new IssueCreateRequest(title, desc, priority));
        createView.clearForm();
        loadIssues();
        mainFrame.showIssueList();
    }
}
