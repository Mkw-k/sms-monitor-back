package com.mk.www.smsmonitor.transaction.infrastructure.analysis;

import com.mk.www.smsmonitor.transaction.domain.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConvenienceStoreStrategyTest {

    private final ConvenienceStoreStrategy strategy = new ConvenienceStoreStrategy();

    @Test
    @DisplayName("편의점, GS25, 지에스 가맹점인 경우 멍청비용으로 판정한다")
    void isStupidCost_True() {
        Transaction tx1 = Transaction.builder().vendor("GS25 강남점").build();
        Transaction tx2 = Transaction.builder().vendor("지에스 편의점").build();
        Transaction tx3 = Transaction.builder().vendor("CU편의점").build();

        assertThat(strategy.isStupidCost(tx1)).isTrue();
        assertThat(strategy.isStupidCost(tx2)).isTrue();
        assertThat(strategy.isStupidCost(tx3)).isTrue();
    }

    @Test
    @DisplayName("편의점 관련 가맹점이 아닌 경우 멍청비용이 아니다")
    void isStupidCost_False() {
        Transaction tx = Transaction.builder().vendor("교보문고").build();
        assertThat(strategy.isStupidCost(tx)).isFalse();
    }
}
