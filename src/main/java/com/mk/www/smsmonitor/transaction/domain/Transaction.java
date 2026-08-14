package com.mk.www.smsmonitor.transaction.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class Transaction {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String vendor;
    private LocalDateTime transactionTime;
    private String category;
    private boolean isStupidCost;
    private String originalSmsContent;
    private String memo;
    private String cardNumber;
    private String name;

    private TransactionType type;
    private boolean isFixedExpense;
    private boolean isManual;
    private boolean isIgnored;
    private boolean isDeleted;
    private boolean reflectInAsset;

    public enum TransactionType {
        INCOME, EXPENSE
    }

    public void analyze(List<StupidCostStrategy> strategies) {
        if (this.type == TransactionType.INCOME || this.isIgnored || this.isDeleted) {
            this.isStupidCost = false;
            return;
        }
        for (StupidCostStrategy strategy : strategies) {
            if (strategy.isStupidCost(this)) {
                this.isStupidCost = true;
                return;
            }
        }
        this.isStupidCost = false;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void updateStupidCost(boolean isStupidCost) {
        this.isStupidCost = isStupidCost;
    }

    public void updateFixedExpense(boolean isFixedExpense) {
        this.isFixedExpense = isFixedExpense;
    }

    public void updateIgnored(boolean isIgnored) {
        this.isIgnored = isIgnored;
    }

    public void updateDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
