package com.mk.www.smsmonitor.transaction.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mk.www.smsmonitor.common.config.JwtAuthorizationFilter;
import com.mk.www.smsmonitor.common.config.SecurityConfig;
import com.mk.www.smsmonitor.transaction.application.SmsService;
import com.mk.www.smsmonitor.transaction.application.TransactionService;
import com.mk.www.smsmonitor.common.util.JwtTokenProvider;
import com.mk.www.smsmonitor.transaction.domain.Transaction;
import com.mk.www.smsmonitor.transaction.api.dto.*;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@Import({SecurityConfig.class, JwtAuthorizationFilter.class})
@AutoConfigureRestDocs
@ActiveProfiles("test")
@WithMockUser(username = "user")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SmsService smsService;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.mk.www.smsmonitor.user.infrastructure.UserJpaRepository userJpaRepository;

    @MockBean
    private com.mk.www.smsmonitor.user.infrastructure.DeviceRepository deviceRepository;

    @MockBean
    private com.mk.www.smsmonitor.common.application.FcmService fcmService;

    @MockBean
    private org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration authenticationConfiguration;

    @MockBean
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

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
    }

    @Test
    @DisplayName("@CurrentUser를_통해_로그인_사용자_정보가_정상적으로_주입되는지_확인한다")
    void CurrentUser_주입_확인() throws Exception {
        // given
        SmsRequest request = new SmsRequest();
        request.setSender("010-1234-5678");
        request.setMessage("KB국민카드 승인...");

        when(smsService.processNewSms(any(SmsRequest.class), eq("user"))).thenReturn(true);

        // when & then
        mockMvc.perform(post("/api/transactions/sms")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(customUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("성공적인_SMS_처리에_대해_200OK_응답을_반환한다")
    void 성공적인_SMS_처리에_대해_200OK_응답을_반환한다() throws Exception {
        // given
        SmsRequest request = new SmsRequest();
        request.setSender("010-1234-5678");
        request.setMessage("KB국민카드 승인...");

        when(smsService.processNewSms(any(SmsRequest.class), eq("user"))).thenReturn(true);

        // when & then
        mockMvc.perform(post("/api/transactions/sms")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(customUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andDo(document("transaction-sms",
                        requestFields(
                                fieldWithPath("sender").description("SMS 발신 번호"),
                                fieldWithPath("message").description("SMS 메시지 내용")
                        ),
                        responseFields(
                                fieldWithPath("tid").description("트랜잭션 ID"),
                                fieldWithPath("code").description("결과 코드"),
                                fieldWithPath("message").description("결과 메시지"),
                                fieldWithPath("data").description("응답 데이터 (null)").optional()
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Transaction")
                                .summary("SMS 수신 및 거래내역 저장")
                                .description("수신된 SMS 메시지를 파싱하여 거래내역으로 저장합니다.")
                                .requestFields(
                                        fieldWithPath("sender").description("SMS 발신 번호"),
                                        fieldWithPath("message").description("SMS 메시지 내용")
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

    @Test
    @DisplayName("GET_api_transactions_거래내역_페이지_조회_요청을_성공한다")
    void GET_api_transactions_거래내역_페이지_조회_요청을_성공한다() throws Exception {
        // given
        Transaction transaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("10000"))
                .vendor("store1")
                .transactionTime(LocalDateTime.now())
                .isStupidCost(false)
                .memo("memo")
                .type(Transaction.TransactionType.EXPENSE)
                .build();
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        when(transactionService.getUserTransactions(eq("user"), any(), any(Pageable.class))).thenReturn(page);

        // when & then
        mockMvc.perform(get("/api/transactions")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(customUserDetails))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].vendor").value("store1"))
                .andDo(document("transaction-list",
                        queryParameters(
                                parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
                                parameterWithName("size").description("페이지 크기").optional(),
                                parameterWithName("isStupid").description("멍청비용 필터 여부").optional()
                        ),
                        responseFields(
                                fieldWithPath("tid").description("트랜잭션 ID"),
                                fieldWithPath("code").description("결과 코드"),
                                fieldWithPath("message").description("결과 메시지"),
                                fieldWithPath("data.content[].id").description("거래 ID"),
                                fieldWithPath("data.content[].amount").description("금액"),
                                fieldWithPath("data.content[].vendor").description("가맹점"),
                                fieldWithPath("data.content[].transactionTime").description("거래 일시"),
                                fieldWithPath("data.content[].stupidCost").description("멍청비용 여부"),
                                fieldWithPath("data.content[].memo").description("메모").optional(),
                                fieldWithPath("data.content[].categoryName").description("카테고리명").optional(),
                                fieldWithPath("data.content[].type").description("거래 유형 (INCOME, EXPENSE)"),
                                fieldWithPath("data.content[].fixedExpense").description("고정 지출 여부"),
                                fieldWithPath("data.content[].manual").description("수동 입력 여부"),
                                fieldWithPath("data.content[].ignored").description("무시 여부"),
                                fieldWithPath("data.content[].name").description("거래명").optional(),
                                fieldWithPath("data.content[].expenseType").description("지출 유형 문자열"),
                                fieldWithPath("data.page.size").description("페이지 크기"),
                                fieldWithPath("data.page.number").description("현재 페이지 번호"),
                                fieldWithPath("data.page.totalElements").description("전체 요소 수"),
                                fieldWithPath("data.page.totalPages").description("전체 페이지 수")
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Transaction")
                                .summary("거래내역 목록 조회")
                                .description("로그인된 사용자의 거래내역을 페이지 단위로 조회합니다.")
                                .queryParameters(
                                        parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
                                        parameterWithName("size").description("페이지 크기").optional(),
                                        parameterWithName("isStupid").description("멍청비용 필터 여부").optional()
                                )
                                .responseFields(
                                        fieldWithPath("tid").description("트랜잭션 ID"),
                                        fieldWithPath("code").description("결과 코드"),
                                        fieldWithPath("message").description("결과 메시지"),
                                        fieldWithPath("data.content[].id").description("거래 ID"),
                                        fieldWithPath("data.content[].amount").description("금액"),
                                        fieldWithPath("data.content[].vendor").description("가맹점"),
                                        fieldWithPath("data.content[].transactionTime").description("거래 일시"),
                                        fieldWithPath("data.content[].stupidCost").description("멍청비용 여부"),
                                        fieldWithPath("data.content[].memo").description("메모").optional(),
                                        fieldWithPath("data.content[].categoryName").description("카테고리명").optional(),
                                        fieldWithPath("data.content[].type").description("거래 유형 (INCOME, EXPENSE)"),
                                        fieldWithPath("data.content[].fixedExpense").description("고정 지출 여부"),
                                        fieldWithPath("data.content[].manual").description("수동 입력 여부"),
                                        fieldWithPath("data.content[].ignored").description("무시 여부"),
                                        fieldWithPath("data.content[].name").description("거래명").optional(),
                                        fieldWithPath("data.content[].expenseType").description("지출 유형 문자열"),
                                        fieldWithPath("data.page.size").description("페이지 크기"),
                                        fieldWithPath("data.page.number").description("현재 페이지 번호"),
                                        fieldWithPath("data.page.totalElements").description("전체 요소 수"),
                                        fieldWithPath("data.page.totalPages").description("전체 페이지 수")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("GET /api/transactions/statistics - 통계 조회 API 문서화")
    void getStatistics_문서화() throws Exception {
        // given
        TransactionStatisticsResponse response = TransactionStatisticsResponse.builder()
                .entries(List.of(TransactionStatisticsResponse.StatEntry.builder()
                        .label("05-14")
                        .income(new BigDecimal("50000"))
                        .expense(new BigDecimal("30000"))
                        .build()))
                .build();
        
        when(transactionService.getStatistics(eq("user"), anyString(), any())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/transactions/statistics")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(customUserDetails))
                        .param("period", "daily"))
                .andExpect(status().isOk())
                .andDo(document("transaction-statistics",
                        queryParameters(
                                parameterWithName("period").description("통계 주기 (daily, weekly, monthly, yearly)").optional()
                        ),
                        responseFields(
                                fieldWithPath("tid").description("트랜잭션 ID"),
                                fieldWithPath("code").description("결과 코드"),
                                fieldWithPath("message").description("결과 메시지"),
                                fieldWithPath("data.entries[].label").description("날짜/기간 레이블"),
                                fieldWithPath("data.entries[].income").description("수입 합계"),
                                fieldWithPath("data.entries[].expense").description("지출 합계")
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Transaction")
                                .summary("거래 통계 조회")
                                .description("일간/주간/월간/연간 거래 통계를 조회합니다.")
                                .queryParameters(
                                        parameterWithName("period").description("통계 주기 (daily, weekly, monthly, yearly)").optional()
                                )
                                .responseFields(
                                        fieldWithPath("tid").description("트랜잭션 ID"),
                                        fieldWithPath("code").description("결과 코드"),
                                        fieldWithPath("message").description("결과 메시지"),
                                        fieldWithPath("data.entries[].label").description("날짜/기간 레이블"),
                                        fieldWithPath("data.entries[].income").description("수입 합계"),
                                        fieldWithPath("data.entries[].expense").description("지출 합계")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("GET /api/transactions/summary - 지출 요약 조회 API 문서화")
    void getMySummary_문서화() throws Exception {
        // given
        TransactionSummaryResponse response = new TransactionSummaryResponse(
                new BigDecimal("500000"), new BigDecimal("1000000"), new BigDecimal("50000"), "5월");
        
        when(transactionService.getSummary(eq("user"))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/transactions/summary")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(customUserDetails)))
                .andExpect(status().isOk())
                .andDo(document("transaction-summary",
                        responseFields(
                                fieldWithPath("tid").description("트랜잭션 ID"),
                                fieldWithPath("code").description("결과 코드"),
                                fieldWithPath("message").description("결과 메시지"),
                                fieldWithPath("data.monthlyTotalAmount").description("총 지출"),
                                fieldWithPath("data.monthlyTotalIncome").description("총 수입"),
                                fieldWithPath("data.monthlyStupidCostAmount").description("총 멍청비용"),
                                fieldWithPath("data.month").description("해당 월 레이블")
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Transaction")
                                .summary("지출 요약 조회")
                                .description("이번 달 총 지출, 수입, 멍청비용 요약을 조회합니다.")
                                .responseFields(
                                        fieldWithPath("tid").description("트랜잭션 ID"),
                                        fieldWithPath("code").description("결과 코드"),
                                        fieldWithPath("message").description("결과 메시지"),
                                        fieldWithPath("data.monthlyTotalAmount").description("총 지출"),
                                        fieldWithPath("data.monthlyTotalIncome").description("총 수입"),
                                        fieldWithPath("data.monthlyStupidCostAmount").description("총 멍청비용"),
                                        fieldWithPath("data.month").description("해당 월 레이블")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("GET /api/transactions/analysis/savings - 저축 분석 조회 API 문서화")
    void getSavingsAnalysis_문서화() throws Exception {
        // given
        SavingsAnalysisResponse response = SavingsAnalysisResponse.builder()
                .income(new BigDecimal("3000000"))
                .fixedExpense(new BigDecimal("1000000"))
                .variableExpense(new BigDecimal("500000"))
                .stupidExpense(new BigDecimal("50000"))
                .maxPossibleSavings(new BigDecimal("2000000"))
                .currentSavings(new BigDecimal("1500000"))
                .targetSavings(new BigDecimal("2000000"))
                .gap(new BigDecimal("500000"))
                .recommendation("지출을 500000원 더 줄여야 합니다.")
                .build();
        
        when(transactionService.analyzeSavings(eq("user"), any(BigDecimal.class))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/transactions/analysis/savings")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(customUserDetails))
                        .param("target", "2000000"))
                .andExpect(status().isOk())
                .andDo(document("transaction-savings-analysis",
                        queryParameters(
                                parameterWithName("target").description("목표 저축액").optional()
                        ),
                        responseFields(
                                fieldWithPath("tid").description("트랜잭션 ID"),
                                fieldWithPath("code").description("결과 코드"),
                                fieldWithPath("message").description("결과 메시지"),
                                fieldWithPath("data.income").description("총 수입"),
                                fieldWithPath("data.fixedExpense").description("고정 지출"),
                                fieldWithPath("data.variableExpense").description("변동 지출"),
                                fieldWithPath("data.stupidExpense").description("멍청 비용"),
                                fieldWithPath("data.maxPossibleSavings").description("최대 가능 저축액"),
                                fieldWithPath("data.currentSavings").description("현재 예상 저축액"),
                                fieldWithPath("data.targetSavings").description("목표 저축액"),
                                fieldWithPath("data.gap").description("목표와의 차이"),
                                fieldWithPath("data.recommendation").description("분석 추천 메시지")
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Transaction")
                                .summary("저축 분석 조회")
                                .description("목표 저축액에 따른 지출 분석 및 가이드를 제공합니다.")
                                .queryParameters(
                                        parameterWithName("target").description("목표 저축액").optional()
                                )
                                .responseFields(
                                        fieldWithPath("tid").description("트랜잭션 ID"),
                                        fieldWithPath("code").description("결과 코드"),
                                        fieldWithPath("message").description("결과 메시지"),
                                        fieldWithPath("data.income").description("총 수입"),
                                        fieldWithPath("data.fixedExpense").description("고정 지출"),
                                        fieldWithPath("data.variableExpense").description("변동 지출"),
                                        fieldWithPath("data.stupidExpense").description("멍청 비용"),
                                        fieldWithPath("data.maxPossibleSavings").description("최대 가능 저축액"),
                                        fieldWithPath("data.currentSavings").description("현재 예상 저축액"),
                                        fieldWithPath("data.targetSavings").description("목표 저축액"),
                                        fieldWithPath("data.gap").description("목표와의 차이"),
                                        fieldWithPath("data.recommendation").description("분석 추천 메시지")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("PUT_api_transactions_id_memo_메모_수정_요청을_성공한다")
    void PUT_api_transactions_id_memo_메모_수정_요청을_성공한다() throws Exception {
        // given
        MemoRequest request = new MemoRequest();
        request.setMemo("새로운 메모");
        Transaction updatedTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("10000"))
                .vendor("store1")
                .transactionTime(LocalDateTime.now())
                .isStupidCost(false)
                .memo("새로운 메모")
                .type(Transaction.TransactionType.EXPENSE)
                .build();

        when(transactionService.updateTransaction(any(Long.class), any(TransactionUpdateRequest.class), eq("user"))).thenReturn(Optional.of(updatedTransaction));

        // when & then
        mockMvc.perform(put("/api/transactions/{id}/memo", 1L)
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(customUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memo").value("새로운 메모"))
                .andDo(document("transaction-memo-update",
                        pathParameters(
                                parameterWithName("id").description("거래 ID")
                        ),
                        requestFields(
                                fieldWithPath("memo").description("수정할 메모 내용")
                        ),
                        responseFields(
                                fieldWithPath("tid").description("트랜잭션 ID"),
                                fieldWithPath("code").description("결과 코드"),
                                fieldWithPath("message").description("결과 메시지"),
                                fieldWithPath("data.id").description("거래 ID"),
                                fieldWithPath("data.amount").description("금액"),
                                fieldWithPath("data.vendor").description("가맹점"),
                                fieldWithPath("data.transactionTime").description("거래 일시"),
                                fieldWithPath("data.stupidCost").description("멍청비용 여부"),
                                fieldWithPath("data.memo").description("수정된 메모 내용"),
                                fieldWithPath("data.categoryName").description("카테고리명").optional(),
                                fieldWithPath("data.type").description("거래 유형"),
                                fieldWithPath("data.fixedExpense").description("고정 지출 여부"),
                                fieldWithPath("data.manual").description("수동 입력 여부"),
                                fieldWithPath("data.ignored").description("무시 여부"),
                                fieldWithPath("data.name").description("거래명").optional(),
                                fieldWithPath("data.expenseType").description("지출 유형 문자열")
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Transaction")
                                .summary("거래내역 메모 수정")
                                .description("특정 거래내역의 메모를 수정합니다.")
                                .pathParameters(
                                        parameterWithName("id").description("거래 ID")
                                )
                                .requestFields(
                                        fieldWithPath("memo").description("수정할 메모 내용")
                                )
                                .responseFields(
                                        fieldWithPath("tid").description("트랜잭션 ID"),
                                        fieldWithPath("code").description("결과 코드"),
                                        fieldWithPath("message").description("결과 메시지"),
                                        fieldWithPath("data.id").description("거래 ID"),
                                        fieldWithPath("data.amount").description("금액"),
                                        fieldWithPath("data.vendor").description("가맹점"),
                                        fieldWithPath("data.transactionTime").description("거래 일시"),
                                        fieldWithPath("data.stupidCost").description("멍청비용 여부"),
                                        fieldWithPath("data.memo").description("수정된 메모 내용"),
                                        fieldWithPath("data.categoryName").description("카테고리명").optional(),
                                        fieldWithPath("data.type").description("거래 유형"),
                                        fieldWithPath("data.fixedExpense").description("고정 지출 여부"),
                                        fieldWithPath("data.manual").description("수동 입력 여부"),
                                        fieldWithPath("data.ignored").description("무시 여부"),
                                        fieldWithPath("data.name").description("거래명").optional(),
                                        fieldWithPath("data.expenseType").description("지출 유형 문자열")
                                )
                                .build()
                        )
                ));
    }
}
