package com.example.its.application.mapper;

import com.example.its.persistence.entity.Account;
import com.example.its.shared.dto.account.AccountResponse;
import java.util.List;
import java.util.stream.Collectors;

public class AccountMapper {
    
    // Entity -> Response DTO 변환
    public AccountResponse toResponse(Account account) {
        if (account == null) return null;
        
        return new AccountResponse(
            account.getAccountId(),
            account.getLoginId(),
            account.getName(),
            account.getEmail(),
            account.getRole(),
            account.getCreatedAt(),
            account.isActive()
        );
    }

    // Entity 리스트 -> Response DTO 리스트 변환
    public List<AccountResponse> toResponseList(List<Account> accounts) {
        return accounts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}