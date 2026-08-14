package com.mk.www.smsmonitor.account.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AccountRequest {
    private String bankName;
    private BigDecimal balance;
    private String accountNumber;
    private boolean isDefault;
}
