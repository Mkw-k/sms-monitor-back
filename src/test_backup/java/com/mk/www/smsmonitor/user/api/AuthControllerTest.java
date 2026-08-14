package com.mk.www.smsmonitor.user.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mk.www.smsmonitor.common.config.JwtAuthorizationFilter;
import com.mk.www.smsmonitor.common.config.SecurityConfig;
import com.mk.www.smsmonitor.user.application.AuthService;
import com.mk.www.smsmonitor.common.util.JwtTokenProvider;
import com.mk.www.smsmonitor.user.api.dto.LoginRequest;
import com.mk.www.smsmonitor.user.api.dto.RegisterRequest;
import com.mk.www.smsmonitor.common.api.ResultDTO;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthorizationFilter.class})
@AutoConfigureRestDocs
@ActiveProfiles("test")
@WithMockUser
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private AuthenticationConfiguration authenticationConfiguration;

    @BeforeEach
    void setUp() throws Exception {
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);
    }

    @Test
    @DisplayName("로그인_API_문서화_테스트")
    void 로그인_API_문서화_테스트() throws Exception {
        // given
        LoginRequest request = new LoginRequest();
        request.setLoginId("testuser");
        request.setPassword("testpassword");

        // Spring Security 필터 체인을 시뮬레이션하기 위한 인증 결과 모킹
        User user = new User("testuser", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsernamePasswordAuthenticationToken authResult = 
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authResult);
        when(jwtTokenProvider.createAccessToken(any(), any())).thenReturn("access-token-example");
        when(jwtTokenProvider.createRefreshToken(any())).thenReturn("refresh-token-example");

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token-example"))
                .andDo(print())
                .andDo(document("auth-login",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth")
                                .summary("로그인")
                                .description("아이디와 비밀번호를 이용하여 로그인하고 JWT Access Token을 발급받습니다. (Spring Security 필터 처리)")
                                .requestFields(
                                        fieldWithPath("loginId").description("사용자 아이디"),
                                        fieldWithPath("password").description("사용자 비밀번호")
                                )
                                .responseFields(
                                        fieldWithPath("tid").description("트랜잭션 ID"),
                                        fieldWithPath("code").description("결과 코드 (SUCCESS)"),
                                        fieldWithPath("message").description("결과 메시지"),
                                        fieldWithPath("data.accessToken").description("발급된 JWT Access Token")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("회원가입_API_테스트")
    void 회원가입_API_테스트() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setLoginId("testuser");
        request.setPassword("testpassword");

        when(authService.register(any())).thenReturn(ResultDTO.success(null, "test-tid"));

        // when & then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andDo(document("auth-register",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth")
                                .summary("회원 가입")
                                .description("새로운 사용자를 등록합니다. 가입 후 관리자 승인이 필요합니다.")
                                .requestFields(
                                        fieldWithPath("loginId").description("사용자 아이디"),
                                        fieldWithPath("password").description("사용자 비밀번호")
                                )
                                .responseFields(
                                        fieldWithPath("tid").description("트랜잭션 ID"),
                                        fieldWithPath("code").description("결과 코드 (SUCCESS)"),
                                        fieldWithPath("message").description("결과 메시지"),
                                        fieldWithPath("data").description("응답 데이터 (null)").optional()
                                )
                                .build()
                        )
                ));
    }
}
