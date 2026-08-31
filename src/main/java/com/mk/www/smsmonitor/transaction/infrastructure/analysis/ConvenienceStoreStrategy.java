package com.mk.www.smsmonitor.transaction.infrastructure.analysis;

import com.mk.www.smsmonitor.transaction.domain.StupidCostStrategy;
import com.mk.www.smsmonitor.transaction.domain.Transaction;
import org.springframework.stereotype.Component;

@Component
public class ConvenienceStoreStrategy implements StupidCostStrategy {

    @Override
    public boolean isStupidCost(Transaction transaction) {
        if (transaction == null || transaction.getVendor() == null) {
            return false;
        }
        String vendor = transaction.getVendor();
        return vendor.contains("지에스") || vendor.contains("GS25") || vendor.contains("편의점");
    }

    @Override
    public String getStrategyName() {
        return "Convenience Store Purchase";
    }
}
