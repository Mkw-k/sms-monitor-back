package com.mk.www.smsmonitor.category.application;

import com.mk.www.smsmonitor.category.domain.SpendingCategory;
import com.mk.www.smsmonitor.category.domain.SpendingCategoryRepository;
import com.mk.www.smsmonitor.category.api.dto.SpendingCategoryRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpendingCategoryServiceTest {

    @InjectMocks
    private SpendingCategoryService spendingCategoryService;

    @Mock
    private SpendingCategoryRepository spendingCategoryRepository;

    @Test
    @DisplayName("새로운_카테고리를_성공적으로_생성한다")
    void 새로운_카테고리를_성공적으로_생성한다() {
        // given
        SpendingCategoryRequest request = new SpendingCategoryRequest();
        request.setName("식비");
        request.setStupidCostTarget(true);

        when(spendingCategoryRepository.save(any(SpendingCategory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        SpendingCategory createdCategory = spendingCategoryService.createSpendingCategory(request);

        // then
        assertThat(createdCategory.getName()).isEqualTo("식비");
        assertThat(createdCategory.isStupidCostTarget()).isTrue();
        verify(spendingCategoryRepository).save(any(SpendingCategory.class));
    }

    @Test
    @DisplayName("모든_카테고리를_성공적으로_조회한다")
    void 모든_카테고리를_성공적으로_조회한다() {
        // given
        List<SpendingCategory> categories = List.of(SpendingCategory.builder().name("식비").build());
        when(spendingCategoryRepository.findAll()).thenReturn(categories);

        // when
        List<SpendingCategory> result = spendingCategoryService.getAllSpendingCategories();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("식비");
    }

    @Test
    @DisplayName("카테고리_수정_시_존재하지_않으면_빈_Optional을_반환한다")
    void 카테고리_수정_시_존재하지_않으면_빈_Optional을_반환한다() {
        // given
        SpendingCategoryRequest request = new SpendingCategoryRequest();
        when(spendingCategoryRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        Optional<SpendingCategory> result = spendingCategoryService.updateSpendingCategory(1L, request);

        // then
        assertThat(result).isNotPresent();
    }

    @Test
    @DisplayName("카테고리_삭제를_성공한다")
    void 카테고리_삭제를_성공한다() {
        // given
        when(spendingCategoryRepository.findById(1L)).thenReturn(Optional.of(SpendingCategory.builder().id(1L).build()));

        // when
        boolean deleted = spendingCategoryService.deleteSpendingCategory(1L);

        // then
        assertThat(deleted).isTrue();
        verify(spendingCategoryRepository).deleteById(1L);
    }
}
