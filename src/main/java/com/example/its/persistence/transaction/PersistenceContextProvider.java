package com.example.its.persistence.transaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

// TransactionManager에서 사용할 EntityManager를 제공
final class PersistenceContextProvider {

    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY =
        Persistence.createEntityManagerFactory("its-persistence-unit");

    private PersistenceContextProvider() {
    }

    static EntityManager createEntityManager() {
        return ENTITY_MANAGER_FACTORY.createEntityManager();
    }
}
