package com.example.its.persistence.repository;

import com.example.its.persistence.entity.Tag;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class TagRepository extends JpaRepositorySupport<Tag> {

    public TagRepository(EntityManager entityManager) {
        super(Tag.class, entityManager);
    }

    public List<Tag> findByProjectId(Long projectId) {
        return entityManager.createQuery(
                "select t from Tag t where t.project.projectId = :projectId order by t.name",
                Tag.class
            )
            .setParameter("projectId", projectId)
            .getResultList();
    }

    public Optional<Tag> findByProjectIdAndName(Long projectId, String name) {
        return entityManager.createQuery(
                "select t from Tag t where t.project.projectId = :projectId and t.name = :name",
                Tag.class
            )
            .setParameter("projectId", projectId)
            .setParameter("name", name)
            .getResultStream()
            .findFirst();
    }
}
