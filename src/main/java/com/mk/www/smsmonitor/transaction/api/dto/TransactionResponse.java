package com.mk.www.smsmonitor.transaction.api.dto;

import com.mk.www.smsmonitor.transaction.domain.Transaction;
import com.mk.www.smsmonitor.transaction.domain.Transaction.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private String vendor;
    private LocalDateTime transactionTime;
    private String categoryName;
    private String memo;
    private boolean stupidCost;
    private TransactionType type;
    private boolean fixedExpense;
    private boolean manual;
    private boolean ignored; // 추가됨
    private String name;
    private String expenseType;

    public static TransactionResponse from(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .vendor(transaction.getVendor())
                .transactionTime(transaction.getTransactionTime())
                .categoryName(transaction.getCategory())
                .memo(transaction.getMemo())
                .stupidCost(transaction.isStupidCost())
                .type(transaction.getType())
                .fixedExpense(transaction.isFixedExpense())
                .manual(transaction.isManual())
                .ignored(transaction.isIgnored()) // 추가됨
                .name(transaction.getName())
                .expenseType(transaction.getType() != null ? transaction.getType().name() : null)
                .build();    }
}
