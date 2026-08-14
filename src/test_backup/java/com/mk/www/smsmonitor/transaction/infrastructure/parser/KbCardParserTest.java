package com.mk.www.smsmonitor.transaction.infrastructure.parser;

import com.mk.www.smsmonitor.transaction.domain.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class KbCardParserTest {

    private KbCardParser parser;

    @BeforeEach
    void setUp() {
        parser = new KbCardParser();
    }

    @Test
    @DisplayName("KB국민카드 줄바꿈 형식 파싱 테스트")
    void KB국민카드_줄바꿈_형식_파싱_테스트() {
        // given
        String sms = "[Web발신]\n" +
                "KB국민카드4020승인\n" +
                "고*우님\n" +
                "1,000원 일시불\n" +
                "03/13 07:51\n" +
                "서울시설공단\n" +
                "누적106,670원";

        // when
        Optional<Transaction> result = parser.parse(sms);

        // then
        assertThat(result).isPresent();
        Transaction transaction = result.get();
        assertThat(transaction.getAmount()).isEqualByComparingTo("1000");
        assertThat(transaction.getVendor()).isEqualTo("서울시설공단");
        assertThat(transaction.getCardNumber()).isEqualTo("4020");
        assertThat(transaction.getName()).isEqualTo("고*우님");
        assertThat(transaction.getTransactionTime().getMonth()).isEqualTo(Month.MARCH);
        assertThat(transaction.getTransactionTime().getDayOfMonth()).isEqualTo(13);
        assertThat(transaction.getTransactionTime().getHour()).isEqualTo(7);
        assertThat(transaction.getTransactionTime().getMinute()).isEqualTo(51);
    }

    @Test
    @DisplayName("KB국민카드 ADB(공백) 형식 파싱 테스트")
    void KB국민카드_ADB_공백_형식_파싱_테스트() {
        // given
        String sms = "[Web발신] KB국민카드4020승인 고*우님 2,900원 일시불 03/04 18:55 지에스25 구로우 누적2,900원";

        // when
        Optional<Transaction> result = parser.parse(sms);

        // then
        assertThat(result).isPresent();
        Transaction transaction = result.get();
        assertThat(transaction.getAmount()).isEqualByComparingTo("2900");
        assertThat(transaction.getVendor()).isEqualTo("지에스25 구로우");
        assertThat(transaction.getCardNumber()).isEqualTo("4020");
        assertThat(transaction.getName()).isEqualTo("고*우님");
        assertThat(transaction.getTransactionTime().getMonth()).isEqualTo(Month.MARCH);
        assertThat(transaction.getTransactionTime().getDayOfMonth()).isEqualTo(4);
        assertThat(transaction.getTransactionTime().getHour()).isEqualTo(18);
        assertThat(transaction.getTransactionTime().getMinute()).isEqualTo(55);
    }

    @Test
    @DisplayName("지원하지_않는_SMS_형식은_파싱하지_않는다")
    void 지원하지_않는_SMS_형식은_파싱하지_않는다() {
        // given
        String sms = "다른 카드사 문자 메시지";

        // when
        Optional<Transaction> result = parser.parse(sms);

        // then
        assertThat(result).isNotPresent();
    }
}
