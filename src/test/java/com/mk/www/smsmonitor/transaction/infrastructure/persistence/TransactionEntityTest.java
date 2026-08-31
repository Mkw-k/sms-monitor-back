package com.mk.www.smsmonitor.transaction.infrastructure.persistence;

import com.mk.www.smsmonitor.transaction.api.dto.TransactionUpdateRequest;
import com.mk.www.smsmonitor.transaction.domain.Transaction.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionEntityTest {

    @Test
    @DisplayName("TransactionUpdateRequest를 통한 도메인 업데이트가 정상 동작한다")
    void updateFromRequest() {
        TransactionEntity entity = new TransactionEntity();
        entity.setAmount(new BigDecimal("10000"));
        entity.setVendor("원래가맹점");
        entity.setType(TransactionType.EXPENSE);
        entity.setMemo("원래메모");

        TransactionUpdateRequest request = new TransactionUpdateRequest();
        request.setAmount(new BigDecimal("20000"));
        request.setVendor("수정가맹점");
        request.setMemo("수정메모");

        entity.update(request);

        assertThat(entity.getAmount()).isEqualByComparingTo(new BigDecimal("20000"));
        assertThat(entity.getVendor()).isEqualTo("수정가맹점");
        assertThat(entity.getMemo()).isEqualTo("수정메모");
        assertThat(entity.getType()).isEqualTo(TransactionType.EXPENSE);
    }

    @Test
    @DisplayName("자산 반영 대상 여부를 올바르게 판정한다")
    void isApplicableToAsset() {
        TransactionEntity entity = new TransactionEntity();
        entity.setReflectInAsset(true);
        entity.setDeleted(false);
        entity.setIgnored(false);
        assertThat(entity.isApplicableToAsset()).isTrue();

        entity.setDeleted(true);
        assertThat(entity.isApplicableToAsset()).isFalse();

        entity.setDeleted(false);
        entity.setIgnored(true);
        assertThat(entity.isApplicableToAsset()).isFalse();

        entity.setIgnored(false);
        entity.setReflectInAsset(false);
        assertThat(entity.isApplicableToAsset()).isFalse();
    }
}
