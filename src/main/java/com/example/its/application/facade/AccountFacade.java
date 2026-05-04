package com.example.its.application.facade;

import com.example.its.application.service.AccountService;
import com.example.its.shared.dto.account.AccountCreateRequest;
import com.example.its.shared.dto.account.AccountResponse;
import com.example.its.shared.dto.account.AccountUpdateRequest;

import java.util.List;

// Fix: Mapper를 없애버리고, Service가 준 DTO를 그대로 리턴하도록 수정
public class AccountFacade {

    private final AccountService accountService;

    public AccountFacade(AccountService accountService) {
        this.accountService = accountService;
    }

    public AccountResponse login(String loginId, String password) {
        return accountService.authenticate(loginId, password);
    }

    public AccountResponse register(AccountCreateRequest request) {
        return accountService.createAccount(request);
    }

    public AccountResponse updateAccount(Long id, AccountUpdateRequest request) {
        return accountService.updateAccountInfo(id, request.getName(), request.getEmail(), null);
    }

    public List<AccountResponse> getActiveAccounts() {
        return accountService.getAllActiveAccounts();
    }
}