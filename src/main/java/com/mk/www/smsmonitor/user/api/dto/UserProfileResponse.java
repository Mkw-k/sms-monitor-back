package com.mk.www.smsmonitor.user.api.dto;

import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserProfileResponse {
    private String loginId;
    private LocalDateTime createdAt;
    private String role;
    private boolean isApproved;
    private Long point;

    public static UserProfileResponse from(UserEntity entity) {
        return UserProfileResponse.builder()
                .loginId(entity.getLoginId())
                .createdAt(entity.getCreatedAt())
                .role(entity.getRole())
                .isApproved(entity.isApproved())
                .point(entity.getPoint())
                .build();
    }
}
