package com.mk.www.smsmonitor.transaction.infrastructure.persistence;

import com.mk.www.smsmonitor.category.domain.SpendingCategory;
import com.mk.www.smsmonitor.category.infrastructure.persistence.SpendingCategoryMapper;
import com.mk.www.smsmonitor.category.infrastructure.persistence.SpendingCategoryRepositoryImpl;
import com.mk.www.smsmonitor.transaction.domain.Transaction;
import com.mk.www.smsmonitor.category.domain.SpendingCategoryRepository;
import com.mk.www.smsmonitor.transaction.domain.TransactionRepository;
import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({TransactionRepositoryImpl.class, TransactionMapper.class, SpendingCategoryRepositoryImpl.class, SpendingCategoryMapper.class})
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SpendingCategoryRepository spendingCategoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity testUser;
    private SpendingCategory savedCategory;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .loginId("test-user")
                .password("password")
                .role("ROLE_USER")
                .isApproved(true)
                .point(0L)
                .build();
        entityManager.persist(testUser);
        entityManager.flush();

        SpendingCategory spendingCategory = SpendingCategory.builder()
                .userId(testUser.getId())
                .name("식비")
                .isStupidCostTarget(true)
                .build();
        savedCategory = spendingCategoryRepository.save(spendingCategory);
    }

    @Test
    @DisplayName("거래내역_엔티티를_성공적으로_저장하고_조회한다")
    void 거래내역_엔티티를_성공적으로_저장하고_조회한다() {
        // given
        Transaction newTransaction = Transaction.builder()
                .userId(testUser.getId())
                .amount(new BigDecimal("10000"))
                .vendor("테스트 식당")
                .transactionTime(LocalDateTime.now())
                .category(savedCategory)
                .isStupidCost(false)
                .originalSmsContent("SMS 원문 내용")
                .memo("메모")
                .build();

        // when
        Transaction savedTransaction = transactionRepository.save(newTransaction);

        // then
        assertThat(savedTransaction.getId()).isNotNull();
        assertThat(savedTransaction.getVendor()).isEqualTo("테스트 식당");
        assertThat(savedTransaction.getCategory()).isNotNull();
        assertThat(savedTransaction.getCategory().getName()).isEqualTo("식비");
    }

    @Test
    @DisplayName("거래내역을_페이지단위로_조회한다")
    void 거래내역을_페이지단위로_조회한다() {
        // given
        transactionRepository.save(Transaction.builder().userId(testUser.getId()).vendor("store1").amount(BigDecimal.ONE).transactionTime(LocalDateTime.now()).originalSmsContent("sms").build());
        transactionRepository.save(Transaction.builder().userId(testUser.getId()).vendor("store2").amount(BigDecimal.ONE).transactionTime(LocalDateTime.now()).originalSmsContent("sms").build());

        // when
        Page<Transaction> result = transactionRepository.findAll(PageRequest.of(0, 1));

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(1);
    }
}
