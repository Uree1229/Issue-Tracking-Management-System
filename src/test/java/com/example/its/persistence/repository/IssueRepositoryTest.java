package com.example.its.persistence.repository;

import com.example.its.persistence.entity.Issue;
import com.example.its.util.TestDatabaseManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IssueRepositoryTest {

    private static EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;
    private IssueRepository issueRepository;

    @BeforeAll
    static void createEntityManagerFactory() {
        entityManagerFactory = Persistence.createEntityManagerFactory("its-persistence-unit");
    }

    @AfterAll
    static void closeEntityManagerFactory() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    @BeforeEach
    void resetDatabaseAndSetUpEntityManagerAndRepository() {
        TestDatabaseManager.resetDatabase();
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        issueRepository = new IssueRepository(entityManager);
    }

    @AfterEach
    void closeEntityManagerAfterEachTest() {
        if (entityManager != null) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }

    @Test
    void testFindByProjectId() {
        List<Issue> issues = issueRepository.findByProjectId(1L);

        assertEquals(2, issues.size());
        assertEquals("Button typo", issues.get(0).getTitle());
        assertEquals("Login fails", issues.get(1).getTitle());
    }

    @Test
    void testFindByProjectIdAndTagId() {
        List<Issue> issues = issueRepository.findByProjectIdAndTagId(1L, 1L);

        assertEquals(1, issues.size());
        assertEquals("Login fails", issues.get(0).getTitle());
    }
}
