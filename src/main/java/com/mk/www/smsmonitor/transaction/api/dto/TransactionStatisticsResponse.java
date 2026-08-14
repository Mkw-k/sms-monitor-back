package com.mk.www.smsmonitor.transaction.api.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionStatisticsResponse {
    private List<StatEntry> entries;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StatEntry {
        private String label; // e.g., "2024-05-13", "May", "2024"
        private BigDecimal income;
        private BigDecimal expense;
    }
}
