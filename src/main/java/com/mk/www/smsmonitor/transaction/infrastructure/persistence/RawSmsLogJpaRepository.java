package com.mk.www.smsmonitor.transaction.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface RawSmsLogJpaRepository extends JpaRepository<RawSmsLogEntity, Long> {
    boolean existsByLoginIdAndSenderAndMessageAndReceivedAtAfter(String loginId, String sender, String message, LocalDateTime receivedAt);
}
