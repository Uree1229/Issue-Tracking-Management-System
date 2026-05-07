package com.example.its.application.service;

import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Priority;
import com.example.its.persistence.query.StatisticsQueryRepository;
import com.example.its.persistence.transaction.TransactionManager;
import com.example.its.shared.dto.issue.StatisticsResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IssueStatisticsService {

    public IssueStatisticsService() {
    }

    // 1. 프로젝트 대시보드용 통계 조회
    public StatisticsResponse getProjectStatistics(Long projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("프로젝트 ID가 필요합니다.");
        }

        return TransactionManager.execute(entityManager -> {
            StatisticsQueryRepository statisticsQueryRepository = new StatisticsQueryRepository(entityManager);

            // 1. 전체 개수 조회
            long totalCount = statisticsQueryRepository.countByProjectId(projectId);

            // 2. 상태별 통계 가공 (FE를 위해 모든 상태값을 0으로 기본 세팅해 둠.)
            Map<IssueStatus, Long> statusCounts = new LinkedHashMap<>();
            for (IssueStatus status : IssueStatus.values()) {
                statusCounts.put(status, 0L);
            }

            List<Object[]> statusResults = statisticsQueryRepository.countGroupByStatus(projectId);
            for (Object[] row : statusResults) {
                IssueStatus status = (IssueStatus) row[0];
                Long count = (Long) row[1];
                statusCounts.put(status, count);
            }

            // 3. 우선순위별 통계 가공 (마찬가지로 0으로 기본 세팅)
            Map<Priority, Long> priorityCounts = new LinkedHashMap<>();
            for (Priority priority : Priority.values()) {
                priorityCounts.put(priority, 0L);
            }

            List<Object[]> priorityResults = statisticsQueryRepository.countGroupByPriority(projectId);
            for (Object[] row : priorityResults) {
                Priority priority = (Priority) row[0];
                Long count = (Long) row[1];
                priorityCounts.put(priority, count);
            }

            // 4. DTO 포장 및 리턴
            return new StatisticsResponse(totalCount, statusCounts, priorityCounts);
        });
    }
}
