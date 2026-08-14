package com.mk.www.smsmonitor.account.api.dto;

import com.mk.www.smsmonitor.account.infrastructure.AccountEntity;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AccountResponse {
    private Long id;
    private String bankName;
    private BigDecimal balance;
    private String accountNumber;
    private boolean isDefault;

    public static AccountResponse from(AccountEntity entity) {
        return AccountResponse.builder()
                .id(entity.getId())
                .bankName(entity.getBankName())
                .balance(entity.getBalance())
                .accountNumber(entity.getAccountNumber())
                .isDefault(entity.isDefault())
                .build();
    }
}
