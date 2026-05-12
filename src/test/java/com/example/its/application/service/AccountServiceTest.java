package com.example.its.application.service;

import com.example.its.application.mapper.AccountMapper;
import com.example.its.persistence.entity.Role;
import com.example.its.shared.dto.account.AccountCreateRequest;
import com.example.its.shared.dto.account.AccountResponse;
import com.example.its.util.TestDatabaseManager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountServiceTest {

    private static AccountService accountService;

    @BeforeAll
    static void setUp() {
        TestDatabaseManager.resetDatabase();
        accountService = new AccountService(new AccountMapper());
    }

    @BeforeEach
    void resetDatabaseBeforeEachTest() {
        TestDatabaseManager.resetDatabase();
    }

    @Test
    void testLoginSuccess() {
        AccountResponse response = accountService.authenticate("dev1", "password");

        assertEquals("dev1", response.getLoginId());
        assertEquals(Role.DEV, response.getRole());
    }


    @Test
    void testCreateAccountWithDuplicateLoginId() {
        AccountCreateRequest request = new AccountCreateRequest(
            "dev1",
            "password",
            "Duplicate Dev",
            "duplicate@its.test",
            Role.DEV
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> accountService.createAccount(request)
        );
    }
}
