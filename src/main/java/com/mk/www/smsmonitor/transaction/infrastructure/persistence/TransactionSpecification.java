package com.mk.www.smsmonitor.transaction.infrastructure.persistence;

import com.mk.www.smsmonitor.transaction.api.dto.TransactionSearchRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {
    public static Specification<TransactionEntity> filterBy(TransactionSearchRequest request, Long userId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));

            // ID 필터 (상세 페이지용)
            if (request.getId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), request.getId()));
            }
            
            // 삭제된 내역 필터: 포함(true)이 아니면 무조건 삭제 안 된 것만 표시
            if (request.getIsDeleted() != null && request.getIsDeleted()) {
                // 포함 상태: 필터 안 걸음 (전체 다 나옴)
            } else {
                predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));
            }

            // 무시된 내역 필터: 포함(true)이 아니면 무조건 무시 안 된 것만 표시
            if (request.getIsIgnored() != null && request.getIsIgnored()) {
                // 포함 상태: 필터 안 걸음
            } else {
                predicates.add(criteriaBuilder.equal(root.get("isIgnored"), false));
            }

            // 멍청비용 필터: 선택 시 멍청비용만 표시
            if (request.getIsStupidCost() != null && request.getIsStupidCost()) {
                predicates.add(criteriaBuilder.equal(root.get("isStupidCost"), true));
            }

            if (request.getVendor() != null && !request.getVendor().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("vendor"), "%" + request.getVendor() + "%"));
            }
            if (request.getType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), request.getType()));
            }
            if (request.getIsFixedExpense() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isFixedExpense"), request.getIsFixedExpense()));
            }
            if (request.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transactionTime"), request.getStartDate()));
            }
            if (request.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transactionTime"), request.getEndDate()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
