package com.mk.www.smsmonitor.common.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mk.www.smsmonitor.common.config.JwtAuthorizationFilter;
import com.mk.www.smsmonitor.common.config.SecurityConfig;
import com.mk.www.smsmonitor.common.util.JwtTokenProvider;
import com.mk.www.smsmonitor.user.domain.Device;
import com.mk.www.smsmonitor.user.infrastructure.DeviceRepository;
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

import java.util.List;
import java.util.Optional;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
@Import({SecurityConfig.class, JwtAuthorizationFilter.class})
@AutoConfigureRestDocs
@ActiveProfiles("test")
@WithMockUser(username = "user")
class DeviceControllerRestDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeviceRepository deviceRepository;

    @MockBean
    private UserJpaRepository userJpaRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.mk.www.smsmonitor.common.application.FcmService fcmService;

    @MockBean
    private AuthenticationConfiguration authenticationConfiguration;

    @MockBean
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() throws Exception {
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);
    }

    @Test
    @DisplayName("POST /api/devices/register - 기기 등록 API 문서화")
    void register_문서화() throws Exception {
        // given
        Device device = new Device();
        device.setToken("fcm-token-example");
        device.setLoginId("user");
        device.setPlatform("android");

        when(deviceRepository.findByToken(any())).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(post("/api/devices/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(device)))
                .andExpect(status().isOk())
                .andDo(document("device-register",
                        requestFields(
                                fieldWithPath("id").description("ID (생성시 무시)").ignored(),
                                fieldWithPath("token").description("FCM 기기 토큰"),
                                fieldWithPath("loginId").description("사용자 아이디"),
                                fieldWithPath("platform").description("기기 플랫폼").optional()
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Device")
                                .summary("기기 등록")
                                .description("푸시 알림 수신을 위한 FCM 토큰을 등록합니다.")
                                .requestFields(
                                        fieldWithPath("id").description("ID (생성시 무시)").ignored(),
                                        fieldWithPath("token").description("FCM 기기 토큰"),
                                        fieldWithPath("loginId").description("사용자 아이디"),
                                        fieldWithPath("platform").description("기기 플랫폼").optional()
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("GET /api/devices - 모든 기기 목록 조회 API 문서화")
    void getDevices_문서화() throws Exception {
        // given
        Device device = new Device();
        device.setToken("fcm-token-example");
        device.setLoginId("user");
        device.setPlatform("android");
        
        when(deviceRepository.findAll()).thenReturn(List.of(device));

        // when & then
        mockMvc.perform(get("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("device-list",
                        responseFields(
                                fieldWithPath("[].id").description("ID").optional(),
                                fieldWithPath("[].token").description("FCM 기기 토큰"),
                                fieldWithPath("[].loginId").description("사용자 아이디"),
                                fieldWithPath("[].platform").description("기기 플랫폼").optional()
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Device")
                                .summary("모든 기기 목록 조회")
                                .description("등록된 모든 기기 정보를 조회합니다.")
                                .responseFields(
                                        fieldWithPath("[].id").description("ID").optional(),
                                        fieldWithPath("[].token").description("FCM 기기 토큰"),
                                        fieldWithPath("[].loginId").description("사용자 아이디"),
                                        fieldWithPath("[].platform").description("기기 플랫폼").optional()
                                )
                                .build()
                        )
                ));
    }
}
