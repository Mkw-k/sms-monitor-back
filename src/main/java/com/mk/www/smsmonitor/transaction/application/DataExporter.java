package com.mk.www.smsmonitor.transaction.application;

import com.mk.www.smsmonitor.transaction.domain.Transaction;

public interface DataExporter {
    void export(Transaction transaction);
}
