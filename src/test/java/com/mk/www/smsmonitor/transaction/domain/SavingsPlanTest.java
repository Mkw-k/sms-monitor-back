package com.mk.www.smsmonitor.transaction.domain;

import com.mk.www.smsmonitor.transaction.api.dto.SavingsAnalysisResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SavingsPlanTest {

    @Test
    @DisplayName("최대 가능 저축액과 현재 저축액을 정확히 계산한다")
    void calculateSavings_Success() {
        // given: 수입 300만, 고정지출 100만, 변동지출 50만, 멍청비용 10만, 목표 120만
        SavingsPlan plan = new SavingsPlan(
                new BigDecimal("3000000"),
                new BigDecimal("1000000"),
                new BigDecimal("500000"),
                new BigDecimal("100000"),
                new BigDecimal("1200000")
        );

        // then
        // 최대 가능 = 300만 - 100만 = 200만
        assertThat(plan.calculateMaxPossibleSavings()).isEqualByComparingTo(new BigDecimal("2000000"));
        // 현재 저축액 = 300만 - 100만 - 50만 = 150만
        assertThat(plan.calculateCurrentSavings()).isEqualByComparingTo(new BigDecimal("1500000"));
        // 목표와의 차이 = 120만 - 150만 = -30만 (초과 달성)
        assertThat(plan.calculateGap()).isEqualByComparingTo(new BigDecimal("-300000"));
        assertThat(plan.generateRecommendation()).contains("훌륭합니다! 저축 목표를 달성했습니다.");
    }

    @Test
    @DisplayName("목표 미달 시 절약 필요 가이드 메시지를 생성한다")
    void calculateSavings_GoalNotMet() {
        // given: 수입 200만, 고정지출 80만, 변동지출 70만, 목표 100만
        SavingsPlan plan = new SavingsPlan(
                new BigDecimal("2000000"),
                new BigDecimal("800000"),
                new BigDecimal("700000"),
                BigDecimal.ZERO,
                new BigDecimal("1000000")
        );

        // then: 현재 저축 50만, 부족 50만
        assertThat(plan.calculateGap()).isEqualByComparingTo(new BigDecimal("500000"));
        assertThat(plan.generateRecommendation()).isEqualTo("목표를 위해 지출을 500000원 더 줄여야 합니다.");

        SavingsAnalysisResponse response = plan.toResponse();
        assertThat(response.getGap()).isEqualByComparingTo(new BigDecimal("500000"));
        assertThat(response.getCurrentSavings()).isEqualByComparingTo(new BigDecimal("500000"));
    }
}
