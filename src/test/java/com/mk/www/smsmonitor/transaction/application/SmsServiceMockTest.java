package com.mk.www.smsmonitor.transaction.application;

import com.mk.www.smsmonitor.transaction.domain.Transaction;
import com.mk.www.smsmonitor.transaction.domain.StupidCostStrategy;
import com.mk.www.smsmonitor.transaction.api.dto.SmsRequest;
import com.mk.www.smsmonitor.transaction.infrastructure.persistence.RawSmsLogJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsServiceMockTest {

    private SmsService smsService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private StupidCostStrategy stupidCostStrategy; // 리스트 대신 개별 전략 모킹

    @Mock
    private SmsParser kbCardParser;

    @Mock
    private SmsParser nhCardParser;

    @Mock
    private RawSmsLogJpaRepository rawSmsLogRepository;

    @Mock
    private com.mk.www.smsmonitor.user.infrastructure.DeviceRepository deviceRepository;

    @Mock
    private com.mk.www.smsmonitor.common.application.FcmService fcmService;

    @BeforeEach
    void setUp() {
        smsService = new SmsService(
                transactionService,
                List.of(stupidCostStrategy),
                List.of(kbCardParser, nhCardParser),
                rawSmsLogRepository,
                deviceRepository,
                fcmService
        );
    }

    @Test
    @DisplayName("SMS_처리_성공_시_파싱_분석_저장_서비스를_모두_호출한다")
    void SMS_처리_성공_시_파싱_분석_저장_서비스를_모두_호출한다() {
        // given
        String smsContent = "SMS content";
        SmsRequest request = new SmsRequest();
        request.setMessage(smsContent);
        Transaction mockTransaction = mock(Transaction.class);

        when(kbCardParser.supports(smsContent)).thenReturn(true);
        when(kbCardParser.parse(smsContent)).thenReturn(Optional.of(mockTransaction));

        // when
        boolean result = smsService.processNewSms(request, "user");

        // then
        assertThat(result).isTrue();
        verify(kbCardParser, times(1)).supports(smsContent);
        verify(kbCardParser, times(1)).parse(smsContent);
        verify(mockTransaction, times(1)).analyze(any());
        verify(transactionService, times(1)).save(mockTransaction, "user");
        verify(deviceRepository, times(1)).findByLoginId("user");
    }

    @Test
    @DisplayName("SMS_파싱_실패_시_다른_서비스는_호출되지_않는다")
    void SMS_파싱_실패_시_다른_서비스는_호출되지_않는다() {
        // given
        String smsContent = "Failed SMS content";
        SmsRequest request = new SmsRequest();
        request.setMessage(smsContent);

        when(kbCardParser.supports(smsContent)).thenReturn(false);
        when(nhCardParser.supports(smsContent)).thenReturn(false);

        // when
        boolean result = smsService.processNewSms(request, "user");

        // then
        assertThat(result).isFalse();
        verify(transactionService, never()).save(any(), anyString());
    }
}
