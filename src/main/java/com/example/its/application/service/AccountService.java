package com.example.its.application.service;

import com.example.its.application.mapper.AccountMapper;
import com.example.its.persistence.entity.Account;
import com.example.its.persistence.entity.Role;
import com.example.its.persistence.repository.AccountRepository;
import com.example.its.shared.dto.account.AccountCreateRequest;
import com.example.its.shared.dto.account.AccountResponse;

import java.util.List;

// Fix: Service에 AccountMapper를 주입하고, 반환 타입을 모두 AccountResponse로 변경
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    // 1. 계정 인증 (Login)
    public AccountResponse authenticate(String loginId, String password) {
        Account account = accountRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));
        if (!account.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        if (!account.isActive()) {
            throw new IllegalStateException("비활성화된 계정입니다. 관리자에게 문의하세요.");
        }
        return accountMapper.toResponse(account);
    }

    // 2. 계정 생성 (Register)
    public AccountResponse createAccount(AccountCreateRequest request) {
        if (accountRepository.findByLoginId(request.getLoginId()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 로그인 아이디입니다.");
        }
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        
        Account account = Account.create(
            request.getLoginId(), 
            request.getPassword(), 
            request.getName(), 
            request.getEmail(), 
            request.getRole() != null ? request.getRole() : Role.DEV
        );
        return accountMapper.toResponse(accountRepository.save(account));
    }

    // 3. 계정 정보 수정 (Update)
    public AccountResponse updateAccountInfo(Long accountId, String newName, String newEmail, Role newRole) {
        Account account = getAccountOrThrow(accountId);

        if (newEmail != null && !newEmail.equals(account.getEmail())) {
            if (accountRepository.findByEmail(newEmail).isPresent()) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
            account.setEmail(newEmail);
        }
        if (newName != null && !newName.trim().isEmpty()) {
            account.setName(newName);
        }
        if (newRole != null) {
            account.setRole(newRole);
        }
        return accountMapper.toResponse(accountRepository.save(account));
    }

    // 4. 계정 상태 관리 (Deactivate)
    public void deactivateAccount(Long accountId) {
        Account account = getAccountOrThrow(accountId);
        if ("admin".equals(account.getLoginId())) {
            throw new SecurityException("기본 관리자(admin) 계정은 비활성화할 수 없습니다.");
        }
        account.setActive(false);
        accountRepository.save(account);
    }

    // 5. 조회 및 유틸리티 (Fetchers & Helpers)
    public AccountResponse getAccount(Long accountId) {
        return accountMapper.toResponse(getAccountOrThrow(accountId));
    }

    public List<AccountResponse> getAllActiveAccounts() {
        return accountMapper.toResponseList(accountRepository.findActiveAccounts());
    }

    public List<AccountResponse> getAccountsByRole(Role role) {
        return accountMapper.toResponseList(accountRepository.findByRole(role));
    }

    private Account getAccountOrThrow(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다. ID: " + accountId));
    }
}