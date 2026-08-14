package com.mk.www.smsmonitor.transaction.infrastructure.persistence;

import com.mk.www.smsmonitor.transaction.domain.Transaction;
import com.mk.www.smsmonitor.transaction.domain.Transaction.TransactionType;
import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionMapper {

    public Transaction toDomain(TransactionEntity entity) {
        if (entity == null) return null;
        return Transaction.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .amount(entity.getAmount())
                .vendor(entity.getVendor())
                .transactionTime(entity.getTransactionTime())
                .category(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .isStupidCost(entity.isStupidCost())
                .type(entity.getType() != null ? entity.getType() : TransactionType.EXPENSE)
                .isFixedExpense(entity.isFixedExpense())
                .isManual(entity.isManual())
                .isIgnored(entity.isIgnored())
                .originalSmsContent(entity.getOriginalSmsContent())
                .memo(entity.getMemo())
                .cardNumber(entity.getCardNumber())
                .name(entity.getName())
                .build();
    }

    public TransactionEntity toEntity(Transaction domain) {
        if (domain == null) return null;
        TransactionEntity entity = new TransactionEntity();
        entity.setId(domain.getId());
        if (domain.getUserId() != null) {
            UserEntity user = new UserEntity();
            user.setId(domain.getUserId());
            entity.setUser(user);
        }
        entity.setAmount(domain.getAmount());
        entity.setVendor(domain.getVendor());
        entity.setTransactionTime(domain.getTransactionTime());
        entity.setStupidCost(domain.isStupidCost());
        entity.setType(domain.getType() != null ? domain.getType() : TransactionType.EXPENSE);
        entity.setFixedExpense(domain.isFixedExpense());
        entity.setManual(domain.isManual());
        entity.setIgnored(domain.isIgnored());
        entity.setOriginalSmsContent(domain.getOriginalSmsContent() != null ? domain.getOriginalSmsContent() : "MANUAL");
        entity.setMemo(domain.getMemo());
        entity.setName(domain.getName());
        entity.setCardNumber(domain.getCardNumber());
        return entity;
    }
}
