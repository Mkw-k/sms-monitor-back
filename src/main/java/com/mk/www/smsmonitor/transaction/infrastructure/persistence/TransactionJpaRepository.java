package com.mk.www.smsmonitor.transaction.infrastructure.persistence;

import com.mk.www.smsmonitor.transaction.domain.Transaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, Long>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<TransactionEntity> {

    Page<TransactionEntity> findAllByUserId(Long userId, Pageable pageable);

    Page<TransactionEntity> findAllByUserIdAndIsStupidCost(Long userId, boolean isStupidCost, Pageable pageable);

    java.util.List<TransactionEntity> findAllByUserIdAndTransactionTimeAfter(Long userId, LocalDateTime after);

    java.util.List<TransactionEntity> findAllByUserIdAndTransactionTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);

    // 자동 고정지출 판별을 위한 쿼리: 과거에 동일 업체, 동일 금액으로 결제한 적이 있는지 확인
    boolean existsByUserIdAndVendorAndAmountAndType(Long userId, String vendor, BigDecimal amount, TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE t.user.id = :userId AND t.type = :type AND t.isIgnored = false AND t.isDeleted = false AND t.transactionTime >= :after")
    BigDecimal sumAmountByUserIdAndTypeAfter(@Param("userId") Long userId, @Param("type") TransactionType type, @Param("after") LocalDateTime after);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE t.user.id = :userId AND t.isStupidCost = true AND t.isIgnored = false AND t.isDeleted = false AND t.transactionTime >= :after")
    BigDecimal sumStupidCostByUserIdAfter(@Param("userId") Long userId, @Param("after") LocalDateTime after);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE t.user.id = :userId AND t.isFixedExpense = true AND t.isIgnored = false AND t.isDeleted = false AND t.transactionTime >= :after")
    BigDecimal sumFixedExpenseByUserIdAfter(@Param("userId") Long userId, @Param("after") LocalDateTime after);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE t.user.id = :userId AND t.type = 'EXPENSE' AND t.isFixedExpense = false AND t.isIgnored = false AND t.isDeleted = false AND t.transactionTime >= :after")
    BigDecimal sumVariableExpenseByUserIdAfter(@Param("userId") Long userId, @Param("after") LocalDateTime after);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE t.user.id = :userId AND t.isIgnored = false AND t.isDeleted = false AND t.transactionTime BETWEEN :start AND :end")
    BigDecimal sumAmountByUserIdBetween(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
