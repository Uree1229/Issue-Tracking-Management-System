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
            .setParameter("projectId", toJpaId(projectId))
            .getResultList();
    }

    public Optional<Tag> findByProjectIdAndTagId(Long projectId, Long tagId) {
        return entityManager.createQuery(
                "select t from Tag t where t.project.projectId = :projectId and t.tagId = :tagId",
                Tag.class
            )
            .setParameter("projectId", toJpaId(projectId))
            .setParameter("tagId", toJpaId(tagId))
            .getResultStream()
            .findFirst();
    }

    public Optional<Tag> findByProjectIdAndName(Long projectId, String name) {
        return entityManager.createQuery(
                "select t from Tag t where t.project.projectId = :projectId and t.name = :name",
                Tag.class
            )
            .setParameter("projectId", toJpaId(projectId))
            .setParameter("name", name)
            .getResultStream()
            .findFirst();
    }

    public boolean existsByProjectIdAndName(Long projectId, String name) {
        Long count = entityManager.createQuery(
                "select count(t) from Tag t where t.project.projectId = :projectId and t.name = :name",
                Long.class
            )
            .setParameter("projectId", toJpaId(projectId))
            .setParameter("name", name)
            .getSingleResult();
        return count > 0;
    }

    public boolean existsIssueUsingTag(Long tagId) {
        Long count = entityManager.createQuery(
                "select count(i) from Issue i join i.tags t where t.tagId = :tagId",
                Long.class
            )
            .setParameter("tagId", toJpaId(tagId))
            .getSingleResult();
        return count > 0;
    }

    @Override
    public void delete(Tag tag) {
        if (tag != null && existsIssueUsingTag(tag.getTagId())) {
            throw new IllegalStateException("이슈에서 사용 중인 태그는 삭제할 수 없습니다.");
        }
        super.delete(tag);
    }
}
