package com.mk.www.smsmonitor.transaction.application;

import com.mk.www.smsmonitor.transaction.application.SmsParser;
import com.mk.www.smsmonitor.transaction.domain.Transaction;
import com.mk.www.smsmonitor.transaction.domain.StupidCostStrategy;
import com.mk.www.smsmonitor.transaction.api.dto.SmsRequest;
import com.mk.www.smsmonitor.transaction.infrastructure.persistence.RawSmsLogEntity;
import com.mk.www.smsmonitor.transaction.infrastructure.persistence.RawSmsLogJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SmsService {

    private final TransactionService transactionService;
    private final List<StupidCostStrategy> stupidCostStrategies;
    private final List<SmsParser> parsers;
    private final RawSmsLogJpaRepository rawSmsLogRepository;

    public boolean processNewSms(SmsRequest request, String loginId) {
        String smsContent = request.getMessage();
        String sender = request.getSender();

        if (smsContent == null || smsContent.trim().isEmpty()) {
            log.warn("수신된 SMS 내용이 비어있습니다. (User: {})", loginId);
            return false;
        }

        // [중복 방지 로직 추가] 10초 이내에 동일한 유저가 동일한 발신자로부터 동일한 메시지를 보낸 경우 무시
        LocalDateTime tenSecondsAgo = LocalDateTime.now().minusSeconds(10);
        boolean isDuplicate = rawSmsLogRepository.existsByLoginIdAndSenderAndMessageAndReceivedAtAfter(
                loginId, sender, smsContent, tenSecondsAgo);

        if (isDuplicate) {
            log.info("📢 [Deduplication] 중복된 메시지 수신 무시: {} (User: {})", smsContent, loginId);
            return false;
        }

        // 원본 로그 저장
        try {
            saveRawSmsToDb(request, loginId);
        } catch (Exception e) {
            log.error("원본 로그 저장 중 에러 발생: {}", e.getMessage());
        }

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
        
        transactionService.save(transaction, loginId);
        return true;
    }

    private void saveRawSmsToDb(SmsRequest request, String loginId) {
        RawSmsLogEntity logEntity = RawSmsLogEntity.builder()
                .sender(request.getSender() != null ? request.getSender() : "UNKNOWN")
                .message(request.getMessage() != null ? request.getMessage() : "EMPTY")
                .loginId(loginId != null ? loginId : "ANONYMOUS")
                .receivedAt(LocalDateTime.now())
                .build();
        rawSmsLogRepository.save(logEntity);
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
