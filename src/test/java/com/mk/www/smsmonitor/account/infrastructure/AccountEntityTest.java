package com.mk.www.smsmonitor.account.infrastructure;

import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountEntityTest {

    @Test
    @DisplayName("계좌 입금 및 출금 처리가 정상 수행된다")
    void depositAndWithdraw() {
        AccountEntity account = AccountEntity.builder()
                .balance(new BigDecimal("10000"))
                .build();

        account.deposit(new BigDecimal("5000"));
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("15000"));

        account.withdraw(new BigDecimal("3000"));
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("12000"));
    }

    @Test
    @DisplayName("0 이하의 금액으로 입금 또는 출금 시 예외가 발생한다")
    void invalidDepositAndWithdraw() {
        AccountEntity account = AccountEntity.builder()
                .balance(new BigDecimal("10000"))
                .build();

        assertThatThrownBy(() -> account.deposit(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> account.withdraw(new BigDecimal("-1000")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("기본 계좌 여부 설정 및 해제가 정상 동작한다")
    void markAndUnmarkDefault() {
        AccountEntity account = AccountEntity.builder().isDefault(false).build();

        account.markAsDefault();
        assertThat(account.isDefault()).isTrue();

        account.unmarkDefault();
        assertThat(account.isDefault()).isFalse();
    }

    @Test
    @DisplayName("소유자 일치 여부를 판정한다")
    void isOwnedBy() {
        UserEntity user1 = UserEntity.builder().id(1L).build();
        UserEntity user2 = UserEntity.builder().id(2L).build();

        AccountEntity account = AccountEntity.builder().user(user1).build();

        assertThat(account.isOwnedBy(user1)).isTrue();
        assertThat(account.isOwnedBy(user2)).isFalse();
    }
}
