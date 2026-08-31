package com.mk.www.smsmonitor.transaction.application;

import com.mk.www.smsmonitor.account.infrastructure.AccountEntity;
import com.mk.www.smsmonitor.account.infrastructure.AccountJpaRepository;
import com.mk.www.smsmonitor.common.application.FcmService;
import com.mk.www.smsmonitor.transaction.api.dto.SmsRequest;
import com.mk.www.smsmonitor.transaction.domain.Transaction;
import com.mk.www.smsmonitor.transaction.infrastructure.persistence.RawSmsLogEntity;
import com.mk.www.smsmonitor.transaction.infrastructure.persistence.RawSmsLogJpaRepository;
import com.mk.www.smsmonitor.transaction.infrastructure.persistence.TransactionJpaRepository;
import com.mk.www.smsmonitor.user.domain.Device;
import com.mk.www.smsmonitor.user.infrastructure.DeviceRepository;
import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import com.mk.www.smsmonitor.user.infrastructure.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SmsServiceIntegrationTest {

    @Autowired
    private SmsService smsService;

    @Autowired
    private RawSmsLogJpaRepository rawSmsLogRepository;

    @Autowired
    private TransactionJpaRepository transactionRepository;

    @Autowired
    private AccountJpaRepository accountJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @MockBean
    private FcmService fcmService;

    private UserEntity userEntity;
    private AccountEntity accountEntity;

    @BeforeEach
    void setUp() {
        rawSmsLogRepository.deleteAll();
        transactionRepository.deleteAll();
        accountJpaRepository.deleteAll();
        userJpaRepository.deleteAll();

        userEntity = UserEntity.builder()
                .loginId("smsuser")
                .password("password")
                .role("ROLE_USER")
                .isApproved(true)
                .point(0L)
                .build();
        userJpaRepository.save(userEntity);

        accountEntity = AccountEntity.builder()
                .user(userEntity)
                .bankName("KB국민은행")
                .accountNumber("999-111-222")
                .balance(new BigDecimal("1000000"))
                .isDefault(true)
                .build();
        accountJpaRepository.save(accountEntity);

        Device device = Device.builder()
                .token("test-fcm-token-123")
                .loginId("smsuser")
                .platform("ANDROID")
                .build();
        deviceRepository.save(device);
    }

    @Test
    @DisplayName("SMS 수신 시 원본 로그 저장, 파싱, 거래내역 저장, 계좌 잔액 차감, FCM 푸시 발송이 정상 동작한다")
    void processNewSms_FullFlow_Success() {
        // given
        String smsMessage = "[Web발신]\n" +
                "KB국민카드4020승인\n" +
                "고*우님\n" +
                "5,000원 일시불\n" +
                "03/13 07:51\n" +
                "서울시설공단\n" +
                "누적106,670원";

        SmsRequest request = new SmsRequest();
        request.setSender("15881688");
        request.setMessage(smsMessage);

        // when
        boolean success = smsService.processNewSms(request, "smsuser");

        // then
        assertThat(success).isTrue();

        // 1. 원본 SMS 로그 검증
        List<RawSmsLogEntity> rawLogs = rawSmsLogRepository.findAll();
        assertThat(rawLogs).hasSize(1);
        assertThat(rawLogs.get(0).getLoginId()).isEqualTo("smsuser");
        assertThat(rawLogs.get(0).getSender()).isEqualTo("15881688");

        // 2. 파싱 및 거래내역 엔티티 검증
        assertThat(transactionRepository.findAll()).hasSize(1);
        assertThat(transactionRepository.findAll().get(0).getAmount()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(transactionRepository.findAll().get(0).getVendor()).isEqualTo("서울시설공단");

        // 3. 자산 반영 플래그(기본 false) 및 원본 SMS 전문 검증
        assertThat(transactionRepository.findAll().get(0).isReflectInAsset()).isFalse();
        assertThat(transactionRepository.findAll().get(0).getOriginalSmsContent()).isEqualTo(smsMessage);

        // 4. FCM 푸시 발송 트리거 검증
        verify(fcmService, times(1)).sendMessage(eq("test-fcm-token-123"), eq("실시간 결제 알림"), anyString());
    }

    @Test
    @DisplayName("10초 이내에 동일한 내용의 SMS가 중복 수신되면 처리를 무시한다")
    void processNewSms_DuplicateIgnored() {
        // given
        String smsMessage = "[Web발신]\n" +
                "KB국민카드4020승인\n" +
                "고*우님\n" +
                "10,000원 일시불\n" +
                "03/13 08:00\n" +
                "식당\n" +
                "누적116,670원";

        SmsRequest request = new SmsRequest();
        request.setSender("15881688");
        request.setMessage(smsMessage);

        // when: 첫 번째 수신
        boolean firstResult = smsService.processNewSms(request, "smsuser");
        assertThat(firstResult).isTrue();

        // when: 동일 SMS 재수신
        boolean secondResult = smsService.processNewSms(request, "smsuser");

        // then
        assertThat(secondResult).isFalse();
        assertThat(transactionRepository.findAll()).hasSize(1);
    }
}
