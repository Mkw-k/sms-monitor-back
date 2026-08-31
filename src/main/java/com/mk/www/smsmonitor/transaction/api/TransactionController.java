package com.mk.www.smsmonitor.transaction.api;

import com.mk.www.smsmonitor.common.util.CurrentUser;
import com.mk.www.smsmonitor.transaction.api.dto.*;
import com.mk.www.smsmonitor.transaction.application.SmsService;
import com.mk.www.smsmonitor.transaction.application.TransactionService;
import com.mk.www.smsmonitor.common.application.FcmService;
import com.mk.www.smsmonitor.common.api.ApiResponse;
import com.mk.www.smsmonitor.transaction.domain.Transaction;
import com.mk.www.smsmonitor.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Transaction", description = "거래내역 관리 API")
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final SmsService smsService;
    private final TransactionService transactionService;
    private final FcmService fcmService;

    @Operation(summary = "푸시 알림 테스트", description = "입력한 토큰으로 테스트 푸시 알림을 전송")
    @PostMapping("/test-push")
    public ResponseEntity<ApiResponse<String>> testPush(
            @RequestParam String token,
            @RequestParam(defaultValue = "테스트 알림") String title,
            @RequestParam(defaultValue = "테스트 메시지입니다.") String body) {
        
        log.info("=== [MANUAL PUSH TEST START] Target Token: {} ===", token);
        try {
            String response = fcmService.sendManualPush(token, title, body);
            log.info("=== [MANUAL PUSH TEST SUCCESS] === Response: {}", response);
            return ResponseEntity.ok(ApiResponse.success("Successfully sent: " + response));
        } catch (com.google.firebase.messaging.FirebaseMessagingException e) {
            log.error("=== [MANUAL PUSH TEST FAILED - FirebaseMessagingException] ===", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("FCM_ERROR", e.getMessage()));
        } catch (Exception e) {
            log.error("=== [MANUAL PUSH TEST FAILED - Unknown Error] ===", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("UNKNOWN_ERROR", e.getMessage()));
        }
    }

    @Operation(summary = "SMS 수신", description = "수신된 SMS 메시지를 파싱하여 거래내역으로 저장")
    @PostMapping("/sms")
    public ResponseEntity<ApiResponse<Void>> receiveSms(@RequestBody SmsRequest request, @CurrentUser User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        boolean success = smsService.processNewSms(request, user.getLoginId());
        if (success) {
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<Void>created(null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.<Void>error("FAIL", "SMS 처리 실패"));
        }
    }

    @Operation(summary = "내역 수동 등록", description = "사용자가 직접 수입/지출 내역을 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @RequestBody TransactionCreateRequest request, @CurrentUser User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Transaction saved = transactionService.createTransaction(request, user.getLoginId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(TransactionResponse.from(saved)));
    }

    @Operation(summary = "내역 수정", description = "특정 내역의 정보를 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @PathVariable Long id, @RequestBody TransactionUpdateRequest request, @CurrentUser User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return transactionService.updateTransaction(id, request, user.getLoginId())
                .map(TransactionResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "거래내역 조회", description = "로그인된 사용자의 거래내역을 조회 (필터링 지원)")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getMyTransactions(
            TransactionSearchRequest searchRequest, Pageable pageable, @CurrentUser User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Page<TransactionResponse> responses = transactionService.getUserTransactions(user.getLoginId(), searchRequest, pageable)
                .map(TransactionResponse::from);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "통계 조회", description = "일간/주간/월간/연간 통계 조회")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<TransactionStatisticsResponse>> getStatistics(
            @RequestParam(defaultValue = "monthly") String period,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime baseDate,
            @CurrentUser User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(ApiResponse.success(transactionService.getStatistics(user.getLoginId(), period, baseDate)));
    }

    @Operation(summary = "지출 요약 조회", description = "로그인된 사용자의 이번 달 지출 요약")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<TransactionSummaryResponse>> getMySummary(@CurrentUser User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(ApiResponse.success(transactionService.getSummary(user.getLoginId())));
    }

    @Operation(summary = "저축 분석 조회", description = "목표 저축액에 따른 지출 분석 가이드 제공")
    @GetMapping("/analysis/savings")
    public ResponseEntity<ApiResponse<SavingsAnalysisResponse>> getSavingsAnalysis(
            @RequestParam(name = "target", defaultValue = "1000000") BigDecimal target, @CurrentUser User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(ApiResponse.success(transactionService.analyzeSavings(user.getLoginId(), target)));
    }

    @Operation(summary = "메모 수정", description = "특정 거래내역 메모 추가 또는 수정")
    @PutMapping("/{id}/memo")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateMemo(
            @PathVariable Long id, @RequestBody MemoRequest request, @CurrentUser User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return transactionService.updateMemo(id, request, user.getLoginId())
                .map(TransactionResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .orElse(ResponseEntity.notFound().build());
    }
}
