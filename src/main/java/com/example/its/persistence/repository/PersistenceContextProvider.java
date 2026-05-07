package com.example.its.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

final class PersistenceContextProvider {

    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY =
        Persistence.createEntityManagerFactory("its-persistence-unit");
    private static EntityManager sharedEntityManager;

    private PersistenceContextProvider() {
    }

    static synchronized EntityManager createEntityManager() {
        if (sharedEntityManager == null || !sharedEntityManager.isOpen()) {
            sharedEntityManager = ENTITY_MANAGER_FACTORY.createEntityManager();
        }
        return sharedEntityManager;
    }
}
