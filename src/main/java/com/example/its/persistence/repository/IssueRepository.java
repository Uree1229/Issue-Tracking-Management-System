package com.example.its.persistence.repository;

import com.example.its.persistence.entity.Issue;
import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Priority;
import jakarta.persistence.EntityManager;

import java.util.List;

public class IssueRepository extends JpaRepositorySupport<Issue> {

    public IssueRepository(EntityManager entityManager) {
        super(Issue.class, entityManager);
    }

    public List<Issue> findByProjectId(Long projectId) {
        return entityManager.createQuery(
                "select i from Issue i where i.project.projectId = :projectId order by i.reportedAt desc",
                Issue.class
            )
            .setParameter("projectId", toJpaId(projectId))
            .getResultList();
    }

    public List<Issue> findByReporterAccountId(Long accountId) {
        return entityManager.createQuery(
                "select i from Issue i where i.reporter.accountId = :accountId order by i.reportedAt desc",
                Issue.class
            )
            .setParameter("accountId", toJpaId(accountId))
            .getResultList();
    }

    public List<Issue> findByAssigneeAccountId(Long accountId) {
        return entityManager.createQuery(
                "select i from Issue i where i.assignee.accountId = :accountId order by i.lastModifiedAt desc",
                Issue.class
            )
            .setParameter("accountId", toJpaId(accountId))
            .getResultList();
    }

    public List<Issue> findByStatus(IssueStatus status) {
        return entityManager.createQuery(
                "select i from Issue i where i.status = :status order by i.lastModifiedAt desc",
                Issue.class
            )
            .setParameter("status", status)
            .getResultList();
    }

    public List<Issue> findByPriority(Priority priority) {
        return entityManager.createQuery(
                "select i from Issue i where i.priority = :priority order by i.reportedAt desc",
                Issue.class
            )
            .setParameter("priority", priority)
            .getResultList();
    }
}
