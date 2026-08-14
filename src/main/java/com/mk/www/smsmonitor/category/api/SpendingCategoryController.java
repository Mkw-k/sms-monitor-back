package com.mk.www.smsmonitor.category.api;

import com.mk.www.smsmonitor.category.application.SpendingCategoryService;
import com.mk.www.smsmonitor.category.domain.SpendingCategory;
import com.mk.www.smsmonitor.common.api.ApiResponse;
import com.mk.www.smsmonitor.category.api.dto.SpendingCategoryRequest;
import com.mk.www.smsmonitor.category.api.dto.SpendingCategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "SpendingCategory", description = "소비 카테고리 관리 API")
@RestController
@RequestMapping("/api/spending-categories")
@RequiredArgsConstructor
public class SpendingCategoryController {

    private final SpendingCategoryService spendingCategoryService;

    @Operation(summary = "소비 카테고리 생성", description = "새로운 카테고리 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<SpendingCategoryResponse>> createSpendingCategory(@RequestBody SpendingCategoryRequest request) {
        SpendingCategory spendingCategory = spendingCategoryService.createSpendingCategory(request);
        SpendingCategoryResponse response = SpendingCategoryResponse.from(spendingCategory);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "모든 소비 카테고리 조회", description = "모든 카테고리 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SpendingCategoryResponse>>> getAllSpendingCategories() {
        List<SpendingCategoryResponse> responses = spendingCategoryService.getAllSpendingCategories().stream()
                .map(SpendingCategoryResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "소비 카테고리 수정", description = "특정 카테고리의 정보를 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SpendingCategoryResponse>> updateSpendingCategory(@PathVariable Long id, @RequestBody SpendingCategoryRequest request) {
        return spendingCategoryService.updateSpendingCategory(id, request)
                .map(SpendingCategoryResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "소비 카테고리 삭제", description = "특정 카테고리를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSpendingCategory(@PathVariable Long id) {
        boolean deleted = spendingCategoryService.deleteSpendingCategory(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success(null));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
