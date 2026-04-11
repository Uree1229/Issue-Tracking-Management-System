package com.example.its.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceUnitUtil;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class JpaRepositorySupport<T> implements AutoCloseable {

    private final Class<T> entityClass;
    protected final EntityManager entityManager;

    protected JpaRepositorySupport(Class<T> entityClass) {
        this(entityClass, PersistenceContextProvider.createEntityManager());
    }

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
        return executeInTransaction(manager -> {
            PersistenceUnitUtil unitUtil = manager.getEntityManagerFactory().getPersistenceUnitUtil();
            Object identifier = unitUtil.getIdentifier(entity);
            if (identifier == null) {
                manager.persist(entity);
                return entity;
            }
            return manager.merge(entity);
        });
    }

    public void delete(T entity) {
        executeInTransaction(manager -> {
            T attachedEntity = manager.contains(entity) ? entity : manager.merge(entity);
            manager.remove(attachedEntity);
            return null;
        });
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }

    protected <R> R executeInTransaction(Function<EntityManager, R> action) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            R result = action.apply(entityManager);
            transaction.commit();
            return result;
        } catch (RuntimeException exception) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw exception;
        }
    }

    @Override
    public void close() {
        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }
}
