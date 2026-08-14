package com.mk.www.smsmonitor.transaction.infrastructure.analysis;

import com.mk.www.smsmonitor.category.domain.SpendingCategory;
import com.mk.www.smsmonitor.transaction.domain.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class HighAmountStrategyTest {

    private HighAmountStrategy strategy;
    private SpendingCategory foodCategory;
    private SpendingCategory otherCategory;

    @BeforeEach
    void setUp() {
        strategy = new HighAmountStrategy();
        foodCategory = SpendingCategory.builder().id(1L).name("식비").build();
        otherCategory = SpendingCategory.builder().id(2L).name("교통비").build();
    }

    @Test
    @DisplayName("식비_카테고리에서_5만원_초과_결제는_멍청비용으로_판단한다")
    void 식비_카테고리에서_5만원_초과_결제는_멍청비용으로_판단한다() {
        // given
        Transaction transaction = Transaction.builder()
                .vendor("고급 레스토랑")
                .amount(new BigDecimal("70000"))
                .transactionTime(LocalDateTime.now())
                .category(foodCategory)
                .build();

        // when
        boolean result = strategy.isStupidCost(transaction);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("식비_카테고리에서_5만원_이하_결제는_멍청비용이_아니다")
    void 식비_카테고리에서_5만원_이하_결제는_멍청비용이_아니다() {
        // given
        Transaction transaction = Transaction.builder()
                .vendor("일반 식당")
                .amount(new BigDecimal("49000"))
                .transactionTime(LocalDateTime.now())
                .category(foodCategory)
                .build();

        // when
        boolean result = strategy.isStupidCost(transaction);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("다른_카테고리에서_5만원_초과_결제는_멍청비용이_아니다")
    void 다른_카테고리에서_5만원_초과_결제는_멍청비용이_아니다() {
        // given
        Transaction transaction = Transaction.builder()
                .vendor("KTX")
                .amount(new BigDecimal("70000"))
                .transactionTime(LocalDateTime.now())
                .category(otherCategory)
                .build();

        // when
        boolean result = strategy.isStupidCost(transaction);

        // then
        assertThat(result).isFalse();
    }
}
