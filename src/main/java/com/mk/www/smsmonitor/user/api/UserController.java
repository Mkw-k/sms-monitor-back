package com.mk.www.smsmonitor.user.api;

import com.mk.www.smsmonitor.common.api.ApiResponse;
import com.mk.www.smsmonitor.user.api.dto.UserProfileResponse;
import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import com.mk.www.smsmonitor.user.infrastructure.UserJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserJpaRepository userJpaRepository;

    @Operation(summary = "내 정보 조회", description = "로그인된 사용자의 프로필 정보를 조회 (마이페이지용)")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Principal principal) {
        UserEntity user = userJpaRepository.findByLoginId(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return ResponseEntity.ok(ApiResponse.success(UserProfileResponse.from(user)));
    }
}
