package com.mk.www.smsmonitor.transaction.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mk.www.smsmonitor.transaction.domain.Transaction.TransactionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TransactionCreateRequest {
    private BigDecimal amount;
    private String vendor;
    private LocalDateTime transactionTime;
    private TransactionType type;
    
    @JsonProperty("isFixedExpense")
    private boolean isFixedExpense;
    
    @JsonProperty("isStupidCost")
    private boolean isStupidCost;
    
    @JsonProperty("isIgnored")
    private boolean isIgnored;

    private boolean reflectInAsset;
    
    private String memo;
    private String category;
}
