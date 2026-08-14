package com.mk.www.smsmonitor.transaction.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(Long id);
    Page<Transaction> findAll(Pageable pageable);
    Page<Transaction> findAllByIsStupidCost(boolean isStupidCost, Pageable pageable);

    // 유저 기반 조회 추가
    Page<Transaction> findAllByUserId(Long userId, Pageable pageable);
    Page<Transaction> findAllByUserIdAndIsStupidCost(Long userId, boolean isStupidCost, Pageable pageable);
    BigDecimal sumAmountByUserIdAfter(Long userId, LocalDateTime start);
    BigDecimal sumAmountByUserIdBetween(Long userId, LocalDateTime start, LocalDateTime end);
    BigDecimal sumStupidCostByUserIdAfter(Long userId, LocalDateTime start);
}
