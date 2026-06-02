package com.example.its.application.service;

import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Priority;
import com.example.its.persistence.query.StatisticsQueryRepository;
import com.example.its.persistence.transaction.TransactionManager;
import com.example.its.shared.dto.issue.DailyIssueStatisticsRequest;
import com.example.its.shared.dto.issue.MonthlyIssueStatisticsRequest;
import com.example.its.shared.dto.issue.StatisticsResponse;

import java.time.LocalDate;
import java.time.YearMonth;
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

    // 2. 최근 N일간 일별 이슈 발생 현황 조회
    public Map<String, Long> getDailyIssueStatistics(DailyIssueStatisticsRequest request) {
        validateDailyRequest(request);

        return TransactionManager.execute(entityManager -> {
            StatisticsQueryRepository statisticsQueryRepository = new StatisticsQueryRepository(entityManager);

            Map<String, Long> dailyCounts = new LinkedHashMap<>();
            LocalDate startDate = LocalDate.now().minusDays(request.getDays() - 1L);
            for (int i = 0; i < request.getDays(); i++) {
                dailyCounts.put(startDate.plusDays(i).toString(), 0L);
            }

            List<Object[]> results = statisticsQueryRepository.countDailyIssues(
                request.getProjectId(),
                request.getDays()
            );
            for (Object[] row : results) {
                String date = (String) row[0];
                Long count = ((Number) row[1]).longValue();
                dailyCounts.put(date, count);
            }

            return dailyCounts;
        });
    }

    // 3. 최근 N개월간 월별 이슈 발생 현황 조회
    public Map<String, Long> getMonthlyIssueStatistics(MonthlyIssueStatisticsRequest request) {
        validateMonthlyRequest(request);

        return TransactionManager.execute(entityManager -> {
            StatisticsQueryRepository statisticsQueryRepository = new StatisticsQueryRepository(entityManager);

            Map<String, Long> monthlyCounts = new LinkedHashMap<>();
            YearMonth startMonth = YearMonth.now().minusMonths(request.getMonths() - 1L);
            for (int i = 0; i < request.getMonths(); i++) {
                monthlyCounts.put(startMonth.plusMonths(i).toString(), 0L);
            }

            List<Object[]> results = statisticsQueryRepository.countMonthlyIssues(
                request.getProjectId(),
                request.getMonths()
            );
            for (Object[] row : results) {
                String month = (String) row[0];
                Long count = ((Number) row[1]).longValue();
                monthlyCounts.put(month, count);
            }

            return monthlyCounts;
        });
    }

    private void validateDailyRequest(DailyIssueStatisticsRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("일별 이슈 통계 요청 정보가 필요합니다.");
        }
        if (request.getProjectId() == null) {
            throw new IllegalArgumentException("프로젝트 ID가 필요합니다.");
        }
        if (request.getDays() <= 0) {
            throw new IllegalArgumentException("조회할 일 수는 1 이상이어야 합니다.");
        }
    }

    private void validateMonthlyRequest(MonthlyIssueStatisticsRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("월별 이슈 통계 요청 정보가 필요합니다.");
        }
        if (request.getProjectId() == null) {
            throw new IllegalArgumentException("프로젝트 ID가 필요합니다.");
        }
        if (request.getMonths() <= 0) {
            throw new IllegalArgumentException("조회할 월 수는 1 이상이어야 합니다.");
        }
    }
}
