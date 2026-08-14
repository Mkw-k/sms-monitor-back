package com.mk.www.smsmonitor.transaction.application;

import com.mk.www.smsmonitor.transaction.domain.Transaction;

import java.util.Optional;

public interface SmsParser {
    Optional<Transaction> parse(String smsContent);
    boolean supports(String smsContent);
}
