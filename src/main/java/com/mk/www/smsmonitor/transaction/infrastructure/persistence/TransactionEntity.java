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

    /**
     * 거래 정보 수정 (요청 DTO에 포함된 필드만 도메인 내부에서 선별 업데이트)
     */
    public void update(com.mk.www.smsmonitor.transaction.api.dto.TransactionUpdateRequest request) {
        if (request == null) return;
        if (request.getAmount() != null) this.amount = request.getAmount();
        if (request.getVendor() != null) this.vendor = request.getVendor();
        if (request.getTransactionTime() != null) this.transactionTime = request.getTransactionTime();
        if (request.getIsStupidCost() != null) this.isStupidCost = request.getIsStupidCost();
        if (request.getIsFixedExpense() != null) this.isFixedExpense = request.getIsFixedExpense();
        if (request.getIsIgnored() != null) this.isIgnored = request.getIsIgnored();
        if (request.getIsDeleted() != null) this.isDeleted = request.getIsDeleted();
        if (request.getReflectInAsset() != null) this.reflectInAsset = request.getReflectInAsset();
        if (request.getMemo() != null) this.memo = request.getMemo();
        if (request.getType() != null) this.type = request.getType();
    }

    /**
     * 메모 단독 수정
     */
    public void updateMemo(String memo) {
        this.memo = memo;
    }

    /**
     * 실시간 자산 잔액 반영 대상 여부 판정
     * (자산 반영 플래그가 활성화되어 있고, 삭제되거나 무시된 내역이 아니어야 함)
     */
    public boolean isApplicableToAsset() {
        return this.reflectInAsset && !this.isDeleted && !this.isIgnored;
    }
}
