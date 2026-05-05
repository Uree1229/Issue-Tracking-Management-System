package com.example.its.application.facade;

import com.example.its.application.service.AccountService;
import com.example.its.application.mapper.AccountMapper;
import com.example.its.persistence.repository.AccountRepository;
import com.example.its.shared.dto.account.AccountCreateRequest;
import com.example.its.shared.dto.account.AccountResponse;
import com.example.its.shared.dto.account.AccountUpdateRequest;

import java.util.List;

public class AccountFacade {

    private final AccountService accountService;

    // Fix: 파라미터 없는 기본 생성자로 변경 (5/4 피드백 반영)
    public AccountFacade() {
        AccountRepository accountRepository = new AccountRepository();
        AccountMapper accountMapper = new AccountMapper();
        this.accountService = new AccountService(accountRepository, accountMapper);
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