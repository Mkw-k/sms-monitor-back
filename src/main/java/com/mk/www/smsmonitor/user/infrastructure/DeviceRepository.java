package com.mk.www.smsmonitor.user.infrastructure;

import com.mk.www.smsmonitor.user.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByToken(String token);
    List<Device> findByLoginId(String loginId);
}
