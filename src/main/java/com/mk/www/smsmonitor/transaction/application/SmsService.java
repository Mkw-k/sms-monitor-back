package com.mk.www.smsmonitor.transaction.application;

import com.mk.www.smsmonitor.transaction.application.SmsParser;
import com.mk.www.smsmonitor.transaction.domain.Transaction;
import com.mk.www.smsmonitor.transaction.domain.StupidCostStrategy;
import com.mk.www.smsmonitor.transaction.api.dto.SmsRequest;
import com.mk.www.smsmonitor.transaction.infrastructure.persistence.RawSmsLogEntity;
import com.mk.www.smsmonitor.transaction.infrastructure.persistence.RawSmsLogJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.mk.www.smsmonitor.common.application.FcmService;
import com.mk.www.smsmonitor.user.domain.Device;
import com.mk.www.smsmonitor.user.infrastructure.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final TransactionService transactionService;
    private final List<StupidCostStrategy> stupidCostStrategies;
    private final List<SmsParser> parsers;
    private final RawSmsLogJpaRepository rawSmsLogRepository;
    private final DeviceRepository deviceRepository;
    private final FcmService fcmService;

    public boolean processNewSms(SmsRequest request, String loginId) {
        String smsContent = request.getMessage();
        String sender = request.getSender();

        if (smsContent == null || smsContent.trim().isEmpty()) {
            log.warn("수신된 SMS 내용이 비어있습니다. (User: {})", loginId);
            return false;
        }

        // 1. 중복 체크
        if (isDuplicateSms(loginId, sender, smsContent)) {
            log.info("📢 [Deduplication] 중복된 메시지 수신 무시: {} (User: {})", smsContent, loginId);
            return false;
        }

        // 2. 원본 로그 저장 (독립 작업)
        try {
            saveRawSmsToDb(request, loginId);
        } catch (Exception e) {
            log.error("원본 로그 저장 중 에러 발생: {}", e.getMessage());
        }

        // 3. 파싱 및 분석 (순수 비즈니스 연산)
        Optional<Transaction> transactionOptional = parseSms(smsContent);
        log.info("sms 전문 >>> {} (User: {})", smsContent, loginId);

        if (transactionOptional.isEmpty()) {
            log.warn("파싱 실패: 해당 패턴에 맞는 파서가 없습니다. 전문: {}", smsContent);
            return false;
        }

        Transaction transaction = transactionOptional.get();
        transaction.analyze(stupidCostStrategies);

        if (transaction.getVendor() != null && (transaction.getVendor().contains("지에스") || transaction.getVendor().contains("GS25") || transaction.getVendor().contains("편의점"))) {
            transaction.updateStupidCost(true);
        }

        // 4. 거래내역 저장 및 자산 반영 (원자적 트랜잭션)
        recordTransaction(transaction, loginId);

        // 5. 푸시 알림 발송 (외부 I/O, 트랜잭션 밖에서 실행)
        sendPushNotifications(request, loginId);

        return true;
    }

    public boolean isDuplicateSms(String loginId, String sender, String message) {
        LocalDateTime tenSecondsAgo = LocalDateTime.now().minusSeconds(10);
        return rawSmsLogRepository.existsByLoginIdAndSenderAndMessageAndReceivedAtAfter(
                loginId, sender, message, tenSecondsAgo);
    }

    @Transactional
    public void saveRawSmsToDb(SmsRequest request, String loginId) {
        RawSmsLogEntity logEntity = RawSmsLogEntity.builder()
                .sender(request.getSender() != null ? request.getSender() : "UNKNOWN")
                .message(request.getMessage() != null ? request.getMessage() : "EMPTY")
                .loginId(loginId != null ? loginId : "ANONYMOUS")
                .receivedAt(LocalDateTime.now())
                .build();
        rawSmsLogRepository.save(logEntity);
    }

    @Transactional
    public Transaction recordTransaction(Transaction transaction, String loginId) {
        return transactionService.save(transaction, loginId);
    }

    public void sendPushNotifications(SmsRequest request, String loginId) {
        try {
            List<Device> devices = deviceRepository.findByLoginId(loginId);
            for (Device device : devices) {
                fcmService.sendMessage(device.getToken(), "실시간 결제 알림", request.getSender() + ": " + request.getMessage());
            }
        } catch (Exception e) {
            log.error("푸시 알림 전송 중 에러 발생: {}", e.getMessage(), e);
        }
    }

    private Optional<Transaction> parseSms(String smsContent) {
        for (SmsParser parser : parsers) {
            if (parser.supports(smsContent)) {
                return parser.parse(smsContent);
            }
        }
        return Optional.empty();
    }
}
