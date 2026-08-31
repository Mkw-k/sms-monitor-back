package com.mk.www.smsmonitor.transaction.domain;

import com.mk.www.smsmonitor.transaction.api.dto.SavingsAnalysisResponse;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class SavingsPlan {

    private final BigDecimal income;
    private final BigDecimal fixedExpense;
    private final BigDecimal variableExpense;
    private final BigDecimal stupidExpense;
    private final BigDecimal targetSavings;

    public SavingsPlan(BigDecimal income, BigDecimal fixedExpense, BigDecimal variableExpense, BigDecimal stupidExpense, BigDecimal targetSavings) {
        this.income = income != null ? income : BigDecimal.ZERO;
        this.fixedExpense = fixedExpense != null ? fixedExpense : BigDecimal.ZERO;
        this.variableExpense = variableExpense != null ? variableExpense : BigDecimal.ZERO;
        this.stupidExpense = stupidExpense != null ? stupidExpense : BigDecimal.ZERO;
        this.targetSavings = targetSavings != null ? targetSavings : BigDecimal.ZERO;
    }

    public BigDecimal calculateMaxPossibleSavings() {
        return this.income.subtract(this.fixedExpense);
    }

    public BigDecimal calculateCurrentSavings() {
        return this.income.subtract(this.fixedExpense).subtract(this.variableExpense);
    }

    public BigDecimal calculateGap() {
        return this.targetSavings.subtract(calculateCurrentSavings());
    }

    public String generateRecommendation() {
        BigDecimal gap = calculateGap();
        if (gap.compareTo(BigDecimal.ZERO) <= 0) {
            return "훌륭합니다! 저축 목표를 달성했습니다.";
        }
        return "목표를 위해 지출을 " + gap + "원 더 줄여야 합니다.";
    }

    public SavingsAnalysisResponse toResponse() {
        return SavingsAnalysisResponse.builder()
                .income(this.income)
                .fixedExpense(this.fixedExpense)
                .variableExpense(this.variableExpense)
                .stupidExpense(this.stupidExpense)
                .maxPossibleSavings(calculateMaxPossibleSavings())
                .currentSavings(calculateCurrentSavings())
                .targetSavings(this.targetSavings)
                .gap(calculateGap())
                .recommendation(generateRecommendation())
                .build();
    }
}
