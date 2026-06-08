package com.example.its.persistence.transaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.function.Consumer;
import java.util.function.Function;


public final class TransactionManager {

    private TransactionManager() {
    }

    public static <R> R execute(Function<EntityManager, R> action) {
        try (EntityManager entityManager = PersistenceContextProvider.createEntityManager()) {
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
    }


    public static void executeVoid(Consumer<EntityManager> action) {
        execute(entityManager -> {
            action.accept(entityManager);
            return null;
        });
    }
}
