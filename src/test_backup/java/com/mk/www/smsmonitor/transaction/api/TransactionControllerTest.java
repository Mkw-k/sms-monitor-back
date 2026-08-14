package com.mk.www.smsmonitor.transaction.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mk.www.smsmonitor.transaction.application.SmsService;
import com.mk.www.smsmonitor.transaction.application.TransactionService;
import com.mk.www.smsmonitor.common.util.JwtTokenProvider;
import com.mk.www.smsmonitor.transaction.domain.Transaction;
import com.mk.www.smsmonitor.transaction.api.dto.MemoRequest;
import com.mk.www.smsmonitor.transaction.api.dto.SmsRequest;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
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
    JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("@CurrentUser를_통해_로그인_사용자_정보가_정상적으로_주입되는지_확인한다")
    void CurrentUser_주입_확인() throws Exception {
        // given
        SmsRequest request = new SmsRequest();
        request.setSender("010-1234-5678");
        request.setMessage("KB국민카드 승인...");

        com.mk.www.smsmonitor.user.domain.User user = com.mk.www.smsmonitor.user.domain.User.builder()
                .loginId("user")
                .role("ROLE_USER")
                .isApproved(true)
                .build();
        com.mk.www.smsmonitor.user.application.CustomUserDetails customUserDetails = new com.mk.www.smsmonitor.user.application.CustomUserDetails(user);

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

        com.mk.www.smsmonitor.user.domain.User user = com.mk.www.smsmonitor.user.domain.User.builder()
                .loginId("user")
                .role("ROLE_USER")
                .isApproved(true)
                .build();
        com.mk.www.smsmonitor.user.application.CustomUserDetails customUserDetails = new com.mk.www.smsmonitor.user.application.CustomUserDetails(user);

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
                        resource(ResourceSnippetParameters.builder()
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
                .build();
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        when(transactionService.getUserTransactions(eq("user"), any(), any(Pageable.class))).thenReturn(page);

        // when & then
        mockMvc.perform(get("/api/transactions")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].vendor").value("store1"))
                .andDo(document("transaction-list",
                        resource(ResourceSnippetParameters.builder()
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
                                        fieldWithPath("data.pageable").description("페이지 정보"),
                                        fieldWithPath("data.last").description("마지막 페이지 여부"),
                                        fieldWithPath("data.totalPages").description("전체 페이지 수"),
                                        fieldWithPath("data.totalElements").description("전체 요소 수"),
                                        fieldWithPath("data.size").description("페이지 크기"),
                                        fieldWithPath("data.number").description("현재 페이지 번호"),
                                        fieldWithPath("data.sort.empty").description("정렬 정보 비어있음 여부"),
                                        fieldWithPath("data.sort.sorted").description("정렬됨 여부"),
                                        fieldWithPath("data.sort.unsorted").description("정렬되지 않음 여부"),
                                        fieldWithPath("data.first").description("첫 페이지 여부"),
                                        fieldWithPath("data.numberOfElements").description("현재 페이지 요소 수"),
                                        fieldWithPath("data.empty").description("비어 있음 여부")
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
                .build();

        when(transactionService.updateMemo(any(Long.class), any(MemoRequest.class), eq("user"))).thenReturn(Optional.of(updatedTransaction));

        // when & then
        mockMvc.perform(put("/api/transactions/{id}/memo", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memo").value("새로운 메모"))
                .andDo(document("transaction-memo-update",
                        resource(ResourceSnippetParameters.builder()
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
                                        fieldWithPath("data.categoryName").description("카테고리명").optional()
                                )
                                .build()
                        )
                ));
    }
}
