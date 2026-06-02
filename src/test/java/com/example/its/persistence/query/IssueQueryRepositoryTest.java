package com.example.its.persistence.query;

import com.example.its.persistence.entity.Issue;
import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Priority;
import com.example.its.shared.dto.issue.IssueSearchCondition;
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

class IssueQueryRepositoryTest {

    private static EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;
    private IssueQueryRepository issueQueryRepository;

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
    void resetDatabaseAndSetUpRepository() {
        TestDatabaseManager.resetDatabase();
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        issueQueryRepository = new IssueQueryRepository(entityManager);
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
    void testSearchWithoutConditionReturnsAllIssuesOrderedByLastModifiedAtDesc() {
        List<Issue> issues = issueQueryRepository.search(new IssueSearchCondition());

        assertEquals(2, issues.size());
        assertEquals("Button typo", issues.get(0).getTitle());
        assertEquals("Login fails", issues.get(1).getTitle());
    }

    @Test
    void testSearchByStatusAndPriority() {
        IssueSearchCondition condition = new IssueSearchCondition();
        condition.setStatus(IssueStatus.NEW);
        condition.setPriority(Priority.MAJOR);

        List<Issue> issues = issueQueryRepository.search(condition);

        assertEquals(1, issues.size());
        assertEquals("Login fails", issues.get(0).getTitle());
    }

    @Test
    void testSearchByAssigneeAccountId() {
        IssueSearchCondition condition = new IssueSearchCondition();
        condition.setAssigneeAccountId(2L);

        List<Issue> issues = issueQueryRepository.search(condition);

        assertEquals(1, issues.size());
        assertEquals("Button typo", issues.get(0).getTitle());
    }

    @Test
    void testSearchByTrimmedKeywordInDescription() {
        IssueSearchCondition condition = new IssueSearchCondition();
        condition.setKeyword(" valid credentials ");

        List<Issue> issues = issueQueryRepository.search(condition);

        assertEquals(1, issues.size());
        assertEquals("Login fails", issues.get(0).getTitle());
    }
}
