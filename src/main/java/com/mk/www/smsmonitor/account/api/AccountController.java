package com.mk.www.smsmonitor.account.api;

import com.mk.www.smsmonitor.account.api.dto.AccountRequest;
import com.mk.www.smsmonitor.account.api.dto.AccountResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Account", description = "계좌 관리 API")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountJpaRepository accountJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Operation(summary = "내 계좌 목록 조회", description = "로그인된 사용자의 모든 계좌 정보를 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts(@CurrentUser User user) {
        String loginId = (user != null) ? user.getLoginId() : "mkw11";
        UserEntity userEntity = userJpaRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<AccountResponse> responses = accountJpaRepository.findAllByUser(userEntity)
                .stream()
                .map(AccountResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "계좌 추가", description = "신규 계좌(자산)를 추가")
    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@CurrentUser User user, @RequestBody AccountRequest request) {
        String loginId = (user != null) ? user.getLoginId() : "mkw11";
        UserEntity userEntity = userJpaRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 만약 이번 계좌를 메인으로 설정한다면 기존 메인 계좌 해제
        if (request.isDefault()) {
            accountJpaRepository.findByUserAndIsDefault(userEntity, true)
                    .ifPresent(acc -> {
                        acc.setDefault(false);
                        accountJpaRepository.save(acc);
                    });
        }

        AccountEntity account = AccountEntity.builder()
                .user(userEntity)
                .bankName(request.getBankName())
                .balance(request.getBalance())
                .accountNumber(request.getAccountNumber())
                .isDefault(request.isDefault())
                .build();

        AccountEntity saved = accountJpaRepository.save(account);
        return ResponseEntity.ok(ApiResponse.success(AccountResponse.from(saved)));
    }

    @Operation(summary = "메인 계좌 설정", description = "특정 계좌를 주 결제 계좌로 설정")
    @PutMapping("/{id}/default")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> setDefaultAccount(@CurrentUser User user, @PathVariable Long id) {
        String loginId = (user != null) ? user.getLoginId() : "mkw11";
        UserEntity userEntity = userJpaRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 기존 메인 해제
        accountJpaRepository.findByUserAndIsDefault(userEntity, true)
                .ifPresent(acc -> acc.setDefault(false));

        // 새로운 메인 설정
        AccountEntity account = accountJpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다."));
        account.setDefault(true);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "계좌 삭제", description = "계좌 정보를 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@CurrentUser User user, @PathVariable Long id) {
        String loginId = (user != null) ? user.getLoginId() : "mkw11";
        UserEntity userEntity = userJpaRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        AccountEntity account = accountJpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다."));

        if (!account.getUser().getId().equals(userEntity.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        accountJpaRepository.delete(account);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "자산 요약 조회", description = "총 자산(모든 계좌의 합계)을 조회")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getAccountSummary(@CurrentUser User user) {
        String loginId = (user != null) ? user.getLoginId() : "mkw11";
        UserEntity userEntity = userJpaRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        java.math.BigDecimal total = accountJpaRepository.findAllByUser(userEntity)
                .stream()
                .map(AccountEntity::getBalance)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return ResponseEntity.ok(ApiResponse.success(total));
    }
}
