package com.mk.www.smsmonitor.transaction.infrastructure.analysis;

import com.mk.www.smsmonitor.transaction.domain.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LateNightPurchaseStrategyTest {

    private LateNightPurchaseStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new LateNightPurchaseStrategy();
    }

    @Test
    @DisplayName("오후_10시_이후_편의점_결제는_멍청비용으로_판단한다")
    void 오후_10시_이후_편의점_결제는_멍청비용으로_판단한다() {
        // given
        Transaction transaction = Transaction.builder()
                .vendor("GS25 수궁동점")
                .amount(new BigDecimal("5000"))
                .transactionTime(LocalDateTime.now().withHour(23))
                .build();

        // when
        boolean result = strategy.isStupidCost(transaction);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("오후_10시_이전_편의점_결제는_멍청비용이_아니다")
    void 오후_10시_이전_편의점_결제는_멍청비용이_아니다() {
        // given
        Transaction transaction = Transaction.builder()
                .vendor("CU 독산점")
                .amount(new BigDecimal("5000"))
                .transactionTime(LocalDateTime.now().withHour(21))
                .build();

        // when
        boolean result = strategy.isStupidCost(transaction);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("오후_10시_이후라도_편의점이_아니면_멍청비용이_아니다")
    void 오후_10시_이후라도_편의점이_아니면_멍청비용이_아니다() {
        // given
        Transaction transaction = Transaction.builder()
                .vendor("일반 슈퍼")
                .amount(new BigDecimal("5000"))
                .transactionTime(LocalDateTime.now().withHour(23))
                .build();

        // when
        boolean result = strategy.isStupidCost(transaction);

        // then
        assertThat(result).isFalse();
    }
}
