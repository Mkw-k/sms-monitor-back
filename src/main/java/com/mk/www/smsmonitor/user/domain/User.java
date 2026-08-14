package com.mk.www.smsmonitor.user.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class User {
    private Long id;
    private String loginId;
    private String password;
    private String role; // "ROLE_USER", "ROLE_ADMIN" 등
    private boolean isApproved; // 승인된 사용자만 로그인 가능
    private LocalDateTime createdAt;
}
