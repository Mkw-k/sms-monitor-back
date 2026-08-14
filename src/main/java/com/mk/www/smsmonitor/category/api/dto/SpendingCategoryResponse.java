package com.mk.www.smsmonitor.category.api.dto;

import com.mk.www.smsmonitor.category.domain.SpendingCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SpendingCategoryResponse {
    private Long id;
    private String name;
    private boolean isStupidCostTarget;

    public static SpendingCategoryResponse from(SpendingCategory spendingCategory) {
        return SpendingCategoryResponse.builder()
                .id(spendingCategory.getId())
                .name(spendingCategory.getName())
                .isStupidCostTarget(spendingCategory.isStupidCostTarget())
                .build();
    }
}
