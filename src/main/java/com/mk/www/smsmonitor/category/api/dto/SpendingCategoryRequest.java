package com.mk.www.smsmonitor.category.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "소비 카테고리 생성/수정 요청 DTO")
public class SpendingCategoryRequest {
    @Schema(description = "카테고리 이름", example = "식비")
    private String name;
    @Schema(description = "멍청비용 분석 대상 여부", example = "true")
    private boolean isStupidCostTarget;
}
