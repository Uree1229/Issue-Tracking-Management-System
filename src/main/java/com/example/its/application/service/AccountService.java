package com.example.its.application.service;

import com.example.its.persistence.entity.Account;
import com.example.its.persistence.entity.Role;
import com.example.its.persistence.repository.AccountRepository;
import com.example.its.shared.dto.account.AccountCreateRequest;

import java.util.List;

public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // 1. 계정 인증 (Login)
    public Account authenticate(String loginId, String password) {
        // 1. 아이디 존재 여부 확인
        Account account = accountRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        // 2. 비밀번호 일치 여부 대조 (평문 비교)
        if (!account.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 활성화 상태 검증
        if (!account.isActive()) {
            throw new IllegalStateException("비활성화된 계정입니다. 관리자에게 문의하세요.");
        }

        return account;
    }

    // 2. 계정 생성 (Register)
    public Account createAccount(AccountCreateRequest request) {
        // 1. 로그인 아이디 중복 검증
        if (accountRepository.findByLoginId(request.getLoginId()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 로그인 아이디입니다.");
        }

        // 2. 이메일 중복 검증
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 3. 엔티티 생성 및 데이터 매핑
        // 04-29 회의내용에 따라 static factory method 사용하도록 수정
        Account account = Account.create(
            request.getLoginId(), 
            request.getPassword(), 
            request.getName(), 
            request.getEmail(), 
            request.getRole() != null ? request.getRole() : Role.DEV // 기본값: DEV
        );

        return accountRepository.save(account);
    }

    // 3. 계정 정보 수정 (Update)
    public Account updateAccountInfo(Long accountId, String newName, String newEmail, Role newRole) {
        Account account = getAccountOrThrow(accountId);

        // 이메일이 변경되었을 경우 중복 검증
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

        return accountRepository.save(account);
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
    public Account getAccount(Long accountId) {
        return getAccountOrThrow(accountId);
    }

    public List<Account> getAllActiveAccounts() {
        return accountRepository.findActiveAccounts();
    }

    public List<Account> getAccountsByRole(Role role) {
        return accountRepository.findByRole(role);
    }

    // 내부 헬퍼 메서드
    private Account getAccountOrThrow(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다. ID: " + accountId));
    }
}