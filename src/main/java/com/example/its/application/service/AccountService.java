package com.example.its.application.service;

import com.example.its.application.mapper.AccountMapper;
import com.example.its.persistence.entity.Account;
import com.example.its.persistence.entity.Role;
import com.example.its.persistence.repository.AccountRepository;
import com.example.its.persistence.transaction.TransactionManager;
import com.example.its.shared.dto.account.AccountCreateRequest;
import com.example.its.shared.dto.account.AccountResponse;

import java.util.List;

// Fix: Service에 AccountMapper를 주입하고, 반환 타입을 모두 AccountResponse로 변경
public class AccountService {

    private final AccountMapper accountMapper;

    public AccountService(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    // 1. 계정 인증 (Login)
    public AccountResponse authenticate(String loginId, String password) {
        return TransactionManager.execute(entityManager -> {
            AccountRepository accountRepository = new AccountRepository(entityManager);
            
            // NOTE: 수동으로 null 체크를 하기 귀찮고 까먹을수도 있으니,
            // 아예 처음부터 ID 존재여부 검사는 .orElseThrow를 사용
            // PW 일치하는지의 여부와 활성화 여부는 null 체크가 아닌 순수 비즈니스 로직이므로 if-throw 사용
            Account account = accountRepository.findByLoginId(loginId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));
            if (!account.getPassword().equals(password)) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
            if (!account.isActive()) {
                throw new IllegalStateException("비활성화된 계정입니다. 관리자에게 문의하세요.");
            }
            return accountMapper.toResponse(account);
        });
    }

    // 2. 계정 생성 (Register)
    public AccountResponse createAccount(AccountCreateRequest request) {
        return TransactionManager.execute(entityManager -> {
            AccountRepository accountRepository = new AccountRepository(entityManager);

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
        });
    }

    // 3. 계정 정보 수정 (Update)
    public AccountResponse updateAccountInfo(Long accountId, String newName, String newEmail, Role newRole) {
        return TransactionManager.execute(entityManager -> {
            AccountRepository accountRepository = new AccountRepository(entityManager);
            Account account = getAccountOrThrow(accountRepository, accountId);

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
        });
    }

    // 4. 계정 상태 관리 (Deactivate)
    public void deactivateAccount(Long accountId) {
        TransactionManager.executeVoid(entityManager -> {
            AccountRepository accountRepository = new AccountRepository(entityManager);
            Account account = getAccountOrThrow(accountRepository, accountId);

            if ("admin".equals(account.getLoginId())) {
                throw new SecurityException("기본 관리자(admin) 계정은 비활성화할 수 없습니다.");
            }
            account.setActive(false);
            accountRepository.save(account);
        });
    }

    // 5. 조회 및 유틸리티 (Fetchers & Helpers)
    // NOTE: 단순 조회일지라도 AccountService 전체와 아키텍쳐를 동일하게 맞추고,
    // 영속성 컨텍스트(세션)이 유지되어야 Lazy Loading이 발생하지 않아 Connection Pool을 안전하게 반환할 수 있으므로
    // 일부러 전부 TransactionManager 통하여 실행되도록 강제
    public AccountResponse getAccount(Long accountId) {
        return TransactionManager.execute(entityManager -> {
            AccountRepository accountRepository = new AccountRepository(entityManager);
            return accountMapper.toResponse(getAccountOrThrow(accountRepository, accountId));
        });
    }

    public List<AccountResponse> getAllActiveAccounts() {
        return TransactionManager.execute(entityManager -> {
            AccountRepository accountRepository = new AccountRepository(entityManager);
            return accountMapper.toResponseList(accountRepository.findActiveAccounts());
        });
    }

    public List<AccountResponse> getAccountsByRole(Role role) {
        return TransactionManager.execute(entityManager -> {
            AccountRepository accountRepository = new AccountRepository(entityManager);
            return accountMapper.toResponseList(accountRepository.findByRole(role));
        });
    }

    // NOTE: ID로 엔티티를 찾고, 만약 아이디가 없으면 Error를 던지는 작업은 반복되므로 따로 빼서 처리
    // NOTE: 이건 DTO가 아니라 순수 Entity이므로 외부에서 조회못하도록 Private으로 선언
    private Account getAccountOrThrow(AccountRepository accountRepository, Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다. ID: " + accountId));
    }
}
