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
    public Transaction createTransaction(TransactionCreateRequest request, String loginId) {
        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .vendor(request.getVendor())
                .transactionTime(request.getTransactionTime() != null ? request.getTransactionTime() : LocalDateTime.now())
                .type(request.getType())
                .isFixedExpense(request.isFixedExpense())
                .isStupidCost(request.isStupidCost())
                .isIgnored(request.isIgnored())
                .reflectInAsset(request.isReflectInAsset())
                .memo(request.getMemo())
                .category(request.getCategory())
                .isManual(true)
                .build();
        return save(transaction, loginId);
    }

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
        if (saved.isApplicableToAsset()) {
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
                    boolean wasReflected = entity.isApplicableToAsset();
                    BigDecimal oldAmount = entity.getAmount();
                    TransactionType oldType = entity.getType();

                    // 도메인 객체 내부로 변경 책임 위임
                    entity.update(request);

                    TransactionEntity updated = transactionRepository.save(entity);

                    // 자산 실시간 동기화
                    boolean isNowReflected = updated.isApplicableToAsset();
                    
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

    @Transactional
    public Optional<Transaction> updateMemo(Long id, MemoRequest request, String loginId) {
        UserEntity user = userJpaRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return transactionRepository.findById(id)
                .filter(entity -> entity.getUser().getId().equals(user.getId()))
                .map(entity -> {
                    entity.updateMemo(request.getMemo());
                    return toDomain(transactionRepository.save(entity));
                });
    }

    private void updateAccountBalance(UserEntity user, BigDecimal amount, TransactionType type, boolean isNew) {
        accountJpaRepository.findByUserAndIsDefault(user, true).ifPresent(account -> {
            boolean isExpense = (type == TransactionType.EXPENSE);
            if (isNew) {
                // 신규 내역: 지출은 출금, 수입은 입금
                if (isExpense) {
                    account.withdraw(amount);
                } else {
                    account.deposit(amount);
                }
            } else {
                // 취소/수정(복구): 지출 취소는 재입금, 수입 취소는 재출금
                if (isExpense) {
                    account.deposit(amount);
                } else {
                    account.withdraw(amount);
                }
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

        com.mk.www.smsmonitor.transaction.domain.SavingsPlan savingsPlan =
                new com.mk.www.smsmonitor.transaction.domain.SavingsPlan(income, fixed, variable, stupid, target);

        return savingsPlan.toResponse();
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
