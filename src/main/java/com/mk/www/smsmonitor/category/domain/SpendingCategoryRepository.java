package com.mk.www.smsmonitor.category.domain;

import com.mk.www.smsmonitor.category.domain.SpendingCategory;

import java.util.List;
import java.util.Optional;

public interface SpendingCategoryRepository {
    Optional<SpendingCategory> findByName(String name);
    SpendingCategory save(SpendingCategory spendingCategory);
    List<SpendingCategory> findAll();
    Optional<SpendingCategory> findById(Long id);
    void deleteById(Long id);
}
