package com.example.its.application.facade;

import com.example.its.application.mapper.AccountMapper;
import com.example.its.application.service.AccountService;
import com.example.its.persistence.entity.Account;
import com.example.its.shared.dto.account.AccountCreateRequest;
import com.example.its.shared.dto.account.AccountResponse;
import com.example.its.shared.dto.account.AccountUpdateRequest;

import java.util.List;

public class AccountFacade {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public AccountFacade(AccountService accountService, AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }

    public AccountResponse login(String loginId, String password) {
        Account account = accountService.authenticate(loginId, password);
        return accountMapper.toResponse(account);
    }

    public AccountResponse register(AccountCreateRequest request) {
        Account account = accountService.createAccount(request);
        return accountMapper.toResponse(account);
    }

    public AccountResponse updateAccount(Long id, AccountUpdateRequest request) {
        // DTO에 권한(Role) 변경 정보가 없으므로 null을 전달하여 기존 권한을 유지
        Account account = accountService.updateAccountInfo(id, request.getName(), request.getEmail(), null);
        return accountMapper.toResponse(account);
    }

    public List<AccountResponse> getActiveAccounts() {
        List<Account> accounts = accountService.getAllActiveAccounts();
        return accountMapper.toResponseList(accounts);
    }
}