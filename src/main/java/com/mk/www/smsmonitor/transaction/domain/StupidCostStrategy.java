package com.mk.www.smsmonitor.transaction.domain;

import com.mk.www.smsmonitor.transaction.domain.Transaction;

public interface StupidCostStrategy {
    boolean isStupidCost(Transaction transaction);
    String getStrategyName();
}
