package com.mk.www.smsmonitor.transaction.infrastructure.analysis;

import com.mk.www.smsmonitor.transaction.domain.Transaction;
import com.mk.www.smsmonitor.transaction.domain.StupidCostStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
public class LateNightPurchaseStrategy implements StupidCostStrategy {

    private static final LocalTime LATE_NIGHT_START = LocalTime.of(22, 0);
    private static final List<String> CONVENIENCE_STORES = Arrays.asList("GS25", "CU", "7-ELEVEN");

    @Override
    public boolean isStupidCost(Transaction transaction) {
        if (transaction.getTransactionTime().toLocalTime().isAfter(LATE_NIGHT_START)) {
            return CONVENIENCE_STORES.stream()
                    .anyMatch(store -> transaction.getVendor().contains(store));
        }
        return false;
    }

    @Override
    public String getStrategyName() {
        return "Late Night Convenience Store Purchase";
    }
}
