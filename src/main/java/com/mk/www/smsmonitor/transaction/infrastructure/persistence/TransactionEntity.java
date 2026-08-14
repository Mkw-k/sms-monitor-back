package com.mk.www.smsmonitor.transaction.infrastructure.persistence;

import com.mk.www.smsmonitor.category.infrastructure.persistence.SpendingCategoryEntity;
import com.mk.www.smsmonitor.transaction.domain.Transaction.TransactionType;
import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String vendor;

    @Column(nullable = false)
    private LocalDateTime transactionTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private SpendingCategoryEntity category;

    @Column(nullable = false)
    private boolean isStupidCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private boolean isFixedExpense;

    @Column(nullable = false)
    private boolean isManual;

    @Column(nullable = false)
    private boolean isIgnored; // 내역 무시 여부

    @Column(nullable = false)
    private boolean isDeleted; // 휴지통 이동 여부

    @Column(nullable = false)
    private boolean reflectInAsset; // 자산 반영 여부

    @Lob
    @Column(nullable = false)
    private String originalSmsContent;

    private String memo;
    private String cardNumber;
    private String name;
}
