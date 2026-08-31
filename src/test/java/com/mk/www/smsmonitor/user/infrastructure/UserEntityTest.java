package com.mk.www.smsmonitor.user.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserEntityTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("비밀번호 일치 여부를 정확하게 검증한다")
    void matchPassword() {
        UserEntity user = UserEntity.builder()
                .password(passwordEncoder.encode("mySecretPassword"))
                .build();

        assertThat(user.matchPassword("mySecretPassword", passwordEncoder)).isTrue();
        assertThat(user.matchPassword("wrongPassword", passwordEncoder)).isFalse();
    }

    @Test
    @DisplayName("포인트 적립 및 사용 처리가 정상 수행된다")
    void addAndUsePoint() {
        UserEntity user = UserEntity.builder()
                .point(1000L)
                .build();

        user.addPoint(500L);
        assertThat(user.getPoint()).isEqualTo(1500L);

        user.usePoint(300L);
        assertThat(user.getPoint()).isEqualTo(1200L);
    }

    @Test
    @DisplayName("보유 포인트 초과 사용 시 예외가 발생한다")
    void usePoint_Insufficient() {
        UserEntity user = UserEntity.builder()
                .point(500L)
                .build();

        assertThatThrownBy(() -> user.usePoint(1000L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("포인트가 부족합니다.");
    }

    @Test
    @DisplayName("승인된 사용자만 접근 가능 여부가 true이다")
    void isAccessible() {
        UserEntity approvedUser = UserEntity.builder().isApproved(true).build();
        UserEntity unapprovedUser = UserEntity.builder().isApproved(false).build();

        assertThat(approvedUser.isAccessible()).isTrue();
        assertThat(unapprovedUser.isAccessible()).isFalse();
    }
}
