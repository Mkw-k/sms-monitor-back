package com.mk.www.smsmonitor.transaction.application;

import com.mk.www.smsmonitor.account.infrastructure.AccountEntity;
import com.mk.www.smsmonitor.account.infrastructure.AccountJpaRepository;
import com.mk.www.smsmonitor.transaction.api.dto.*;
import com.mk.www.smsmonitor.transaction.domain.Transaction;
import com.mk.www.smsmonitor.transaction.domain.Transaction.TransactionType;
import com.mk.www.smsmonitor.transaction.infrastructure.persistence.TransactionEntity;
import com.mk.www.smsmonitor.transaction.infrastructure.persistence.TransactionJpaRepository;
import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import com.mk.www.smsmonitor.user.infrastructure.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionJpaRepository transactionRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private AccountJpaRepository accountJpaRepository;

    private UserEntity userEntity;
    private AccountEntity defaultAccount;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountJpaRepository.deleteAll();
        userJpaRepository.deleteAll();

        userEntity = UserEntity.builder()
                .loginId("testuser")
                .password("password123")
                .role("ROLE_USER")
                .isApproved(true)
                .point(1000L)
                .build();
        userJpaRepository.save(userEntity);

        defaultAccount = AccountEntity.builder()
                .user(userEntity)
                .bankName("메인은행")
                .accountNumber("110-001")
                .balance(new BigDecimal("1000000"))
                .isDefault(true)
                .build();
        accountJpaRepository.save(defaultAccount);
    }

    @Test
    @DisplayName("지출 내역 등록 시 기본 계좌의 잔액이 정확하게 차감된다")
    void createTransaction_ExpenseDeductsBalance() {
        // given
        TransactionCreateRequest request = new TransactionCreateRequest();
        request.setAmount(new BigDecimal("30000"));
        request.setVendor("스타벅스");
        request.setType(TransactionType.EXPENSE);
        request.setReflectInAsset(true);
        request.setTransactionTime(LocalDateTime.now());

        // when
        Transaction saved = transactionService.createTransaction(request, "testuser");

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("30000"));

        AccountEntity updatedAccount = accountJpaRepository.findById(defaultAccount.getId()).orElseThrow();
        // 100만 - 3만 = 97만
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo(new BigDecimal("970000"));
    }

    @Test
    @DisplayName("수입 내역 등록 시 기본 계좌의 잔액이 정확하게 가산된다")
    void createTransaction_IncomeAddsBalance() {
        // given
        TransactionCreateRequest request = new TransactionCreateRequest();
        request.setAmount(new BigDecimal("500000"));
        request.setVendor("월급");
        request.setType(TransactionType.INCOME);
        request.setReflectInAsset(true);
        request.setTransactionTime(LocalDateTime.now());

        // when
        Transaction saved = transactionService.createTransaction(request, "testuser");

        // then
        assertThat(saved.getId()).isNotNull();
        AccountEntity updatedAccount = accountJpaRepository.findById(defaultAccount.getId()).orElseThrow();
        // 100만 + 50만 = 150만
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo(new BigDecimal("1500000"));
    }

    @Test
    @DisplayName("거래 금액 수정 시 계좌 잔액이 실시간으로 동기화되어 재조정된다")
    void updateTransaction_SyncAccountBalance() {
        // given: 5만원 지출 등록 (100만 -> 95만)
        TransactionCreateRequest createReq = new TransactionCreateRequest();
        createReq.setAmount(new BigDecimal("50000"));
        createReq.setVendor("마트");
        createReq.setType(TransactionType.EXPENSE);
        createReq.setReflectInAsset(true);
        Transaction saved = transactionService.createTransaction(createReq, "testuser");

        AccountEntity accAfterCreate = accountJpaRepository.findById(defaultAccount.getId()).orElseThrow();
        assertThat(accAfterCreate.getBalance()).isEqualByComparingTo(new BigDecimal("950000"));

        // when: 지출 금액을 5만원 -> 7만원으로 수정 (이전 5만 복구(+5만) 후 7만 차감(-7만) => 최종 93만)
        TransactionUpdateRequest updateReq = new TransactionUpdateRequest();
        updateReq.setAmount(new BigDecimal("70000"));
        Optional<Transaction> updatedOpt = transactionService.updateTransaction(saved.getId(), updateReq, "testuser");

        // then
        assertThat(updatedOpt).isPresent();
        assertThat(updatedOpt.get().getAmount()).isEqualByComparingTo(new BigDecimal("70000"));

        AccountEntity accAfterUpdate = accountJpaRepository.findById(defaultAccount.getId()).orElseThrow();
        assertThat(accAfterUpdate.getBalance()).isEqualByComparingTo(new BigDecimal("930000"));
    }

    @Test
    @DisplayName("거래내역의 메모만 수정할 수 있다")
    void updateMemo_Success() {
        // given
        TransactionCreateRequest createReq = new TransactionCreateRequest();
        createReq.setAmount(new BigDecimal("10000"));
        createReq.setVendor("편의점");
        createReq.setType(TransactionType.EXPENSE);
        Transaction saved = transactionService.createTransaction(createReq, "testuser");

        // when
        MemoRequest memoReq = new MemoRequest();
        memoReq.setMemo("야식 구매");
        Optional<Transaction> result = transactionService.updateMemo(saved.getId(), memoReq, "testuser");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getMemo()).isEqualTo("야식 구매");
    }

    @Test
    @DisplayName("이번 달 지출, 수입, 멍청비용 요약을 정확하게 계산한다")
    void getSummary_Success() {
        // given
        // 1. 지출 20,000 (멍청비용 O)
        TransactionCreateRequest exp1 = new TransactionCreateRequest();
        exp1.setAmount(new BigDecimal("20000"));
        exp1.setVendor("택시");
        exp1.setType(TransactionType.EXPENSE);
        exp1.setStupidCost(true);
        transactionService.createTransaction(exp1, "testuser");

        // 2. 지출 30,000 (일반 지출)
        TransactionCreateRequest exp2 = new TransactionCreateRequest();
        exp2.setAmount(new BigDecimal("30000"));
        exp2.setVendor("서점");
        exp2.setType(TransactionType.EXPENSE);
        exp2.setStupidCost(false);
        transactionService.createTransaction(exp2, "testuser");

        // 3. 수입 100,000
        TransactionCreateRequest inc1 = new TransactionCreateRequest();
        inc1.setAmount(new BigDecimal("100000"));
        inc1.setVendor("용돈");
        inc1.setType(TransactionType.INCOME);
        transactionService.createTransaction(inc1, "testuser");

        // when
        TransactionSummaryResponse summary = transactionService.getSummary("testuser");

        // then
        assertThat(summary.getMonthlyTotalAmount()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(summary.getMonthlyTotalIncome()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(summary.getMonthlyStupidCostAmount()).isEqualByComparingTo(new BigDecimal("20000"));
    }

    @Test
    @DisplayName("거래내역 페이징 조회를 성공한다")
    void getUserTransactions_Paging() {
        // given
        for (int i = 1; i <= 5; i++) {
            TransactionCreateRequest req = new TransactionCreateRequest();
            req.setAmount(new BigDecimal(1000 * i));
            req.setVendor("가맹점" + i);
            req.setType(TransactionType.EXPENSE);
            transactionService.createTransaction(req, "testuser");
        }

        TransactionSearchRequest searchRequest = new TransactionSearchRequest();
        PageRequest pageable = PageRequest.of(0, 3);

        // when
        Page<Transaction> page = transactionService.getUserTransactions("testuser", searchRequest, pageable);

        // then
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).hasSize(3);
    }
}
