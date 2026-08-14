package com.mk.www.smsmonitor.transaction.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TransactionSummaryResponse {
    private BigDecimal monthlyTotalAmount;
    private BigDecimal monthlyTotalIncome; // 추가된 필드
    private BigDecimal monthlyStupidCostAmount;
    private String month;
}
