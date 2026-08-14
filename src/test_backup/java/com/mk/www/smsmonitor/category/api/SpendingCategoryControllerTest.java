package com.mk.www.smsmonitor.category.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mk.www.smsmonitor.category.application.SpendingCategoryService;
import com.mk.www.smsmonitor.common.config.JwtAuthorizationFilter;
import com.mk.www.smsmonitor.common.config.SecurityConfig;
import com.mk.www.smsmonitor.common.util.JwtTokenProvider;
import com.mk.www.smsmonitor.category.domain.SpendingCategory;
import com.mk.www.smsmonitor.category.api.dto.SpendingCategoryRequest;
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

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpendingCategoryController.class)
@AutoConfigureRestDocs
@Import({SecurityConfig.class, JwtAuthorizationFilter.class})
@ActiveProfiles("test")
@WithMockUser
class SpendingCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SpendingCategoryService spendingCategoryService;

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
    @DisplayName("POST /api/spending-categories - 카테고리 생성 요청을 성공한다")
    void POST_api_spending_categories_카테고리_생성_요청을_성공한다() throws Exception {
        // given
        SpendingCategoryRequest request = new SpendingCategoryRequest();
        request.setName("식비");
        request.setStupidCostTarget(true);

        SpendingCategory createdCategory = SpendingCategory.builder()
                .id(1L)
                .name("식비")
                .isStupidCostTarget(true)
                .build();

        when(spendingCategoryService.createSpendingCategory(any(SpendingCategoryRequest.class))).thenReturn(createdCategory);

        // when & then
        mockMvc.perform(post("/api/spending-categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("식비"))
                .andDo(document("spending-category-create",
                        resource(ResourceSnippetParameters.builder()
                                .summary("카테고리 생성")
                                .description("새로운 지출 카테고리를 생성합니다.")
                                .requestFields(
                                        fieldWithPath("name").description("카테고리 이름"),
                                        fieldWithPath("stupidCostTarget").description("멍청비용 대상 여부")
                                )
                                .responseFields(
                                        fieldWithPath("tid").description("트랜잭션 ID"),
                                        fieldWithPath("code").description("결과 코드"),
                                        fieldWithPath("message").description("결과 메시지"),
                                        fieldWithPath("data.id").description("생성된 카테고리 ID"),
                                        fieldWithPath("data.name").description("카테고리 이름"),
                                        fieldWithPath("data.stupidCostTarget").description("멍청비용 대상 여부")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("GET /api/spending-categories - 모든 카테고리 조회 요청을 성공한다")
    void GET_api_spending_categories_모든_카테고리_조회_요청을_성공한다() throws Exception {
        // given
        List<SpendingCategory> categories = List.of(SpendingCategory.builder().id(1L).name("식비").isStupidCostTarget(true).build());
        when(spendingCategoryService.getAllSpendingCategories()).thenReturn(categories);

        // when & then
        mockMvc.perform(get("/api/spending-categories")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("식비"))
                .andDo(document("spending-category-list",
                        resource(ResourceSnippetParameters.builder()
                                .summary("카테고리 목록 조회")
                                .description("등록된 모든 지출 카테고리를 조회합니다.")
                                .responseFields(
                                        fieldWithPath("tid").description("트랜잭션 ID"),
                                        fieldWithPath("code").description("결과 코드"),
                                        fieldWithPath("message").description("결과 메시지"),
                                        fieldWithPath("data[].id").description("카테고리 ID"),
                                        fieldWithPath("data[].name").description("카테고리 이름"),
                                        fieldWithPath("data[].stupidCostTarget").description("멍청비용 대상 여부")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("DELETE /api/spending-categories/{id} - 카테고리 삭제 요청을 성공한다")
    void DELETE_api_spending_categories_id_카테고리_삭제_요청을_성공한다() throws Exception {
        // given
        when(spendingCategoryService.deleteSpendingCategory(1L)).thenReturn(true);

        // when & then
        mockMvc.perform(delete("/api/spending-categories/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(document("spending-category-delete",
                        resource(ResourceSnippetParameters.builder()
                                .summary("카테고리 삭제")
                                .description("특정 지출 카테고리를 삭제합니다.")
                                .pathParameters(
                                        parameterWithName("id").description("삭제할 카테고리 ID")
                                )
                                .responseFields(
                                        fieldWithPath("tid").description("트랜잭션 ID"),
                                        fieldWithPath("code").description("결과 코드"),
                                        fieldWithPath("message").description("결과 메시지"),
                                        fieldWithPath("data").description("응답 데이터 (null)").optional()
                                )
                                .build()
                        )
                ));
    }
}
