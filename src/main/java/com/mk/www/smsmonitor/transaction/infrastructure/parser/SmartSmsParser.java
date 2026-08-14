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
public class SmartSmsParser implements SmsParser {

    private static final Pattern CARD_APPROVAL_PATTERN = Pattern.compile("(.*?)카드(\\d+)?승인");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("([\\d,]+)원");
    private static final Pattern DATE_TIME_PATTERN = Pattern.compile("(\\d{2}/\\d{2})\\s+(\\d{2}:\\d{2})");

    @Override
    public Optional<Transaction> parse(String smsContent) {
        if (!supports(smsContent)) {
            return Optional.empty();
        }

        try {
            String cleanContent = smsContent.replace("[Web발신]", "").trim();
            String[] lines = cleanContent.split("\\r?\\n");
            
            String cardNumber = null;
            String name = null;
            BigDecimal amount = null;
            LocalDateTime transactionTime = null;
            String vendor = null;

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;

                log.debug("Parsing line [{}]: {}", i, line);

                // 1. 헤더 분석 (카드사명, 카드번호, 승인여부)
                Matcher cardMatcher = CARD_APPROVAL_PATTERN.matcher(line.replace(" ", ""));
                if (cardMatcher.find() && cardNumber == null) {
                    cardNumber = cardMatcher.group(2); // 카드 뒤 숫자 (있을 경우)
                    log.debug("Found card number: {}", cardNumber);
                    continue;
                }

                // 2. 금액 및 결제타입 분석
                Matcher amountMatcher = AMOUNT_PATTERN.matcher(line);
                if (amountMatcher.find() && amount == null) {
                    amount = new BigDecimal(amountMatcher.group(1).replace(",", ""));
                    log.debug("Found amount: {}", amount);
                    continue;
                }

                // 3. 일시 분석
                Matcher dateMatcher = DATE_TIME_PATTERN.matcher(line);
                if (dateMatcher.find() && transactionTime == null) {
                    String dateStr = dateMatcher.group(1);
                    String timeStr = dateMatcher.group(2);
                    
                    DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                            .appendPattern("MM/dd HH:mm")
                            .parseDefaulting(ChronoField.YEAR, LocalDateTime.now().getYear())
                            .toFormatter();
                    
                    transactionTime = LocalDateTime.parse(dateStr + " " + timeStr, formatter);
                    log.debug("Found transaction time: {}", transactionTime);

                    // 9. 상호명 분석 (일시 이후의 줄에서 탐색)
                    for (int j = i + 1; j < lines.length; j++) {
                        String nextLine = lines[j].trim();
                        if (!nextLine.isEmpty() && !nextLine.contains("누적") && !nextLine.contains("일시불") && !nextLine.contains("승인")) {
                            vendor = nextLine;
                            log.debug("Found vendor: {}", vendor);
                            break;
                        }
                    }
                    continue;
                }

                // 4 & 5. 이름 분석 (짧은 줄이거나 '님'으로 끝나는 경우)
                if (name == null && amount == null && transactionTime == null) {
                    if (line.endsWith("님")) {
                        name = line.substring(0, line.length() - 1);
                        log.debug("Found name (suffix): {}", name);
                    } else if (line.length() >= 2 && line.length() <= 5) {
                        name = line;
                        log.debug("Found name (length): {}", name);
                    }
                }
            }

            if (amount != null && transactionTime != null) {
                // 상호명을 못 찾았을 경우 기본값 설정
                if (vendor == null || vendor.isEmpty()) {
                    vendor = "기타 상점";
                }

                return Optional.of(Transaction.builder()
                        .amount(amount)
                        .vendor(vendor)
                        .transactionTime(transactionTime)
                        .cardNumber(cardNumber)
                        .name(name)
                        .originalSmsContent(smsContent)
                        .isStupidCost(false)
                        .build());
            }

        } catch (Exception e) {
            log.error("SmartSmsParser parsing failed: {}", e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public boolean supports(String smsContent) {
        return smsContent != null && smsContent.contains("카드") && smsContent.contains("승인");
    }
}
