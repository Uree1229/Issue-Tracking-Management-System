package com.example.its.application.service;

import com.example.its.persistence.entity.Issue;
import com.example.its.persistence.query.IssueQueryRepository;
import com.example.its.persistence.transaction.TransactionManager;
import com.example.its.shared.dto.issue.IssueSearchCondition;
import com.example.its.shared.dto.issue.IssueSummaryResponse;

import java.util.List;
import java.util.stream.Collectors;

public class IssueSearchService {

    public IssueSearchService() {
    }

    // 1. 복합 조건 이슈 검색
    public List<IssueSummaryResponse> searchIssues(IssueSearchCondition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("검색 조건이 입력되지 않았습니다.");
        }

        return TransactionManager.execute(entityManager -> {
            IssueQueryRepository issueQueryRepository = new IssueQueryRepository(entityManager);

            // 동적 쿼리로 필터링된 엔티티 리스트 조회
            List<Issue> searchResults = issueQueryRepository.search(condition);

            // FE에서 리스트를 그리기 편하도록 SummaryResponse DTO로 변환하여 리턴
            return searchResults.stream()
                    .map(IssueSummaryResponse::from)
                    .collect(Collectors.toList());
        });
    }
}
