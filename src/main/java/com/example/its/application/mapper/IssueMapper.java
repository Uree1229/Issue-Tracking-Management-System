package com.example.its.application.mapper;

import com.example.its.persistence.entity.Comment;
import com.example.its.persistence.entity.Issue;
import com.example.its.persistence.entity.IssueDelta;
import com.example.its.persistence.entity.IssueHistory;
import com.example.its.shared.dto.issue.CommentResponse;
import com.example.its.shared.dto.issue.IssueDeltaResponse;
import com.example.its.shared.dto.issue.IssueDetailResponse;
import com.example.its.shared.dto.issue.IssueHistoryResponse;
import com.example.its.shared.dto.issue.IssueSummaryResponse;
import java.util.List;
import java.util.stream.Collectors;

public class IssueMapper {

    // Entity -> 상세 정보 Response DTO
    public IssueDetailResponse toDetailResponse(Issue issue) {
        if (issue == null) return null;
        return new IssueDetailResponse(
                issue.getIssueId(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getStatus(),
                issue.getPriority(),
                issue.getReporter() != null ? issue.getReporter().getAccountId() : null,
                issue.getReporter() != null ? issue.getReporter().getName() : null,
                issue.getAssignee() != null ? issue.getAssignee().getAccountId() : null,
                issue.getAssignee() != null ? issue.getAssignee().getName() : null,
                issue.getFixer() != null ? issue.getFixer().getAccountId() : null,
                issue.getFixer() != null ? issue.getFixer().getName() : null,
                issue.getProject() != null ? issue.getProject().getProjectId() : null,
                issue.getProject() != null ? issue.getProject().getName() : null,
                issue.getReportedAt(),
                issue.getLastModifiedAt(),
                issue.getTags().stream().map(tag -> tag.getName()).collect(Collectors.toList()),
                issue.getComments().stream().map(this::toCommentResponse).collect(Collectors.toList()),
                issue.getIssueHistories().stream().map(this::toHistoryResponse).collect(Collectors.toList())
        );
    }

    // Entity -> 요약 정보 Response DTO (목록 조회용)
    public IssueSummaryResponse toSummaryResponse(Issue issue) {
        if (issue == null) return null;
        return new IssueSummaryResponse(
                issue.getIssueId(),
                issue.getTitle(),
                issue.getStatus(),
                issue.getPriority(),
                issue.getReporter() != null ? issue.getReporter().getAccountId() : null,
                issue.getReporter() != null ? issue.getReporter().getName() : null,
                issue.getAssignee() != null ? issue.getAssignee().getAccountId() : null,
                issue.getAssignee() != null ? issue.getAssignee().getName() : null,
                issue.getProject() != null ? issue.getProject().getProjectId() : null,
                issue.getReportedAt(),
                issue.getLastModifiedAt()
        );
    }

    public CommentResponse toCommentResponse(Comment comment) {
        if (comment == null) return null;
        return new CommentResponse(
                comment.getCommentId(),
                comment.getIssue() != null ? comment.getIssue().getIssueId() : null,
                comment.getAuthor() != null ? comment.getAuthor().getAccountId() : null,
                comment.getAuthor() != null ? comment.getAuthor().getName() : null,
                comment.getContent(),
                comment.getCreatedAt()
        );
    }

    public IssueHistoryResponse toHistoryResponse(IssueHistory history) {
        if (history == null) return null;
        return new IssueHistoryResponse(
                history.getHistoryId(),
                history.getIssue() != null ? history.getIssue().getIssueId() : null,
                history.getChangedBy() != null ? history.getChangedBy().getAccountId() : null,
                history.getChangedBy() != null ? history.getChangedBy().getLoginId() : null,
                history.getChangedAt(),
                toDeltaResponse(history.getIssueDelta())
        );
    }

    public IssueDeltaResponse toDeltaResponse(IssueDelta delta) {
        if (delta == null) return null;
        return new IssueDeltaResponse(
                delta.getDeltaId(),
                delta.getOldTitle(),
                delta.getNewTitle(),
                delta.getOldContent(),
                delta.getNewContent(),
                delta.getOldPriority(),
                delta.getNewPriority(),
                delta.getOldStatus(),
                delta.getNewStatus()
        );
    }

    // Entity 리스트 -> 요약 정보 리스트 변환
    public List<IssueSummaryResponse> toSummaryResponseList(List<Issue> issues) {
        return issues.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }
}
