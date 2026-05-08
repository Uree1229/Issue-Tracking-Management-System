package com.example.its.persistence.repository;

import com.example.its.persistence.entity.Comment;
import jakarta.persistence.EntityManager;

import java.util.List;

public class CommentRepository extends JpaRepositorySupport<Comment> {

    public CommentRepository(EntityManager entityManager) {
        super(Comment.class, entityManager);
    }

    public List<Comment> findByIssueId(Long issueId) {
        return entityManager.createQuery(
                "select c from Comment c where c.issue.issueId = :issueId order by c.createdAt asc",
                Comment.class
            )
            .setParameter("issueId", issueId)
            .getResultList();
    }

    public List<Comment> findByAuthorAccountId(Long accountId) {
        return entityManager.createQuery(
                "select c from Comment c where c.author.accountId = :accountId order by c.createdAt desc",
                Comment.class
            )
            .setParameter("accountId", accountId)
            .getResultList();
    }
}
