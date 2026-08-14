package com.mk.www.smsmonitor.transaction.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SavingsAnalysisResponse {
    private BigDecimal income;               // 총 수입
    private BigDecimal fixedExpense;         // 고정 지출
    private BigDecimal variableExpense;      // 변동 지출
    private BigDecimal stupidExpense;        // 멍청 비용
    private BigDecimal maxPossibleSavings;   // 최대 저축 가능 금액 (수입 - 고정지출)
    private BigDecimal currentSavings;       // 현재 저축액 (수입 - 총지출)
    private BigDecimal targetSavings;        // 목표 저축액
    private BigDecimal gap;                  // 목표와의 차이
    private String recommendation;           // 분석 가이드 메시지
}
