package com.example.its.ui.swing.controller;

import com.example.its.application.facade.AccountFacade;
import com.example.its.application.facade.IssueFacade;
import com.example.its.application.facade.ProjectFacade;
import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Priority;
import com.example.its.persistence.entity.Role;
import com.example.its.shared.dto.account.AccountResponse;

import com.example.its.shared.dto.issue.*;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.shared.dto.project.ProjectTagUpdateRequest;
import com.example.its.shared.dto.tag.TagResponse;
import com.example.its.ui.swing.SessionContext;
import com.example.its.ui.swing.view.*;
import com.example.its.ui.swing.view.dialog.AssigneeDialog;
import com.example.its.ui.swing.view.dialog.TagEditDialog;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class IssueController {

    private final IssueListView listView;
    private final IssueDetailView detailView;
    private final IssueCreateView createView;
    private final MainFrame mainFrame;
    private final IssueFacade issueFacade;
    private final AccountFacade accountFacade;
    private final ProjectFacade projectFacade;

    private Long currentProjectId;
    private Long currentIssueId;
    private IssueDetailResponse currentDetail;

    public IssueController(IssueListView listView, IssueDetailView detailView,
                           IssueCreateView createView, MainFrame mainFrame,
                           IssueFacade issueFacade, AccountFacade accountFacade,
                           ProjectFacade projectFacade) {
        this.listView = listView;
        this.detailView = detailView;
        this.createView = createView;
        this.mainFrame = mainFrame;
        this.issueFacade = issueFacade;
        this.accountFacade = accountFacade;
        this.projectFacade = projectFacade;
        initListeners();
    }

    private void initListeners() {
        listView.getSearchButton().addActionListener(e -> loadIssues());
        listView.getResetButton().addActionListener(e -> { listView.resetFilters(); applyRoleRestrictions(); loadIssues(); });
        listView.getCreateButton().addActionListener(e -> showCreateView());
        listView.getStatisticsButton().addActionListener(e -> showStatistics());
        listView.getBackButton().addActionListener(e -> mainFrame.showProjectList());
        listView.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showIssueDetail();
        });

        detailView.getBackButton().addActionListener(e -> mainFrame.showIssueList());
        detailView.getAddCommentButton().addActionListener(e -> handleAddComment());
        detailView.getEditTagsButton().addActionListener(e -> handleEditIssueTags());
        detailView.getAssignButton().addActionListener(e -> handleAssign());
        detailView.getFixButton().addActionListener(e -> handleFix());
        detailView.getFailButton().addActionListener(e -> handleFail());
        detailView.getResolveButton().addActionListener(e -> handleResolve());
        detailView.getReopenButton().addActionListener(e -> handleReopen());
        detailView.getCloseButton().addActionListener(e -> handleClose());

        createView.getSubmitButton().addActionListener(e -> handleCreateIssue());
        createView.getCancelButton().addActionListener(e -> mainFrame.showIssueList());

        mainFrame.getStatisticsView().getBackButton().addActionListener(e -> mainFrame.showIssueList());
        mainFrame.getStatisticsView().getRefreshButton().addActionListener(e -> loadStatistics());
    }

    public void setCurrentProjectId(Long projectId) {
        this.currentProjectId = projectId;
        applyRoleRestrictions();
    }

    private void applyRoleRestrictions() {
        AccountResponse me = SessionContext.getCurrentAccount();
        if (me == null) return;
        Role role = me.getRole();

        listView.setStatisticsButtonVisible(role == Role.PL);

        if (role == Role.DEV) {
            listView.setAssigneeFilterText(me.getLoginId());
            listView.setAssigneeFilterEditable(false);
        } else {
            listView.setAssigneeFilterEditable(true);
        }
    }

    // ── 이슈 목록 ──────────────────────────────────────────────

    public void loadIssues() {
        try {
            List<IssueSummaryResponse> issues = issueFacade.searchIssues(buildSearchCondition());
            listView.getIssueTableModel().setRows(issues);
        } catch (Exception ex) {
            listView.getIssueTableModel().clearRows();
        }
    }

    private IssueSearchCondition buildSearchCondition() {
        String statusStr       = listView.getStatusFilter();
        String priorityStr     = listView.getPriorityFilter();
        String reporterLoginId = listView.getReporterFilter();
        String assigneeLoginId = listView.getAssigneeFilter();
        String keyword         = listView.getKeywordFilter();

        IssueStatus status   = "전체".equals(statusStr)   ? null : IssueStatus.valueOf(statusStr);
        Priority    priority = "전체".equals(priorityStr) ? null : Priority.valueOf(priorityStr);

        Long reporterAccountId = null;
        Long assigneeAccountId = null;
        if (!reporterLoginId.isEmpty() || !assigneeLoginId.isEmpty()) {
            try {
                List<AccountResponse> accounts = accountFacade.getActiveAccounts();
                if (!reporterLoginId.isEmpty()) {
                    reporterAccountId = accounts.stream()
                        .filter(a -> a.getLoginId().equals(reporterLoginId))
                        .map(AccountResponse::getAccountId)
                        .findFirst().orElse(null);
                }
                if (!assigneeLoginId.isEmpty()) {
                    assigneeAccountId = accounts.stream()
                        .filter(a -> a.getLoginId().equals(assigneeLoginId))
                        .map(AccountResponse::getAccountId)
                        .findFirst().orElse(null);
                }
            } catch (Exception ignored) {}
        }

        return new IssueSearchCondition(
            currentProjectId, status, priority, reporterAccountId, assigneeAccountId,
            keyword.isEmpty() ? null : keyword
        );
    }

    // ── 이슈 상세 ──────────────────────────────────────────────

    private void showIssueDetail() {
        int row = listView.getSelectedRow();
        if (row < 0) return;

        currentIssueId = listView.getIssueTableModel().getIssueIdAt(row);
        if (currentIssueId == null) return;

        try {
            currentDetail = issueFacade.getIssue(currentIssueId);
            bindDetailView(currentDetail);
            loadRecommendations();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame, "이슈 조회 실패: " + ex.getMessage());
            return;
        }
        mainFrame.showIssueDetail();
    }

    private void bindDetailView(IssueDetailResponse d) {
        detailView.setTitle(d.getTitle() != null ? d.getTitle() : "");
        detailView.setStatus(d.getStatus() != null ? d.getStatus().name() : "");
        detailView.setPriority(d.getPriority() != null ? d.getPriority().name() : "");
        detailView.setReporter(d.getReporterName() != null ? d.getReporterName() : "-");
        detailView.setAssignee(d.getAssigneeName() != null ? d.getAssigneeName() : "-");
        detailView.setFixer(d.getFixerName() != null ? d.getFixerName() : "-");
        detailView.setReportedAt(d.getReportedAt() != null ? d.getReportedAt().toLocalDate().toString() : "");
        detailView.setLastModifiedAt(d.getLastModifiedAt() != null ? d.getLastModifiedAt().toLocalDate().toString() : "");
        detailView.setDescription(d.getDescription() != null ? d.getDescription() : "");
        detailView.setTags(d.getTagNames() != null ? String.join(", ", d.getTagNames()) : "");

        if (d.getComments() != null) {
            String commentText = d.getComments().stream()
                .map(c -> "[" + (c.getCreatedAt() != null ? c.getCreatedAt().toLocalDate() : "") + "] "
                        + (c.getAuthorName() != null ? c.getAuthorName() : "") + ": " + c.getContent())
                .collect(Collectors.joining("\n"));
            detailView.setCommentHistory(commentText);
        }
    }

    private void loadRecommendations() {
        try {
            List<RecommendationResponse> recs = issueFacade.recommendAssignees(currentProjectId, Collections.emptyList());
            if (!recs.isEmpty()) {
                String text = recs.stream()
                    .map(r -> r.getName() + " [" + String.format("%.1f", r.getScore()) + "점]")
                    .collect(Collectors.joining(", "));
                detailView.setRecommendations(text);
            } else {
                detailView.setRecommendations(" ");
            }
        } catch (Exception ex) {
            detailView.setRecommendations(" ");
        }
    }

    // ── 상태 변경 핸들러 ──────────────────────────────────────

    private void handleAssign() {
        try {
            List<RecommendationResponse> recs = issueFacade.recommendAssignees(currentProjectId, Collections.emptyList());
            AssigneeDialog dialog = new AssigneeDialog(mainFrame);
            dialog.setRecommendations(recs);
            dialog.getConfirmButton().addActionListener(e -> {
                Long assigneeId = dialog.getSelectedAccountId();
                if (assigneeId == null) return;
                try {
                    Long plId = SessionContext.getCurrentAccount().getAccountId();
                    issueFacade.assign(new IssueAssignRequest(currentIssueId, plId, assigneeId));
                    dialog.dispose();
                    refreshDetail();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Assign 실패: " + ex.getMessage());
                }
            });
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame, "추천 목록 조회 실패: " + ex.getMessage());
        }
    }

    private void handleFix() {
        String comment = JOptionPane.showInputDialog(mainFrame, "완료 코멘트를 입력하세요:", "Fixed 처리", JOptionPane.PLAIN_MESSAGE);
        if (comment == null || comment.trim().isEmpty()) return;
        try {
            Long devId = SessionContext.getCurrentAccount().getAccountId();
            issueFacade.fix(new IssueFixRequest(currentIssueId, devId, comment.trim()));
            refreshDetail();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame, "Fixed 처리 실패: " + ex.getMessage());
        }
    }

    private void handleResolve() {
        try {
            Long testerId = SessionContext.getCurrentAccount().getAccountId();
            issueFacade.resolve(new IssueResolveRequest(currentIssueId, testerId));
            refreshDetail();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame, "Resolved 처리 실패: " + ex.getMessage());
        }
    }

    private void handleFail() {
        String reason = JOptionPane.showInputDialog(mainFrame, "반려 사유를 입력하세요:", "검증 실패", JOptionPane.PLAIN_MESSAGE);
        if (reason == null || reason.trim().isEmpty()) return;
        try {
            Long accountId = SessionContext.getCurrentAccount().getAccountId();
            issueFacade.fail(new IssueFailRequest(currentIssueId, accountId, reason.trim()));
            refreshDetail();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame, "검증 실패 처리 오류: " + ex.getMessage());
        }
    }

    private void handleReopen() {
        try {
            Long accountId = SessionContext.getCurrentAccount().getAccountId();
            issueFacade.reopen(new IssueReopenRequest(currentIssueId, accountId, null));
            refreshDetail();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame, "Reopen 실패: " + ex.getMessage());
        }
    }

    private void handleClose() {
        try {
            Long plId = SessionContext.getCurrentAccount().getAccountId();
            issueFacade.close(new IssueCloseRequest(currentIssueId, plId));
            refreshDetail();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame, "Close 실패: " + ex.getMessage());
        }
    }

    private void handleEditIssueTags() {
        if (currentDetail == null) return;
        try {
            ProjectResponse project = projectFacade.getProject(currentProjectId);
            List<TagResponse> projectTags = project.getTags();

            Set<String> currentTagNames = currentDetail.getTagNames() != null
                    ? new HashSet<>(currentDetail.getTagNames()) : Collections.emptySet();
            Set<Long> currentTagIds = projectTags.stream()
                    .filter(t -> currentTagNames.contains(t.getName()))
                    .map(TagResponse::getTagId)
                    .collect(Collectors.toSet());

            TagEditDialog dialog = new TagEditDialog(mainFrame, projectTags, currentTagIds);
            dialog.setVisible(true);
            if (!dialog.isConfirmed()) return;

            List<Long>   toAdd        = new ArrayList<>(dialog.getTagIdsToAdd());
            List<Long>   toRemove     = dialog.getTagIdsToRemove();
            List<String> newTagNames  = dialog.getNewTagNamesToCreate();

            if (!newTagNames.isEmpty()) {
                projectFacade.updateProjectTags(
                        new ProjectTagUpdateRequest(currentProjectId, newTagNames, Collections.emptyList()));
                ProjectResponse refreshed = projectFacade.getProject(currentProjectId);
                Set<String> wanted = new HashSet<>(newTagNames);
                for (TagResponse t : refreshed.getTags()) {
                    if (wanted.contains(t.getName())) toAdd.add(t.getTagId());
                }
            }

            if (!toAdd.isEmpty() || !toRemove.isEmpty()) {
                issueFacade.updateIssueTags(new IssueTagUpdateRequest(currentIssueId, toAdd, toRemove));
                refreshDetail();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame, "태그 편집 실패: " + ex.getMessage());
        }
    }

    private void handleAddComment() {
        String content = detailView.getCommentInput();
        if (content.isEmpty()) return;
        try {
            Long authorId = SessionContext.getCurrentAccount().getAccountId();
            issueFacade.addComment(new CommentCreateRequest(currentIssueId, authorId, content));
            detailView.clearCommentInput();
            refreshDetail();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame, "댓글 추가 실패: " + ex.getMessage());
        }
    }

    private void refreshDetail() {
        try {
            currentDetail = issueFacade.getIssue(currentIssueId);
            bindDetailView(currentDetail);
            loadRecommendations();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame, "새로고침 실패: " + ex.getMessage());
        }
    }

    // ── 이슈 등록 ──────────────────────────────────────────────

    private void showCreateView() {
        AccountResponse me = SessionContext.getCurrentAccount();
        createView.clearForm();
        createView.setReporter(me != null ? me.getName() : "");
        createView.setReportedAt(java.time.LocalDate.now().toString());
        try {
            ProjectResponse project = projectFacade.getProject(currentProjectId);
            createView.setAvailableTags(project.getTags());
        } catch (Exception ex) {
            createView.setAvailableTags(Collections.emptyList());
        }
        mainFrame.showIssueCreate();
    }

    private void handleCreateIssue() {
        String title = createView.getTitle();
        String desc  = createView.getDescription();
        if (title.isEmpty() || desc.isEmpty()) return;

        try {
            Long reporterId = SessionContext.getCurrentAccount().getAccountId();
            Priority priority = Priority.valueOf(createView.getPriority());

            List<Long>   tagIds       = new ArrayList<>(createView.getSelectedExistingTagIds());
            List<String> newTagNames  = createView.getNewTagNamesToCreate();

            if (!newTagNames.isEmpty()) {
                projectFacade.updateProjectTags(
                        new ProjectTagUpdateRequest(currentProjectId, newTagNames, Collections.emptyList()));
                ProjectResponse refreshed = projectFacade.getProject(currentProjectId);
                Set<String> wanted = new HashSet<>(newTagNames);
                for (TagResponse t : refreshed.getTags()) {
                    if (wanted.contains(t.getName())) tagIds.add(t.getTagId());
                }
            }

            issueFacade.registerIssue(new IssueCreateRequest(
                title, desc, priority, reporterId, null, currentProjectId, tagIds
            ));
            createView.clearForm();
            loadIssues();
            mainFrame.showIssueList();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame, "이슈 등록 실패: " + ex.getMessage());
        }
    }

    // ── 통계 ──────────────────────────────────────────────────

    private void showStatistics() {
        loadStatistics();
        mainFrame.showStatistics();
    }

    public void loadStatistics() {
        StatisticsView statsView = mainFrame.getStatisticsView();
        try {
            StatisticsResponse stats = issueFacade.getStatistics(currentProjectId);

            StringBuilder statusSb = new StringBuilder("[ 상태별 집계 ]\n전체: ")
                    .append(stats.getTotalIssueCount()).append("건\n\n");
            stats.getStatusCounts().forEach((s, c) -> statusSb.append(s.name()).append(": ").append(c).append("건\n"));
            statusSb.append("\n[ 우선순위별 집계 ]\n");
            stats.getPriorityCounts().forEach((p, c) -> statusSb.append(p.name()).append(": ").append(c).append("건\n"));
            statsView.setStatusStats(statusSb.toString());

            Map<String, Long> daily = issueFacade.getDailyIssueStatistics(
                    new DailyIssueStatisticsRequest(currentProjectId, 30));
            StringBuilder dailySb = new StringBuilder("[ 최근 30일 ]\n\n");
            daily.forEach((date, count) -> dailySb.append(date).append(": ").append(count).append("건\n"));
            statsView.setDailyStats(dailySb.toString());

            Map<String, Long> monthly = issueFacade.getMonthlyIssueStatistics(
                    new MonthlyIssueStatisticsRequest(currentProjectId, 12));
            StringBuilder monthlySb = new StringBuilder("[ 최근 12개월 ]\n\n");
            monthly.forEach((month, count) -> monthlySb.append(month).append(": ").append(count).append("건\n"));
            statsView.setMonthlyStats(monthlySb.toString());
        } catch (Exception ex) {
            statsView.setStatusStats("통계 조회 실패: " + ex.getMessage());
            statsView.setDailyStats("");
            statsView.setMonthlyStats("");
        }
    }
}
