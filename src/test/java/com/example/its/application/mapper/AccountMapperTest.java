package com.example.its.application.mapper;

import com.example.its.persistence.entity.Account;
import com.example.its.persistence.entity.Role;
import com.example.its.shared.dto.account.AccountResponse;
import com.example.its.support.ReflectionTestUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccountMapperTest {

    private final AccountMapper mapper = new AccountMapper();

    @Test
    void toResponseMapsCoreFields() {
        Account account = Account.create("tester1", "pw", "Tester One", "tester1@its.local", Role.TESTER);
        account.setActive(false);

        ReflectionTestUtils.setField(account, "accountId", 42);
        ReflectionTestUtils.setField(account, "createdAt", "2026-05-08T10:15:00");

        AccountResponse response = mapper.toResponse(account);

        assertNotNull(response);
        assertEquals(42L, response.getAccountId());
        assertEquals("tester1", response.getLoginId());
        assertEquals("Tester One", response.getName());
        assertEquals("tester1@its.local", response.getEmail());
        assertEquals(Role.TESTER, response.getRole());
        assertEquals(LocalDateTime.of(2026, 5, 8, 10, 15), response.getCreatedAt());
        assertFalse(response.isActive());
    }

    @Test
    void toResponseListPreservesOrder() {
        Account first = Account.create("admin", "pw", "Admin", "admin@its.local", Role.ADMIN);
        Account second = Account.create("dev1", "pw", "Dev One", "dev1@its.local", Role.DEV);

        ReflectionTestUtils.setField(first, "accountId", 1);
        ReflectionTestUtils.setField(second, "accountId", 2);

        List<AccountResponse> responses = mapper.toResponseList(List.of(first, second));

        assertEquals(2, responses.size());
        assertEquals("admin", responses.get(0).getLoginId());
        assertEquals("dev1", responses.get(1).getLoginId());
    }
}
