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
public class KbCardParser implements SmsParser {

    // 기존에 잘 작동하던 포맷을 기반으로 보강
    private static final Pattern KB_CARD_PATTERN = Pattern.compile(
            "KB국민(?:카드|체크)(?<cardNum>.*?)승인[\\s\\n]+" +
            "(?<name>.*?)[\\s\\n]+" +
            "(?<amount>[\\d,]+)원.*?[\\s\\n]+" +
            "(?<date>\\d{2}/\\d{2}\\s+\\d{2}:\\d{2})[\\s\\n]+" +
            "(?<vendor>.*?)([\\s\\n]+누적|$)",
            Pattern.DOTALL
    );

    @Override
    public Optional<Transaction> parse(String smsContent) {
        if (!supports(smsContent)) {
            return Optional.empty();
        }

        String cleanContent = smsContent.replace("[Web발신]", "").trim();

        Matcher matcher = KB_CARD_PATTERN.matcher(cleanContent);
        if (matcher.find()) {
            try {
                String cardNum = matcher.group("cardNum").trim();
                String name = matcher.group("name").trim();
                String amountStr = matcher.group("amount").replace(",", "");
                BigDecimal amount = new BigDecimal(amountStr);
                String dateTimeStr = matcher.group("date");
                String vendor = matcher.group("vendor").trim();

                DateTimeFormatter formatter = new java.time.format.DateTimeFormatterBuilder()
                        .appendPattern("MM/dd HH:mm")
                        .parseDefaulting(java.time.temporal.ChronoField.YEAR, LocalDateTime.now().getYear())
                        .toFormatter();
                LocalDateTime transactionTime = LocalDateTime.parse(dateTimeStr, formatter);

                return Optional.of(Transaction.builder()
                        .cardNumber(cardNum)
                        .name(name)
                        .amount(amount)
                        .vendor(vendor)
                        .transactionTime(transactionTime)
                        .originalSmsContent(smsContent)
                        .isStupidCost(false)
                        .type(Transaction.TransactionType.EXPENSE) // 필수 필드 추가
                        .isFixedExpense(false)
                        .isManual(false)
                        .isIgnored(false)
                        .build());
            } catch (Exception e) {
                log.error("KB카드 SMS 파싱 에러: {}", e.getMessage());
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(String smsContent) {
        return smsContent != null && (smsContent.contains("KB국민") || smsContent.contains("국민카드")) && smsContent.contains("승인");
    }
}
