package com.mk.www.smsmonitor.user.application;

import com.mk.www.smsmonitor.common.api.ResultDTO;
import com.mk.www.smsmonitor.user.api.dto.LoginRequest;
import com.mk.www.smsmonitor.user.api.dto.RegisterRequest;
import com.mk.www.smsmonitor.user.api.dto.TokenResponse;
import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import com.mk.www.smsmonitor.user.infrastructure.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("신규 회원가입 성공 시 DB에 비밀번호가 암호화되어 저장되고 포인트가 지급된다")
    void register_Success() {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setLoginId("newuser");
        request.setPassword("plainPassword123!");

        // when
        ResultDTO<Void> result = authService.register(request);

        // then
        assertThat(result.getCode()).isEqualTo("SUCCESS");

        UserEntity user = userJpaRepository.findByLoginId("newuser").orElseThrow();
        assertThat(passwordEncoder.matches("plainPassword123!", user.getPassword())).isTrue();
        assertThat(user.getRole()).isEqualTo("ROLE_USER");
        assertThat(user.isApproved()).isTrue();
        assertThat(user.getPoint()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("중복된 아이디로 회원가입 시 에러를 반환한다")
    void register_DuplicateLoginId() {
        // given
        RegisterRequest request1 = new RegisterRequest();
        request1.setLoginId("dupuser");
        request1.setPassword("pass1");
        authService.register(request1);

        // when
        RegisterRequest request2 = new RegisterRequest();
        request2.setLoginId("dupuser");
        request2.setPassword("pass2");
        ResultDTO<Void> result = authService.register(request2);

        // then
        assertThat(result.getCode()).isEqualTo("AUTH_ERROR_001");
        assertThat(result.getMessage()).isEqualTo("이미 존재하는 아이디입니다.");
    }

    @Test
    @DisplayName("정상 로그인 시 엑세스 토큰이 발급되고 쿠키에 리프레시 토큰이 설정된다")
    void login_Success() {
        // given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setLoginId("loginuser");
        registerRequest.setPassword("securePassword");
        authService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLoginId("loginuser");
        loginRequest.setPassword("securePassword");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        ResultDTO<TokenResponse> result = authService.login(loginRequest, response);

        // then
        assertThat(result.getCode()).isEqualTo("SUCCESS");
        assertThat(result.getData().getAccessToken()).isNotBlank();
        assertThat(response.getCookie("refreshToken")).isNotNull();
    }

    @Test
    @DisplayName("비밀번호가 불일치하면 로그인에 실패한다")
    void login_WrongPassword() {
        // given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setLoginId("loginuser");
        registerRequest.setPassword("correctPassword");
        authService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLoginId("loginuser");
        loginRequest.setPassword("wrongPassword");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        ResultDTO<TokenResponse> result = authService.login(loginRequest, response);

        // then
        assertThat(result.getCode()).isEqualTo("AUTH_ERROR_002");
        assertThat(result.getMessage()).isEqualTo("아이디 또는 비밀번호가 일치하지 않습니다.");
    }
}
