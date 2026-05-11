package com.example.its.persistence.repository;

import com.example.its.persistence.entity.Project;
import com.example.its.persistence.entity.ProjectMember;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ProjectMemberRepository extends JpaRepositorySupport<ProjectMember> {

    public ProjectMemberRepository(EntityManager entityManager) {
        super(ProjectMember.class, entityManager);
    }

    public List<ProjectMember> findByProjectId(Long projectId) {
        return entityManager.createQuery(
                "select pm from ProjectMember pm where pm.project.projectId = :projectId order by pm.account.name",
                ProjectMember.class
            )
            .setParameter("projectId", projectId)
            .getResultList();
    }

    public List<Project> findProjectsByAccountId(Long accountId) {
        return entityManager.createQuery(
                "select pm.project from ProjectMember pm where pm.account.accountId = :accountId order by pm.project.name",
                Project.class
            )
            .setParameter("accountId", accountId)
            .getResultList();
    }

    public void deleteByProjectId(Long projectId) {
        entityManager.createQuery(
                "delete from ProjectMember pm where pm.project.projectId = :projectId"
            )
            .setParameter("projectId", projectId)
            .executeUpdate();
    }
}
