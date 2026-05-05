package com.example.its.application.mapper;

import com.example.its.persistence.entity.Issue;
import com.example.its.shared.dto.issue.IssueDetailResponse;
import com.example.its.shared.dto.issue.IssueSummaryResponse;
import java.util.List;
import java.util.stream.Collectors;

public class IssueMapper {

    // Entity -> 상세 정보 Response DTO (이미 DTO에 구현된 from 메서드 활용)
    public IssueDetailResponse toDetailResponse(Issue issue) {
        if (issue == null) return null;
        return IssueDetailResponse.from(issue);
    }

    // Entity -> 요약 정보 Response DTO (목록 조회용)
    public IssueSummaryResponse toSummaryResponse(Issue issue) {
        if (issue == null) return null;
        return IssueSummaryResponse.from(issue);
    }

    // Entity 리스트 -> 요약 정보 리스트 변환
    public List<IssueSummaryResponse> toSummaryResponseList(List<Issue> issues) {
        return issues.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }
}