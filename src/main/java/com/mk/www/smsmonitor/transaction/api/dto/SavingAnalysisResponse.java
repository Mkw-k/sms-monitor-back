package com.mk.www.smsmonitor.transaction.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class SavingAnalysisResponse {
    private BigDecimal currentMonthAmount;
    private BigDecimal lastMonthSamePeriodAmount;
    private BigDecimal savingAmount; // (지난달 - 이번달) 양수면 절약
    private String message;
}
