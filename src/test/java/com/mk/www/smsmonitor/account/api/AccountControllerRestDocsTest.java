package com.mk.www.smsmonitor.account.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mk.www.smsmonitor.account.api.dto.AccountResponse;
import com.mk.www.smsmonitor.account.infrastructure.AccountEntity;
import com.mk.www.smsmonitor.account.infrastructure.AccountJpaRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import({SecurityConfig.class, JwtAuthorizationFilter.class})
@AutoConfigureRestDocs
@ActiveProfiles("test")
@WithMockUser(username = "user")
class AccountControllerRestDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountJpaRepository accountJpaRepository;

    @MockBean
    private UserJpaRepository userJpaRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AuthenticationConfiguration authenticationConfiguration;

    @MockBean
    private AuthenticationManager authenticationManager;

    private com.mk.www.smsmonitor.user.application.CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() throws Exception {
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);
        
        com.mk.www.smsmonitor.user.domain.User user = com.mk.www.smsmonitor.user.domain.User.builder()
                .loginId("user")
                .role("ROLE_USER")
                .isApproved(true)
                .build();
        customUserDetails = new com.mk.www.smsmonitor.user.application.CustomUserDetails(user);
        
        UserEntity userEntity = UserEntity.builder().id(1L).loginId("user").build();
        when(userJpaRepository.findByLoginId("user")).thenReturn(Optional.of(userEntity));
    }

    @Test
    @DisplayName("GET /api/accounts - 내 계좌 목록 조회 API 문서화")
    void getMyAccounts_문서화() throws Exception {
        // given
        UserEntity userEntity = UserEntity.builder().id(1L).loginId("user").build();
        AccountEntity account = AccountEntity.builder()
                .id(1L)
                .user(userEntity)
                .accountNumber("123-456-789")
                .bankName("KB국민은행")
                .balance(new BigDecimal("1000000"))
                .build();
        
        when(accountJpaRepository.findAllByUser(any())).thenReturn(List.of(account));

        // when & then
        mockMvc.perform(get("/api/accounts")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(customUserDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("account-list",
                        responseFields(
                                fieldWithPath("tid").description("트랜잭션 ID"),
                                fieldWithPath("code").description("결과 코드"),
                                fieldWithPath("message").description("결과 메시지"),
                                fieldWithPath("data[].id").description("계좌 ID"),
                                fieldWithPath("data[].accountNumber").description("계좌 번호"),
                                fieldWithPath("data[].bankName").description("은행명"),
                                fieldWithPath("data[].balance").description("잔액"),
                                fieldWithPath("data[].default").description("기본 계좌 여부")
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Account")
                                .summary("내 계좌 목록 조회")
                                .description("로그인된 사용자의 모든 계좌 정보를 조회합니다.")
                                .responseFields(
                                        fieldWithPath("tid").description("트랜잭션 ID"),
                                        fieldWithPath("code").description("결과 코드"),
                                        fieldWithPath("message").description("결과 메시지"),
                                        fieldWithPath("data[].id").description("계좌 ID"),
                                        fieldWithPath("data[].accountNumber").description("계좌 번호"),
                                        fieldWithPath("data[].bankName").description("은행명"),
                                        fieldWithPath("data[].balance").description("잔액"),
                                        fieldWithPath("data[].default").description("기본 계좌 여부")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("GET /api/accounts/summary - 자산 요약 조회 API 문서화")
    void getAccountSummary_문서화() throws Exception {
        // given
        UserEntity userEntity = UserEntity.builder().id(1L).loginId("user").build();
        AccountEntity account = AccountEntity.builder()
                .id(1L)
                .user(userEntity)
                .balance(new BigDecimal("1000000"))
                .build();
        
        when(accountJpaRepository.findAllByUser(any())).thenReturn(List.of(account));

        // when & then
        mockMvc.perform(get("/api/accounts/summary")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(customUserDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("account-summary",
                        responseFields(
                                fieldWithPath("tid").description("트랜잭션 ID"),
                                fieldWithPath("code").description("결과 코드"),
                                fieldWithPath("message").description("결과 메시지"),
                                fieldWithPath("data").description("총 자산 합계")
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Account")
                                .summary("자산 요약 조회")
                                .description("모든 계좌의 잔액 합계를 조회합니다.")
                                .responseFields(
                                        fieldWithPath("tid").description("트랜잭션 ID"),
                                        fieldWithPath("code").description("결과 코드"),
                                        fieldWithPath("message").description("결과 메시지"),
                                        fieldWithPath("data").description("총 자산 합계")
                                )
                                .build()
                        )
                ));
    }
}
