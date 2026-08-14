package com.mk.www.smsmonitor.category.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class SpendingCategory {
    private Long id;
    private Long userId; // 유저 식별자 추가
    private String name;
    private boolean isStupidCostTarget;

    //소비 카테고리 수정
    public void update(String name, boolean isStupidCostTarget) {
        this.name = name;
        this.isStupidCostTarget = isStupidCostTarget;
    }
}
