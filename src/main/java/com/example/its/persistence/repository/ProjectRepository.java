package com.example.its.persistence.repository;

import com.example.its.persistence.entity.Project;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class ProjectRepository extends JpaRepositorySupport<Project> {

    public ProjectRepository(EntityManager entityManager) {
        super(Project.class, entityManager);
    }

    @Override
    public Project save(Project project) {
        Object identifier = entityManager.getEntityManagerFactory()
            .getPersistenceUnitUtil()
            .getIdentifier(project);
        if (identifier != null) {
            throw new IllegalStateException("프로젝트는 생성 후 수정할 수 없습니다.");
        }
        return super.save(project);
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
