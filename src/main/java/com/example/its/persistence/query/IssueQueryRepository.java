package com.example.its.persistence.query;

import com.example.its.persistence.entity.Issue;
import com.example.its.persistence.repository.JpaRepositorySupport;
import com.example.its.shared.dto.issue.IssueSearchCondition;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IssueQueryRepository extends JpaRepositorySupport<Issue> {

    public IssueQueryRepository(EntityManager entityManager) {
        super(Issue.class, entityManager);
    }

    public List<Issue> search(IssueSearchCondition condition) {
        StringBuilder jpql = new StringBuilder("select i from Issue i where 1=1");
        Map<String, Object> parameters = new HashMap<>();

        if (condition.getProjectId() != null) {
            jpql.append(" and i.project.projectId = :projectId");
            parameters.put("projectId", condition.getProjectId());
        }
        if (condition.getStatus() != null) {
            jpql.append(" and i.status = :status");
            parameters.put("status", condition.getStatus());
        }
        if (condition.getPriority() != null) {
            jpql.append(" and i.priority = :priority");
            parameters.put("priority", condition.getPriority());
        }
        if (condition.getReporterAccountId() != null) {
            jpql.append(" and i.reporter.accountId = :reporterId");
            parameters.put("reporterId", condition.getReporterAccountId());
        }
        if (condition.getAssigneeAccountId() != null) {
            jpql.append(" and i.assignee.accountId = :assigneeId");
            parameters.put("assigneeId", condition.getAssigneeAccountId());
        }
        // 키워드 검색 (제목 또는 내용에 포함)
        if (condition.getKeyword() != null && !condition.getKeyword().trim().isEmpty()) {
            jpql.append(" and (i.title like :keyword or i.description like :keyword)");
            parameters.put("keyword", "%" + condition.getKeyword().trim() + "%");
        }

        // 최신 수정일 기준으로 정렬
        jpql.append(" order by i.lastModifiedAt desc");

        TypedQuery<Issue> query = entityManager.createQuery(jpql.toString(), Issue.class);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        return query.getResultList();
    }
}
