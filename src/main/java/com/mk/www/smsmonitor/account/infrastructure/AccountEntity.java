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
}
