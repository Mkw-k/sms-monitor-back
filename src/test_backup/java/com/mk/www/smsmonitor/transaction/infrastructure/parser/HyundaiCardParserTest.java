package com.mk.www.smsmonitor.transaction.infrastructure.parser;

import com.mk.www.smsmonitor.transaction.domain.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class HyundaiCardParserTest {

    private final HyundaiCardParser parser = new HyundaiCardParser();

    @Test
    @DisplayName("현대카드 승인 문자를 정상적으로 파싱한다")
    void parseHyundaiCardSms() {
        // given
        String smsContent = "[Web발신]\n" +
                "Hyundai Mobility카드 승인\n" +
                "고*우\n" +
                "3,600원 일시불\n" +
                "03/11 19:16\n" +
                "GS25구로우신\n" +
                "누적835,215원";

        // when
        Optional<Transaction> result = parser.parse(smsContent);

        // then
        assertThat(result).isPresent();
        Transaction transaction = result.get();
        assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("3600"));
        assertThat(transaction.getVendor()).isEqualTo("GS25구로우신");
        assertThat(transaction.getTransactionTime().getMonthValue()).isEqualTo(3);
        assertThat(transaction.getTransactionTime().getDayOfMonth()).isEqualTo(11);
        assertThat(transaction.getTransactionTime().getHour()).isEqualTo(19);
        assertThat(transaction.getTransactionTime().getMinute()).isEqualTo(16);
    }

    @Test
    @DisplayName("지원하지 않는 형식의 문자는 파싱하지 않는다")
    void shouldNotParseUnsupportedSms() {
        // given
        String smsContent = "일반적인 메시지 내용";

        // when
        Optional<Transaction> result = parser.parse(smsContent);

        // then
        assertThat(result).isEmpty();
    }
}
