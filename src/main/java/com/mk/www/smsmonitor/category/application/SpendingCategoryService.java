package com.mk.www.smsmonitor.category.application;

import com.mk.www.smsmonitor.category.domain.SpendingCategory;
import com.mk.www.smsmonitor.category.domain.SpendingCategoryRepository;
import com.mk.www.smsmonitor.category.api.dto.SpendingCategoryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpendingCategoryService {

    private final SpendingCategoryRepository spendingCategoryRepository;

    @Transactional
    public SpendingCategory createSpendingCategory(SpendingCategoryRequest request) {
        SpendingCategory spendingCategory = SpendingCategory.builder()
                .name(request.getName())
                .isStupidCostTarget(request.isStupidCostTarget())
                .build();
        return spendingCategoryRepository.save(spendingCategory);
    }

    public List<SpendingCategory> getAllSpendingCategories() {
        return spendingCategoryRepository.findAll();
    }

    public Optional<SpendingCategory> getSpendingCategoryById(Long id) {
        return spendingCategoryRepository.findById(id);
    }

    @Transactional
    public Optional<SpendingCategory> updateSpendingCategory(Long id, SpendingCategoryRequest request) {
        return spendingCategoryRepository.findById(id)
                .map(category -> {
                    category.update(request.getName(), request.isStupidCostTarget());
                    return spendingCategoryRepository.save(category);
                });
    }

    @Transactional
    public boolean deleteSpendingCategory(Long id) {
        if (spendingCategoryRepository.findById(id).isPresent()) {
            spendingCategoryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

