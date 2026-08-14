package com.mk.www.smsmonitor.transaction.application;

import com.mk.www.smsmonitor.transaction.api.dto.TransactionSearchRequest;
import com.mk.www.smsmonitor.transaction.domain.Transaction;
import com.mk.www.smsmonitor.transaction.infrastructure.persistence.TransactionEntity;
import com.mk.www.smsmonitor.transaction.infrastructure.persistence.TransactionJpaRepository;
import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import com.mk.www.smsmonitor.user.infrastructure.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionJpaRepository transactionRepository;

    @Mock
    private UserJpaRepository userJpaRepository;

    @InjectMocks
    private TransactionService transactionService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(1L);
        user.setLoginId("testUser");
    }

    @Test
    void getUserTransactions_ShouldReturnPage() {
        // Given
        TransactionSearchRequest request = new TransactionSearchRequest();
        PageRequest pageable = PageRequest.of(0, 10);
        when(userJpaRepository.findByLoginId("testUser")).thenReturn(Optional.of(user));
        
        TransactionEntity entity = new TransactionEntity();
        entity.setUser(user);
        entity.setAmount(new java.math.BigDecimal("1000"));
        entity.setVendor("Test Vendor");
        entity.setTransactionTime(java.time.LocalDateTime.now());
        entity.setType(Transaction.TransactionType.EXPENSE);
        
        Page<TransactionEntity> page = new PageImpl<>(Collections.singletonList(entity));
        when(transactionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // When
        Page<Transaction> result = transactionService.getUserTransactions("testUser", request, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getVendor()).isEqualTo("Test Vendor");
    }
}
