package com.example.its.persistence.query;

import com.example.its.persistence.entity.Issue;
import com.example.its.persistence.repository.JpaRepositorySupport;
import jakarta.persistence.EntityManager;

import java.util.List;

public class StatisticsQueryRepository extends JpaRepositorySupport<Issue> {

    public StatisticsQueryRepository() {
        super(Issue.class);
    }

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
}