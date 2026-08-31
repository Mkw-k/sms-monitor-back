package com.mk.www.smsmonitor.transaction.application;

import com.mk.www.smsmonitor.account.infrastructure.AccountEntity;
import com.mk.www.smsmonitor.account.infrastructure.AccountJpaRepository;
import com.mk.www.smsmonitor.transaction.api.dto.*;
import com.mk.www.smsmonitor.transaction.domain.Transaction;
import com.mk.www.smsmonitor.transaction.domain.Transaction.TransactionType;
import com.mk.www.smsmonitor.transaction.infrastructure.persistence.TransactionEntity;
import com.mk.www.smsmonitor.transaction.infrastructure.persistence.TransactionJpaRepository;
import com.mk.www.smsmonitor.transaction.infrastructure.persistence.TransactionSpecification;
import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import com.mk.www.smsmonitor.user.infrastructure.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionJpaRepository transactionRepository;
    private final UserJpaRepository userJpaRepository;
    private final AccountJpaRepository accountJpaRepository;

    @Transactional
    public Transaction save(Transaction transaction, String loginId) {
        UserEntity user = userJpaRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        TransactionEntity entity = new TransactionEntity();
        entity.setUser(user);
        entity.setAmount(transaction.getAmount());
        entity.setVendor(transaction.getVendor());
        entity.setTransactionTime(transaction.getTransactionTime());
        entity.setStupidCost(transaction.isStupidCost());
        entity.setType(transaction.getType() != null ? transaction.getType() : TransactionType.EXPENSE);
        
        boolean isLikelyFixed = transactionRepository.existsByUserIdAndVendorAndAmountAndType(
                user.getId(), transaction.getVendor(), transaction.getAmount(), entity.getType());
        
        entity.setFixedExpense(transaction.isFixedExpense() || isLikelyFixed);
        entity.setManual(transaction.isManual());
        entity.setIgnored(transaction.isIgnored());
        entity.setDeleted(transaction.isDeleted());
        entity.setReflectInAsset(transaction.isReflectInAsset());
        entity.setOriginalSmsContent(transaction.getOriginalSmsContent() != null ? transaction.getOriginalSmsContent() : "MANUAL");
        entity.setMemo(transaction.getMemo());
        entity.setCardNumber(transaction.getCardNumber());
        entity.setName(transaction.getName());

        TransactionEntity saved = transactionRepository.save(entity);

        // 자산 반영 로직
        if (saved.isReflectInAsset() && !saved.isDeleted() && !saved.isIgnored()) {
            updateAccountBalance(user, saved.getAmount(), saved.getType(), true);
        }

        return toDomain(saved);
    }

    @Transactional
    public Optional<Transaction> updateTransaction(Long id, TransactionUpdateRequest request, String loginId) {
        UserEntity user = userJpaRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return transactionRepository.findById(id)
                .filter(entity -> entity.getUser().getId().equals(user.getId()))
                .map(entity -> {
                    // 이전 상태 백업 (자산 반영 계산용)
                    boolean wasReflected = entity.isReflectInAsset() && !entity.isDeleted() && !entity.isIgnored();
                    BigDecimal oldAmount = entity.getAmount();
                    TransactionType oldType = entity.getType();

                    if (request.getAmount() != null) entity.setAmount(request.getAmount());
                    if (request.getVendor() != null) entity.setVendor(request.getVendor());
                    if (request.getTransactionTime() != null) entity.setTransactionTime(request.getTransactionTime());
                    if (request.getIsStupidCost() != null) entity.setStupidCost(request.getIsStupidCost());
                    if (request.getIsFixedExpense() != null) entity.setFixedExpense(request.getIsFixedExpense());
                    if (request.getIsIgnored() != null) entity.setIgnored(request.getIsIgnored());
                    if (request.getIsDeleted() != null) entity.setDeleted(request.getIsDeleted());
                    if (request.getReflectInAsset() != null) entity.setReflectInAsset(request.getReflectInAsset());
                    if (request.getMemo() != null) entity.setMemo(request.getMemo());
                    if (request.getType() != null) entity.setType(request.getType());

                    TransactionEntity updated = transactionRepository.save(entity);

                    // 자산 실시간 동기화
                    boolean isNowReflected = updated.isReflectInAsset() && !updated.isDeleted() && !updated.isIgnored();
                    
                    if (wasReflected && !isNowReflected) {
                        // 자산에서 제외됨 -> 이전 금액 복구
                        updateAccountBalance(user, oldAmount, oldType, false);
                    } else if (!wasReflected && isNowReflected) {
                        // 자산에 새로 포함됨 -> 금액 반영
                        updateAccountBalance(user, updated.getAmount(), updated.getType(), true);
                    } else if (wasReflected && isNowReflected) {
                        // 둘 다 반영됨 -> 금액 차이만큼만 조정
                        if (!oldAmount.equals(updated.getAmount()) || oldType != updated.getType()) {
                            updateAccountBalance(user, oldAmount, oldType, false); // 복구 후
                            updateAccountBalance(user, updated.getAmount(), updated.getType(), true); // 재반영
                        }
                    }

                    return toDomain(updated);
                });
    }

    private void updateAccountBalance(UserEntity user, BigDecimal amount, TransactionType type, boolean isNew) {
        accountJpaRepository.findByUserAndIsDefault(user, true).ifPresent(account -> {
            BigDecimal adjustment = amount;
            if (type == TransactionType.EXPENSE) {
                // 지출이면: 새 내역이면 차감(-), 취소/수정이면 합산(+)
                if (!isNew) adjustment = adjustment.negate();
                account.setBalance(account.getBalance().subtract(adjustment));
            } else {
                // 수입이면: 새 내역이면 합산(+), 취소/수정이면 차감(-)
                if (!isNew) adjustment = adjustment.negate();
                account.setBalance(account.getBalance().add(adjustment));
            }
            accountJpaRepository.save(account);
            log.info("[Asset Sync] Account: {}, Type: {}, Amount: {}, NewBalance: {}", 
                    account.getBankName(), type, amount, account.getBalance());
        });
    }

    public TransactionSummaryResponse getSummary(String loginId) {
        UserEntity user = userJpaRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        // 삭제된 내역 제외
        BigDecimal expense = transactionRepository.sumAmountByUserIdAndTypeAfter(user.getId(), TransactionType.EXPENSE, startOfMonth);
        BigDecimal income = transactionRepository.sumAmountByUserIdAndTypeAfter(user.getId(), TransactionType.INCOME, startOfMonth);
        BigDecimal stupid = transactionRepository.sumStupidCostByUserIdAfter(user.getId(), startOfMonth);

        return new TransactionSummaryResponse(expense, income, stupid, LocalDateTime.now().getMonthValue() + "월");
    }

    public Page<Transaction> getUserTransactions(String loginId, TransactionSearchRequest searchRequest, Pageable pageable) {
        UserEntity user = userJpaRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return transactionRepository.findAll(TransactionSpecification.filterBy(searchRequest, user.getId()), pageable)
                .map(this::toDomain);
    }

    public TransactionStatisticsResponse getStatistics(String loginId, String period, LocalDateTime baseDate) {
        UserEntity user = userJpaRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        LocalDateTime refDate = (baseDate != null) ? baseDate : LocalDateTime.now();
        LocalDateTime start;
        LocalDateTime end = refDate;
        DateTimeFormatter formatter;
        List<LocalDateTime> dateRange = new ArrayList<>();
        
        switch (period.toLowerCase()) {
            case "daily":
                start = refDate.minusDays(6);
                formatter = DateTimeFormatter.ofPattern("MM-dd");
                for (int i = 0; i < 7; i++) dateRange.add(start.plusDays(i));
                break;
            case "weekly":
                start = refDate.minusWeeks(6);
                formatter = DateTimeFormatter.ofPattern("w'주'");
                for (int i = 0; i < 7; i++) dateRange.add(start.plusWeeks(i));
                break;
            case "monthly":
                start = refDate.minusMonths(11);
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                for (int i = 0; i < 12; i++) dateRange.add(start.plusMonths(i));
                break;
            case "yearly":
                start = refDate.minusYears(2);
                formatter = DateTimeFormatter.ofPattern("yyyy'년'");
                for (int i = 0; i < 3; i++) dateRange.add(start.plusYears(i));
                break;
            default:
                start = refDate.minusMonths(1);
                formatter = DateTimeFormatter.ofPattern("MM-dd");
        }

        List<TransactionEntity> transactions = transactionRepository.findAllByUserIdAndTransactionTimeBetween(user.getId(), start, end);
        
        Map<String, TransactionStatisticsResponse.StatEntry> aggregation = new LinkedHashMap<>();
        for (LocalDateTime dt : dateRange) {
            String label = dt.format(formatter);
            aggregation.put(label, TransactionStatisticsResponse.StatEntry.builder()
                    .label(label)
                    .income(BigDecimal.ZERO)
                    .expense(BigDecimal.ZERO)
                    .build());
        }
        
        for (TransactionEntity entity : transactions) {
            if (entity.isDeleted() || entity.isIgnored()) continue; // 삭제되거나 무시된 내역은 통계에서 제외

            String label = entity.getTransactionTime().format(formatter);
            if (!aggregation.containsKey(label)) continue;
            
            TransactionStatisticsResponse.StatEntry entry = aggregation.get(label);
            if (entity.getType() == TransactionType.INCOME) {
                entry.setIncome(entry.getIncome().add(entity.getAmount()));
            } else {
                entry.setExpense(entry.getExpense().add(entity.getAmount()));
            }
        }

        return TransactionStatisticsResponse.builder()
                .entries(new ArrayList<>(aggregation.values()))
                .build();
    }

    public SavingsAnalysisResponse analyzeSavings(String loginId, BigDecimal target) {
        UserEntity user = userJpaRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        BigDecimal income = transactionRepository.sumAmountByUserIdAndTypeAfter(user.getId(), TransactionType.INCOME, startOfMonth);
        BigDecimal fixed = transactionRepository.sumFixedExpenseByUserIdAfter(user.getId(), startOfMonth);
        BigDecimal variable = transactionRepository.sumVariableExpenseByUserIdAfter(user.getId(), startOfMonth);
        BigDecimal stupid = transactionRepository.sumStupidCostByUserIdAfter(user.getId(), startOfMonth);

        BigDecimal maxPossible = income.subtract(fixed);
        BigDecimal currentSavings = income.subtract(fixed).subtract(variable);
        BigDecimal gap = target.subtract(currentSavings);

        String recommendation = gap.compareTo(BigDecimal.ZERO) <= 0 
                ? "훌륭합니다! 저축 목표를 달성했습니다." 
                : "목표를 위해 지출을 " + gap + "원 더 줄여야 합니다.";

        return SavingsAnalysisResponse.builder()
                .income(income).fixedExpense(fixed).variableExpense(variable).stupidExpense(stupid)
                .maxPossibleSavings(maxPossible).currentSavings(currentSavings).targetSavings(target).gap(gap)
                .recommendation(recommendation).build();
    }

    private Transaction toDomain(TransactionEntity entity) {
        return Transaction.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .amount(entity.getAmount())
                .vendor(entity.getVendor())
                .transactionTime(entity.getTransactionTime())
                .isStupidCost(entity.isStupidCost())
                .type(entity.getType())
                .isFixedExpense(entity.isFixedExpense())
                .isManual(entity.isManual())
                .isIgnored(entity.isIgnored())
                .isDeleted(entity.isDeleted())
                .reflectInAsset(entity.isReflectInAsset())
                .originalSmsContent(entity.getOriginalSmsContent())
                .memo(entity.getMemo())
                .cardNumber(entity.getCardNumber())
                .name(entity.getName())
                .build();
    }
}
