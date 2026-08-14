package com.mk.www.smsmonitor.transaction.api.dto;

import com.mk.www.smsmonitor.transaction.domain.Transaction.TransactionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TransactionSearchRequest {
    private Long id; // 개별 조회용 ID 추가
    private String vendor;
    private TransactionType type;
    private Boolean isStupidCost;
    private Boolean isFixedExpense;
    private Boolean isIgnored;
    private Boolean isDeleted;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;
    
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Long categoryId;
}
