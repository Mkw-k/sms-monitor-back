package com.mk.www.smsmonitor.user.application;

import com.mk.www.smsmonitor.common.util.JwtTokenProvider;
import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import com.mk.www.smsmonitor.user.infrastructure.UserJpaRepository;
import com.mk.www.smsmonitor.user.api.dto.RegisterRequest;
import com.mk.www.smsmonitor.common.api.ResultDTO;
import com.mk.www.smsmonitor.user.api.dto.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.MDC;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserJpaRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        MDC.put("tid", "test-tid");
    }

    @Test
    @DisplayName("회원가입_성공_테스트")
    void 회원가입_성공_테스트() {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setLoginId("testuser");
        request.setPassword("password");

        when(userRepository.existsByLoginId("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        // when
        ResultDTO<Void> result = authService.register(request);

        // then
        assertThat(result.getCode()).isEqualTo("SUCCESS");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("회원가입_중복아이디_실패_테스트")
    void 회원가입_중복아이디_실패_테스트() {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setLoginId("testuser");
        request.setPassword("password");

        when(userRepository.existsByLoginId("testuser")).thenReturn(true);

        // when
        ResultDTO<Void> result = authService.register(request);

        // then
        assertThat(result.getCode()).isEqualTo("AUTH_ERROR_001");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("토큰_갱신_성공_테스트")
    void 토큰_갱신_성공_테스트() {
        // given
        String refreshToken = "valid-refresh-token";
        UserEntity user = UserEntity.builder()
                .loginId("testuser")
                .role("ROLE_USER")
                .isApproved(true)
                .build();

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getLoginId(refreshToken)).thenReturn("testuser");
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.createAccessToken(any(), any())).thenReturn("new-access-token");

        // when
        ResultDTO<TokenResponse> result = authService.refresh(refreshToken);

        // then
        assertThat(result.getCode()).isEqualTo("SUCCESS");
        assertThat(result.getData().getAccessToken()).isEqualTo("new-access-token");
    }
}
