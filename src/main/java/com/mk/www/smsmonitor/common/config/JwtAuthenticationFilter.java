package com.mk.www.smsmonitor.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mk.www.smsmonitor.common.util.JwtTokenProvider;
import com.mk.www.smsmonitor.user.api.dto.LoginRequest;
import com.mk.www.smsmonitor.common.api.ResultDTO;
import com.mk.www.smsmonitor.user.api.dto.TokenResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        // 로그인 URL 매핑을 프로젝트 관례에 맞게 설정
        setFilterProcessesUrl("/api/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // 요청 바디에서 LoginRequest 추출
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
            log.info("[Login Attempt] ID: {}", loginRequest.getLoginId());

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.getLoginId(), loginRequest.getPassword());

            return authenticationManager.authenticate(authenticationToken);
        } catch (IOException e) {
            log.error("[Login Error] Failed to read request body", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        UserDetails user = (UserDetails) authResult.getPrincipal();
        String loginId = user.getUsername();
        log.info("[Login Success] ID: {}", loginId);
        
        // 권한 정보가 없을 경우 기본값 ROLE_USER 부여
        String role = user.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .orElse("ROLE_USER");

        // 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(loginId, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(loginId);

        // Refresh Token 쿠키 설정
        jwtTokenProvider.setRefreshTokenInCookie(response, refreshToken);

        // 공통 규격(ResultDTO)으로 응답 생성
        ResultDTO<TokenResponse> result = ResultDTO.success(new TokenResponse(accessToken), "login-success");

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        log.warn("[Login Fail] Reason: {}", failed.getMessage());
        ResultDTO<Void> result = ResultDTO.error("AUTH_001", "인증 실패: " + failed.getMessage(), "login-fail");
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
