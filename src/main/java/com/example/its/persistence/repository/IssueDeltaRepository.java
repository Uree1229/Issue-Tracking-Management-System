package com.example.its.persistence.repository;

import com.example.its.persistence.entity.IssueDelta;
import jakarta.persistence.EntityManager;

import java.util.Optional;

public class IssueDeltaRepository extends JpaRepositorySupport<IssueDelta> {

    public IssueDeltaRepository() {
        super(IssueDelta.class);
    }

    public IssueDeltaRepository(EntityManager entityManager) {
        super(IssueDelta.class, entityManager);
    }

    public Optional<IssueDelta> findByHistoryId(Long historyId) {
        return entityManager.createQuery(
                "select d from IssueDelta d where d.issueHistory.historyId = :historyId",
                IssueDelta.class
            )
            .setParameter("historyId", historyId)
            .getResultStream()
            .findFirst();
    }
}
