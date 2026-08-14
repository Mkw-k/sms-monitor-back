package com.mk.www.smsmonitor.transaction.infrastructure.exporter;

import com.mk.www.smsmonitor.transaction.application.DataExporter;
import com.mk.www.smsmonitor.transaction.domain.Transaction;
import com.mk.www.smsmonitor.transaction.domain.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseExporter implements DataExporter {

    private final TransactionRepository transactionRepository;

    @Override
    public void export(Transaction transaction) {
        transactionRepository.save(transaction);
    }
}
