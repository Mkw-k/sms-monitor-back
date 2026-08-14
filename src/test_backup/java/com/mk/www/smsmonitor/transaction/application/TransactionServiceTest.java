package com.mk.www.smsmonitor.transaction.application;

import com.mk.www.smsmonitor.transaction.application.DataExporter;
import com.mk.www.smsmonitor.transaction.domain.Transaction;
import com.mk.www.smsmonitor.transaction.domain.TransactionRepository;
import com.mk.www.smsmonitor.transaction.api.dto.MemoRequest;
import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import com.mk.www.smsmonitor.user.infrastructure.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private TransactionService transactionService;

    @Mock
    private DataExporter exporter1;
    @Mock
    private DataExporter exporter2;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserJpaRepository userJpaRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(Arrays.asList(exporter1, exporter2), transactionRepository, userJpaRepository);
        testUser = UserEntity.builder().id(1L).loginId("user").build();
    }

    @Test
    @DisplayName("거래내역_저장_요청시_리포지토리에_저장하고_모든_Exporter가_호출되지_않는다")
    void 거래내역_저장_요청시_리포지토리에_저장하고_모든_Exporter가_호출되지_않는다() {
        // given
        Transaction transaction = Transaction.builder().build();
        when(userJpaRepository.findByLoginId("user")).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Transaction saved = transactionService.save(transaction, "user");

        // then
        assertThat(saved.getUserId()).isEqualTo(testUser.getId());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(exporter1, never()).export(any(Transaction.class));
        verify(exporter2, never()).export(any(Transaction.class));
    }

    @Test
    @DisplayName("메모_수정_요청_시_본인의_거래내역을_찾아_저장한다")
    void 메모_수정_요청_시_본인의_거래내역을_찾아_저장한다() {
        // given
        MemoRequest memoRequest = new MemoRequest();
        memoRequest.setMemo("새로운 메모");
        Transaction originalTransaction = Transaction.builder().id(100L).userId(1L).memo("옛날 메모").build();

        when(userJpaRepository.findByLoginId("user")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(100L)).thenReturn(Optional.of(originalTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Optional<Transaction> result = transactionService.updateMemo(100L, memoRequest, "user");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getMemo()).isEqualTo("새로운 메모");
        verify(transactionRepository, times(1)).findById(100L);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
}
