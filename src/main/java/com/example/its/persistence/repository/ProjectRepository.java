package com.example.its.persistence.repository;

import com.example.its.persistence.entity.Project;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class ProjectRepository extends JpaRepositorySupport<Project> {

    public ProjectRepository(EntityManager entityManager) {
        super(Project.class, entityManager);
    }

    public Optional<Project> findByName(String name) {
        return entityManager.createQuery(
                "select p from Project p where p.name = :name",
                Project.class
            )
            .setParameter("name", name)
            .getResultStream()
            .findFirst();
    }

    public List<Project> findByCreatedByAccountId(Long accountId) {
        return entityManager.createQuery(
                "select p from Project p where p.createdBy.accountId = :accountId order by p.createdAt desc",
                Project.class
            )
            .setParameter("accountId", toJpaId(accountId))
            .getResultList();
    }
}
