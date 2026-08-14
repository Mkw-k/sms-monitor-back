package com.mk.www.smsmonitor.account.infrastructure;

import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {
    List<AccountEntity> findAllByUser(UserEntity user);
    java.util.Optional<AccountEntity> findByUserAndIsDefault(UserEntity user, boolean isDefault);
}
