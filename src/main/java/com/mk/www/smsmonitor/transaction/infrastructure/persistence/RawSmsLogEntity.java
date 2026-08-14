package com.mk.www.smsmonitor.transaction.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "raw_sms_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawSmsLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sender;

    @Lob
    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String loginId;

    @Column(nullable = false)
    private LocalDateTime receivedAt;
}
