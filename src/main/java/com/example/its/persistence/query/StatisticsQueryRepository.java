package com.example.its.persistence.query;

import com.example.its.persistence.entity.Issue;
import com.example.its.persistence.repository.JpaRepositorySupport;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;

public class StatisticsQueryRepository extends JpaRepositorySupport<Issue> {

    public StatisticsQueryRepository(EntityManager entityManager) {
        super(Issue.class, entityManager);
    }

    // 1. 프로젝트의 전체 이슈 개수 조회
    public long countByProjectId(Long projectId) {
        Long count = entityManager.createQuery(
                "select count(i) from Issue i where i.project.projectId = :projectId", 
                Long.class
            )
            .setParameter("projectId", projectId)
            .getSingleResult();
        return count != null ? count : 0L;
    }

    // 2. 상태(Status)별 이슈 개수 그룹핑
    public List<Object[]> countGroupByStatus(Long projectId) {
        return entityManager.createQuery(
                "select i.status, count(i) from Issue i " +
                "where i.project.projectId = :projectId " +
                "group by i.status", 
                Object[].class
            )
            .setParameter("projectId", projectId)
            .getResultList();
    }

    // 3. 우선순위(Priority)별 이슈 개수 그룹핑
    public List<Object[]> countGroupByPriority(Long projectId) {
        return entityManager.createQuery(
                "select i.priority, count(i) from Issue i " +
                "where i.project.projectId = :projectId " +
                "group by i.priority", 
                Object[].class
            )
            .setParameter("projectId", projectId)
            .getResultList();
    }

    // 4. 최근 N일간 일별 이슈 발생 개수 그룹핑
    public List<Object[]> countDailyIssues(Long projectId, int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("조회할 일 수는 1 이상이어야 합니다.");
        }

        String startReportedAt = LocalDate.now()
            .minusDays(days - 1L)
            .atStartOfDay()
            .toString();

        return entityManager.createQuery(
                "select substring(i.reportedAt, 1, 10), count(i) from Issue i " +
                "where i.project.projectId = :projectId " +
                "and i.reportedAt >= :startReportedAt " +
                "group by substring(i.reportedAt, 1, 10) " +
                "order by substring(i.reportedAt, 1, 10) asc",
                Object[].class
            )
            .setParameter("projectId", projectId)
            .setParameter("startReportedAt", startReportedAt)
            .getResultList();
    }

    // 5. 최근 N개월간 월별 이슈 발생 개수 그룹핑
    public List<Object[]> countMonthlyIssues(Long projectId, int months) {
        if (months <= 0) {
            throw new IllegalArgumentException("조회할 월 수는 1 이상이어야 합니다.");
        }

        String startReportedAt = LocalDate.now()
            .withDayOfMonth(1)
            .minusMonths(months - 1L)
            .atStartOfDay()
            .toString();

        return entityManager.createQuery(
                "select substring(i.reportedAt, 1, 7), count(i) from Issue i " +
                "where i.project.projectId = :projectId " +
                "and i.reportedAt >= :startReportedAt " +
                "group by substring(i.reportedAt, 1, 7) " +
                "order by substring(i.reportedAt, 1, 7) asc",
                Object[].class
            )
            .setParameter("projectId", projectId)
            .setParameter("startReportedAt", startReportedAt)
            .getResultList();
    }
}
