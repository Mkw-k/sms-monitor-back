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
public class TransactionUpdateRequest {
    private BigDecimal amount;
    private String vendor;
    private LocalDateTime transactionTime;
    
    @JsonProperty("isStupidCost")
    private Boolean isStupidCost;
    
    @JsonProperty("isFixedExpense")
    private Boolean isFixedExpense;
    
    @JsonProperty("isIgnored")
    private Boolean isIgnored;

    @JsonProperty("isDeleted")
    private Boolean isDeleted;

    private Boolean reflectInAsset;
    
    private String memo;
    private TransactionType type;
}
