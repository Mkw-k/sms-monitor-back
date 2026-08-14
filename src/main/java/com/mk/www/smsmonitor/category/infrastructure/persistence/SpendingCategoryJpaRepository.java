package com.mk.www.smsmonitor.category.infrastructure.persistence;

import com.mk.www.smsmonitor.category.infrastructure.persistence.SpendingCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpendingCategoryJpaRepository extends JpaRepository<SpendingCategoryEntity, Long> {
    Optional<SpendingCategoryEntity> findByName(String name);
}
