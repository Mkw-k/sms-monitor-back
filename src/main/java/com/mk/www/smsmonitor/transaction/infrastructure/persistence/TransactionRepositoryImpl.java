package com.mk.www.smsmonitor.transaction.infrastructure.persistence;

import com.mk.www.smsmonitor.transaction.domain.Transaction;
import com.mk.www.smsmonitor.transaction.domain.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

    private final TransactionJpaRepository transactionJpaRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = transactionMapper.toEntity(transaction);
        TransactionEntity savedEntity = transactionJpaRepository.save(entity);
        return transactionMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return transactionJpaRepository.findById(id)
                .map(transactionMapper::toDomain);
    }

    @Override
    public Page<Transaction> findAll(Pageable pageable) {
        return transactionJpaRepository.findAll(pageable)
                .map(transactionMapper::toDomain);
    }

    @Override
    public Page<Transaction> findAllByIsStupidCost(boolean isStupidCost, Pageable pageable) {
        return Page.empty();
    }

    @Override
    public Page<Transaction> findAllByUserId(Long userId, Pageable pageable) {
        return transactionJpaRepository.findAllByUserId(userId, pageable)
                .map(transactionMapper::toDomain);
    }

    @Override
    public Page<Transaction> findAllByUserIdAndIsStupidCost(Long userId, boolean isStupidCost, Pageable pageable) {
        return transactionJpaRepository.findAllByUserIdAndIsStupidCost(userId, isStupidCost, pageable)
                .map(transactionMapper::toDomain);
    }

    @Override
    public BigDecimal sumAmountByUserIdAfter(Long userId, LocalDateTime start) {
        return transactionJpaRepository.sumAmountByUserIdAndTypeAfter(userId, Transaction.TransactionType.EXPENSE, start);
    }

    @Override
    public BigDecimal sumAmountByUserIdBetween(Long userId, LocalDateTime start, LocalDateTime end) {
        return transactionJpaRepository.sumAmountByUserIdBetween(userId, start, end);
    }

    @Override
    public BigDecimal sumStupidCostByUserIdAfter(Long userId, LocalDateTime start) {
        return transactionJpaRepository.sumStupidCostByUserIdAfter(userId, start);
    }
}
