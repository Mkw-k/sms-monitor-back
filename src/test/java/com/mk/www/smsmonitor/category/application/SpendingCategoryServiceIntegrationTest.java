package com.mk.www.smsmonitor.category.application;

import com.mk.www.smsmonitor.category.api.dto.SpendingCategoryRequest;
import com.mk.www.smsmonitor.category.domain.SpendingCategory;
import com.mk.www.smsmonitor.category.domain.SpendingCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SpendingCategoryServiceIntegrationTest {

    @Autowired
    private SpendingCategoryService spendingCategoryService;

    @Autowired
    private com.mk.www.smsmonitor.category.infrastructure.persistence.SpendingCategoryJpaRepository spendingCategoryJpaRepository;

    @BeforeEach
    void setUp() {
        spendingCategoryJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("지출 카테고리를 등록하고 조회할 수 있다")
    void createAndFindCategory_Success() {
        // given
        SpendingCategoryRequest request = new SpendingCategoryRequest();
        request.setName("카페/간식");
        request.setStupidCostTarget(true);

        // when
        SpendingCategory created = spendingCategoryService.createSpendingCategory(request);

        // then
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("카페/간식");
        assertThat(created.isStupidCostTarget()).isTrue();

        Optional<SpendingCategory> found = spendingCategoryService.getSpendingCategoryById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("카페/간식");

        List<SpendingCategory> all = spendingCategoryService.getAllSpendingCategories();
        assertThat(all).hasSize(1);
    }

    @Test
    @DisplayName("지출 카테고리 정보를 수정할 수 있다")
    void updateCategory_Success() {
        // given
        SpendingCategoryRequest createReq = new SpendingCategoryRequest();
        createReq.setName("쇼핑");
        createReq.setStupidCostTarget(false);
        SpendingCategory created = spendingCategoryService.createSpendingCategory(createReq);

        // when
        SpendingCategoryRequest updateReq = new SpendingCategoryRequest();
        updateReq.setName("온라인 쇼핑");
        updateReq.setStupidCostTarget(true);
        Optional<SpendingCategory> updated = spendingCategoryService.updateSpendingCategory(created.getId(), updateReq);

        // then
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("온라인 쇼핑");
        assertThat(updated.get().isStupidCostTarget()).isTrue();
    }

    @Test
    @DisplayName("지출 카테고리를 삭제할 수 있다")
    void deleteCategory_Success() {
        // given
        SpendingCategoryRequest createReq = new SpendingCategoryRequest();
        createReq.setName("문화생활");
        createReq.setStupidCostTarget(false);
        SpendingCategory created = spendingCategoryService.createSpendingCategory(createReq);

        // when
        boolean deleted = spendingCategoryService.deleteSpendingCategory(created.getId());

        // then
        assertThat(deleted).isTrue();
        assertThat(spendingCategoryService.getSpendingCategoryById(created.getId())).isEmpty();
    }
}
