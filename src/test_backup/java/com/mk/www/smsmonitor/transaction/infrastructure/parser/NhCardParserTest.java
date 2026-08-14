package com.mk.www.smsmonitor.transaction.infrastructure.parser;

import com.mk.www.smsmonitor.transaction.domain.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class NhCardParserTest {

    private final NhCardParser parser = new NhCardParser();

    @Test
    @DisplayName("한_줄로_된_NH카드_승인_문자를_성공적으로_파싱한다")
    void 한_줄로_된_NH카드_승인_문자를_성공적으로_파싱한다() {
        // given
        String smsContent = "NH카드2*0* 승인 고명우 1,000 체크 11/22 16:25 지에스(GS)25 궁동중앙점";

        // when
        Optional<Transaction> result = parser.parse(smsContent);

        // then
        assertThat(result).isPresent();
        Transaction transaction = result.get();
        assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(transaction.getVendor()).isEqualTo("지에스(GS)25 궁동중앙점");
        assertThat(transaction.getCardNumber()).isEqualTo("2*0*");
        assertThat(transaction.getName()).isEqualTo("고명우");
    }
}
