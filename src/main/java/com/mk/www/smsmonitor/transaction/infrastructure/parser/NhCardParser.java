package com.mk.www.smsmonitor.transaction.infrastructure.parser;

import com.mk.www.smsmonitor.transaction.application.SmsParser;
import com.mk.www.smsmonitor.transaction.domain.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class NhCardParser implements SmsParser {

    // 한 줄 포맷과 다중 행 포맷을 모두 지원하는 유연한 정규식
    private static final Pattern NH_CARD_PATTERN = Pattern.compile(
            "NH카드(?<cardNum>.*?)\\s*승인\\s+(?<name>.*?)\\s+(?<amount>[\\d,]+)\\s*(?:원|체크)?\\s+(?<date>\\d{2}/\\d{2}\\s+\\d{2}:\\d{2})\\s+(?<vendor>.*?)($|\\s+잔액|\\s+누적)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    @Override
    public Optional<Transaction> parse(String smsContent) {
        if (!supports(smsContent)) {
            return Optional.empty();
        }

        Matcher matcher = NH_CARD_PATTERN.matcher(smsContent);

        if (matcher.find()) {
            try {
                // 1. 데이터 추출 (그룹 이름으로 가져오기)
                String cardNum = matcher.group("cardNum").trim();
                String name = matcher.group("name").trim();
                String amountStr = matcher.group("amount").replace(",", "");
                String dateTimeStr = matcher.group("date");
                String vendor = matcher.group("vendor").trim();

                // 2. 데이터 변환
                BigDecimal amount = new BigDecimal(amountStr);

                DateTimeFormatter formatter = new java.time.format.DateTimeFormatterBuilder()
                        .appendPattern("MM/dd HH:mm")
                        .parseDefaulting(java.time.temporal.ChronoField.YEAR, LocalDateTime.now().getYear())
                        .toFormatter();
                LocalDateTime transactionTime = LocalDateTime.parse(dateTimeStr, formatter);

                // 3. 객체 생성
                return Optional.of(Transaction.builder()
                        .cardNumber(cardNum)
                        .name(name)
                        .amount(amount)
                        .vendor(vendor)
                        .transactionTime(transactionTime)
                        .originalSmsContent(smsContent)
                        .isStupidCost(false)
                        .type(Transaction.TransactionType.EXPENSE)
                        .isFixedExpense(false)
                        .isManual(false)
                        .isIgnored(false)
                        .build());

            } catch (Exception e) {
                log.error("NH카드 SMS 파싱 에러: {}", e.getMessage(), e);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(String smsContent) {
        return smsContent != null && smsContent.contains("NH카드") && smsContent.contains("승인") && !smsContent.contains("승인거절");
    }
}
