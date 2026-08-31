package com.mk.www.smsmonitor.account.api;

import com.mk.www.smsmonitor.account.api.dto.AccountRequest;
import com.mk.www.smsmonitor.account.api.dto.AccountResponse;
import com.mk.www.smsmonitor.account.application.AccountService;
import com.mk.www.smsmonitor.account.infrastructure.AccountEntity;
import com.mk.www.smsmonitor.account.infrastructure.AccountJpaRepository;
import com.mk.www.smsmonitor.common.api.ApiResponse;
import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import com.mk.www.smsmonitor.user.infrastructure.UserJpaRepository;
import com.mk.www.smsmonitor.common.util.CurrentUser;
import com.mk.www.smsmonitor.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Account", description = "계좌 관리 API")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "내 계좌 목록 조회", description = "로그인된 사용자의 모든 계좌 정보를 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts(@CurrentUser User user) {
        List<AccountResponse> myAccounts = accountService.getMyAccounts(user);
        return ResponseEntity.ok(ApiResponse.success(myAccounts));
    }

    @Operation(summary = "계좌 추가", description = "신규 계좌(자산)를 추가")
    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@CurrentUser User user, @RequestBody AccountRequest request) {
        AccountEntity savedAccount = accountService.createAccount(user, request);
        return ResponseEntity.ok(ApiResponse.success(AccountResponse.from(savedAccount)));
    }

    @Operation(summary = "메인 계좌 설정", description = "특정 계좌를 주 결제 계좌로 설정")
    @PutMapping("/{id}/default")
    public ResponseEntity<ApiResponse<Void>> setDefaultAccount(@CurrentUser User user, @PathVariable Long id) {
        AccountEntity accountEntity = accountService.setDefaultAccount(user, id);
        log.info(accountEntity.toString());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "계좌 삭제", description = "계좌 정보를 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@CurrentUser User user, @PathVariable Long id) {
        accountService.deleteAccount(user, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "자산 요약 조회", description = "총 자산(모든 계좌의 합계)을 조회")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getAccountSummary(@CurrentUser User user) {
        BigDecimal accountSummary = accountService.getAccountSummary(user);
        return ResponseEntity.ok(ApiResponse.success(accountSummary));
    }
}
