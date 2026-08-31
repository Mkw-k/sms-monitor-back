package com.mk.www.smsmonitor.user.application;

import com.mk.www.smsmonitor.common.util.JwtTokenProvider;
import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import com.mk.www.smsmonitor.user.infrastructure.UserJpaRepository;
import com.mk.www.smsmonitor.user.api.dto.LoginRequest;
import com.mk.www.smsmonitor.user.api.dto.RegisterRequest;
import com.mk.www.smsmonitor.common.api.ResultDTO;
import com.mk.www.smsmonitor.user.api.dto.TokenResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private static final String TID_KEY = "tid";

    @Transactional
    public ResultDTO<Void> register(RegisterRequest request) {
        String tid = MDC.get(TID_KEY);

        if (userRepository.existsByLoginId(request.getLoginId())) {
            return ResultDTO.error("AUTH_ERROR_001", "이미 존재하는 아이디입니다.", tid);
        }

        UserEntity user = UserEntity.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .isApproved(true) // 즉시 승인으로 변경 (개발 편의성)
                .point(1000L) // 신규 가입 축하 포인트 1000P
                .build();

        userRepository.save(user);
        return ResultDTO.success(null, tid);
    }

    public ResultDTO<TokenResponse> login(LoginRequest request, HttpServletResponse response) {
        String tid = MDC.get(TID_KEY);

        UserEntity user = userRepository.findByLoginId(request.getLoginId())
                .orElse(null);

        if (user == null || !user.matchPassword(request.getPassword(), passwordEncoder)) {
            return ResultDTO.error("AUTH_ERROR_002", "아이디 또는 비밀번호가 일치하지 않습니다.", tid);
        }

        if (!user.isAccessible()) {
            return ResultDTO.error("AUTH_ERROR_003", "승인 대기 중인 사용자입니다.", tid);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getLoginId());

        jwtTokenProvider.setRefreshTokenInCookie(response, refreshToken);

        return ResultDTO.success(new TokenResponse(accessToken), tid);
    }

    public ResultDTO<TokenResponse> refresh(String refreshToken) {
        String tid = MDC.get(TID_KEY);

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResultDTO.error("AUTH_ERROR_004", "유효하지 않은 리프레시 토큰입니다.", tid);
        }

        String loginId = jwtTokenProvider.getLoginId(refreshToken);
        UserEntity user = userRepository.findByLoginId(loginId).orElse(null);

        if (user == null || !user.isAccessible()) {
            return ResultDTO.error("AUTH_ERROR_005", "유효하지 않은 사용자이거나 승인되지 않았습니다.", tid);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole());
        return ResultDTO.success(new TokenResponse(newAccessToken), tid);
    }
}
