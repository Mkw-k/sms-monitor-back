package com.mk.www.smsmonitor.account.application;

import com.mk.www.smsmonitor.account.api.dto.AccountRequest;
import com.mk.www.smsmonitor.account.api.dto.AccountResponse;
import com.mk.www.smsmonitor.account.infrastructure.AccountEntity;
import com.mk.www.smsmonitor.account.infrastructure.AccountJpaRepository;
import com.mk.www.smsmonitor.user.domain.User;
import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import com.mk.www.smsmonitor.user.infrastructure.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final UserJpaRepository userJpaRepository;
    private final AccountJpaRepository accountJpaRepository;

    @Transactional
    public List<AccountResponse> getMyAccounts(User user) {
        String loginId = (user != null) ? user.getLoginId() : "mkw11";
        UserEntity userEntity = userJpaRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return accountJpaRepository.findAllByUser(userEntity)
                .stream()
                .map(AccountResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AccountEntity createAccount (User user, AccountRequest request){
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

        return accountJpaRepository.save(account);
    }

    @Transactional
    public AccountEntity setDefaultAccount(User user, Long id) {
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

        return account;
    }

    public void deleteAccount(User user, Long id) {
        String loginId = (user != null) ? user.getLoginId() : "mkw11";
        UserEntity userEntity = userJpaRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        AccountEntity account = accountJpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다."));

        if (!account.getUser().getId().equals(userEntity.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        accountJpaRepository.delete(account);
    }

    @Transactional
    public BigDecimal getAccountSummary(User user){
        String loginId = (user != null) ? user.getLoginId() : "mkw11";
        UserEntity userEntity = userJpaRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return accountJpaRepository.findAllByUser(userEntity)
                .stream()
                .map(AccountEntity::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
