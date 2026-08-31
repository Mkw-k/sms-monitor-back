package com.mk.www.smsmonitor.account.infrastructure;

import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private boolean isDefault; // 메인(기본) 계좌 여부

    /**
     * 계좌 입금 처리
     */
    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("입금액은 0보다 커야 합니다.");
        }
        this.balance = this.balance.add(amount);
    }

    /**
     * 계좌 출금 처리
     */
    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("출금액은 0보다 커야 합니다.");
        }
        this.balance = this.balance.subtract(amount);
    }

    /**
     * 대표(기본) 계좌로 설정
     */
    public void markAsDefault() {
        this.isDefault = true;
    }

    /**
     * 기본 계좌 해제
     */
    public void unmarkDefault() {
        this.isDefault = false;
    }

    /**
     * 계좌 소유자 여부 검증
     */
    public boolean isOwnedBy(UserEntity user) {
        if (user == null || this.user == null) {
            return false;
        }
        return this.user.getId().equals(user.getId());
    }
}
