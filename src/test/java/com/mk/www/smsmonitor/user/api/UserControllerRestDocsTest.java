package com.mk.www.smsmonitor.user.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mk.www.smsmonitor.common.config.JwtAuthorizationFilter;
import com.mk.www.smsmonitor.common.config.SecurityConfig;
import com.mk.www.smsmonitor.common.util.JwtTokenProvider;
import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import com.mk.www.smsmonitor.user.infrastructure.UserJpaRepository;
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
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthorizationFilter.class})
@AutoConfigureRestDocs
@ActiveProfiles("test")
@WithMockUser(username = "user")
class UserControllerRestDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserJpaRepository userJpaRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AuthenticationConfiguration authenticationConfiguration;

    @MockBean
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() throws Exception {
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);
    }

    @Test
    @DisplayName("GET /api/users/me - 내 정보 조회 API 문서화")
    void getMyProfile_문서화() throws Exception {
        // given
        UserEntity user = UserEntity.builder()
                .id(1L)
                .loginId("user")
                .role("ROLE_USER")
                .isApproved(true)
                .createdAt(LocalDateTime.now())
                .point(500L)
                .build();
        
        when(userJpaRepository.findByLoginId(anyString())).thenReturn(Optional.of(user));

        // when & then
        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("user-me",
                        responseFields(
                                fieldWithPath("tid").description("트랜잭션 ID"),
                                fieldWithPath("code").description("결과 코드"),
                                fieldWithPath("message").description("결과 메시지"),
                                fieldWithPath("data.loginId").description("사용자 아이디"),
                                fieldWithPath("data.role").description("사용자 권한"),
                                fieldWithPath("data.approved").description("승인 여부"),
                                fieldWithPath("data.point").description("보유 포인트"),
                                fieldWithPath("data.createdAt").description("가입 일시")
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("내 정보 조회")
                                .description("로그인된 사용자의 프로필 정보를 조회합니다.")
                                .responseFields(
                                        fieldWithPath("tid").description("트랜잭션 ID"),
                                        fieldWithPath("code").description("결과 코드"),
                                        fieldWithPath("message").description("결과 메시지"),
                                        fieldWithPath("data.loginId").description("사용자 아이디"),
                                        fieldWithPath("data.role").description("사용자 권한"),
                                        fieldWithPath("data.approved").description("승인 여부"),
                                        fieldWithPath("data.point").description("보유 포인트"),
                                        fieldWithPath("data.createdAt").description("가입 일시")
                                )
                                .build()
                        )
                ));
    }
}
