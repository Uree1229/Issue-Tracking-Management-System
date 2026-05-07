package com.example.its.persistence.repository;

import com.example.its.persistence.entity.Account;
import com.example.its.persistence.entity.Role;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class AccountRepository extends JpaRepositorySupport<Account> {

    public AccountRepository(EntityManager entityManager) {
        super(Account.class, entityManager);
    }

    public Optional<Account> findByLoginId(String loginId) {
        return entityManager.createQuery(
                "select a from Account a where a.loginId = :loginId",
                Account.class
            )
            .setParameter("loginId", loginId)
            .getResultStream()
            .findFirst();
    }

    public Optional<Account> findByEmail(String email) {
        return entityManager.createQuery(
                "select a from Account a where a.email = :email",
                Account.class
            )
            .setParameter("email", email)
            .getResultStream()
            .findFirst();
    }

    public List<Account> findByRole(Role role) {
        return entityManager.createQuery(
                "select a from Account a where a.role = :role order by a.name",
                Account.class
            )
            .setParameter("role", role)
            .getResultList();
    }

    public List<Account> findActiveAccounts() {
        return entityManager.createQuery(
                "select a from Account a where a.isActive = true order by a.name",
                Account.class
            )
            .getResultList();
    }
}
