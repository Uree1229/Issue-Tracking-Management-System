package com.example.its.persistence.repository;

import com.example.its.persistence.entity.IssueHistory;
import jakarta.persistence.EntityManager;

import java.util.List;

public class IssueHistoryRepository extends JpaRepositorySupport<IssueHistory> {

    public IssueHistoryRepository(EntityManager entityManager) {
        super(IssueHistory.class, entityManager);
    }

    public List<IssueHistory> findByIssueId(Long issueId) {
        return entityManager.createQuery(
                "select h from IssueHistory h where h.issue.issueId = :issueId order by h.changedAt desc",
                IssueHistory.class
            )
            .setParameter("issueId", toJpaId(issueId))
            .getResultList();
    }

    public List<IssueHistory> findByChangedByAccountId(Long accountId) {
        return entityManager.createQuery(
                "select h from IssueHistory h where h.changedBy.accountId = :accountId order by h.changedAt desc",
                IssueHistory.class
            )
            .setParameter("accountId", toJpaId(accountId))
            .getResultList();
    }
}
