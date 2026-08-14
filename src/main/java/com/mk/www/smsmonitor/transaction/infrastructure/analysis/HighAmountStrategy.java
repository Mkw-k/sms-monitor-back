package com.mk.www.smsmonitor.transaction.infrastructure.analysis;

import com.mk.www.smsmonitor.transaction.domain.Transaction;
import com.mk.www.smsmonitor.transaction.domain.StupidCostStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HighAmountStrategy implements StupidCostStrategy {

    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("50000");
    private static final String TARGET_CATEGORY = "식비";

    @Override
    public boolean isStupidCost(Transaction transaction) {
        if (transaction.getCategory() != null && TARGET_CATEGORY.equals(transaction.getCategory())) {
            return transaction.getAmount().compareTo(HIGH_AMOUNT_THRESHOLD) > 0;
        }
        return false;
    }

    @Override
    public String getStrategyName() {
        return "High Amount Food Purchase";
    }
}
