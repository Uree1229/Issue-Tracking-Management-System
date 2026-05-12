package com.example.its.persistence.repository;

import com.example.its.persistence.entity.Tag;
import com.example.its.util.TestDatabaseManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// 유닛 테스트인데 EntityManager를 직접 생성하면 안됨.
// TODO: Stub을 만들어서 테스트 DB에 접근하지 않도록 리팩토링 필요
class TagRepositoryTest {

    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY =
        Persistence.createEntityManagerFactory("its-persistence-unit");

    private EntityManager entityManager;
    private TagRepository tagRepository;

    @BeforeEach
    void resetDatabaseBeforeEachTest() {
        // 테스트 DB 초기화
        TestDatabaseManager.resetDatabase();

        // EntityManager 생성 및 TagRepository 초기화
        entityManager = ENTITY_MANAGER_FACTORY.createEntityManager();
        entityManager.getTransaction().begin();
        tagRepository = new TagRepository(entityManager);
    }

    @AfterEach
    void closeEntityManagerAfterEachTest() {
        // 항상 트랜잭션 롤백 및 EntityManager 종료
        if (entityManager != null) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }

    @Test
    void testFindByProjectId() {
        List<Tag> tags = tagRepository.findByProjectId(1L);

        assertEquals(2, tags.size());
        assertEquals("backend", tags.get(0).getName());
        assertEquals("ui", tags.get(1).getName());
    }

    @Test
    void testFindByProjectIdAndName() {
        Optional<Tag> tag = tagRepository.findByProjectIdAndName(1L, "backend");

        assertTrue(tag.isPresent());
        assertEquals(1L, tag.get().getTagId());
        assertEquals("Backend issues", tag.get().getDescription());
    }

    @Test
    void testExistsByProjectIdAndName() {
        Boolean exists = tagRepository.existsByProjectIdAndName(1L, "ui");

        assertTrue(exists);
    }

    @Test
    void testDeleteTagUsedByIssue() {
        Tag tag = tagRepository.findById(1L)
            .orElseThrow();

        assertThrows(
            IllegalStateException.class,
            () -> tagRepository.delete(tag)
        );
    }
}
