package com.mk.www.smsmonitor.transaction.infrastructure.parser;

import com.mk.www.smsmonitor.transaction.application.SmsParser;
import com.mk.www.smsmonitor.transaction.domain.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class HyundaiCardParser implements SmsParser {

    // RCS 포맷에 유연하게 대응하도록 정규식 개선
    private static final Pattern HYUNDAI_CARD_PATTERN = Pattern.compile(
            "Hyundai Mobility카드\\s*승인[\\s\\n]*" +
            ".*?" + // 이름 등 무시
            "(?<amount>[\\d,]+)원[\\s\\n]*" +
            ".*?" + // 일시불 등 무시
            "(?<date>\\d{1,2}/\\d{1,2}\\s+\\d{1,2}:\\d{2})[\\s\\n]+" +
            "(?<vendor>.*?)[\\s\\n]+" +
            "누적",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    @Override
    public Optional<Transaction> parse(String smsContent) {
        if (!supports(smsContent)) {
            return Optional.empty();
        }

        Matcher matcher = HYUNDAI_CARD_PATTERN.matcher(smsContent);
        if (matcher.find()) {
            try {
                String amountStr = matcher.group("amount").replace(",", "");
                BigDecimal amount = new BigDecimal(amountStr);
                String dateTimeStr = matcher.group("date");
                String vendor = matcher.group("vendor").trim();

                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .appendPattern("M/d H:m")
                        .parseDefaulting(ChronoField.YEAR, LocalDateTime.now().getYear())
                        .toFormatter();
                
                LocalDateTime transactionTime = LocalDateTime.parse(dateTimeStr, formatter);

                return Optional.of(Transaction.builder()
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
                log.error("Failed to parse Hyundai Card SMS: {}", e.getMessage());
                return Optional.empty();
            }
        } else {
            log.warn("Hyundai Card pattern not matched for content: {}", smsContent);
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(String smsContent) {
        return smsContent != null && smsContent.contains("Hyundai Mobility카드") && smsContent.contains("승인");
    }
}
