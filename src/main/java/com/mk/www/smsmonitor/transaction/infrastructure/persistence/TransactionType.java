package com.mk.www.smsmonitor.transaction.infrastructure.persistence;

/**
 * packageName    : com.mk.www.smsmonitor.transaction.infrastructure.persistence
 * fileName       : TransactionType
 * author         : rhaud
 * date           : 2026-04-08
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-08        rhaud       최초 생성
 */
public enum TransactionType {
    EXPENSE("지출"),
    INCOME("수입");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }
}
