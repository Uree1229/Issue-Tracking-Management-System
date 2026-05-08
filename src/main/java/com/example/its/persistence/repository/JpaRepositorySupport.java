package com.example.its.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;

import java.util.List;
import java.util.Optional;

public abstract class JpaRepositorySupport<T> {

    private final Class<T> entityClass;
    protected final EntityManager entityManager;

    protected JpaRepositorySupport(Class<T> entityClass, EntityManager entityManager) {
        this.entityClass = entityClass;
        this.entityManager = entityManager;
    }

    public Optional<T> findById(Long id) {
        return Optional.ofNullable(entityManager.find(entityClass, id));
    }

    public List<T> findAll() {
        return entityManager.createQuery(
            "select e from " + entityClass.getSimpleName() + " e",
            entityClass
        ).getResultList();
    }

    public T save(T entity) {
        requireActiveTransaction();

        PersistenceUnitUtil unitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        Object identifier = unitUtil.getIdentifier(entity);
        if (identifier == null) {
            entityManager.persist(entity);
            return entity;
        }
        return entityManager.merge(entity);
    }

    public void delete(T entity) {
        requireActiveTransaction();

        T attachedEntity = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(attachedEntity);
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }

    private void requireActiveTransaction() {
        if (!entityManager.getTransaction().isActive()) {
            throw new IllegalStateException("쓰기 작업은 TransactionManager.execute 안에서만 수행해야 합니다.");
        }
    }
}
