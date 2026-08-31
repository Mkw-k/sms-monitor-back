package com.mk.www.smsmonitor.account.application;

import com.mk.www.smsmonitor.account.api.dto.AccountRequest;
import com.mk.www.smsmonitor.account.api.dto.AccountResponse;
import com.mk.www.smsmonitor.account.infrastructure.AccountEntity;
import com.mk.www.smsmonitor.account.infrastructure.AccountJpaRepository;
import com.mk.www.smsmonitor.user.domain.User;
import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import com.mk.www.smsmonitor.user.infrastructure.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AccountServiceIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountJpaRepository accountJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    private UserEntity userEntity;
    private User testUser;

    @BeforeEach
    void setUp() {
        accountJpaRepository.deleteAll();
        userJpaRepository.deleteAll();

        userEntity = UserEntity.builder()
                .loginId("testuser")
                .password("password123")
                .role("ROLE_USER")
                .isApproved(true)
                .point(1000L)
                .build();
        userJpaRepository.save(userEntity);

        testUser = User.builder()
                .loginId("testuser")
                .role("ROLE_USER")
                .isApproved(true)
                .build();
    }

    @Test
    @DisplayName("신규 계좌를 성공적으로 등록한다")
    void createAccount_Success() {
        // given
        AccountRequest request = new AccountRequest();
        request.setBankName("신한은행");
        request.setAccountNumber("110-123-456789");
        request.setBalance(new BigDecimal("500000"));
        request.setDefault(true);

        // when
        AccountEntity created = accountService.createAccount(testUser, request);

        // then
        assertThat(created.getId()).isNotNull();
        assertThat(created.getBankName()).isEqualTo("신한은행");
        assertThat(created.getBalance()).isEqualByComparingTo(new BigDecimal("500000"));
        assertThat(created.isDefault()).isTrue();

        List<AccountResponse> myAccounts = accountService.getMyAccounts(testUser);
        assertThat(myAccounts).hasSize(1);
    }

    @Test
    @DisplayName("새로운 기본 계좌 등록 시 기존 기본 계좌는 false로 변경된다")
    void createAccount_SwitchDefaultAccount() {
        // given
        AccountRequest request1 = new AccountRequest();
        request1.setBankName("국민은행");
        request1.setAccountNumber("111-222");
        request1.setBalance(new BigDecimal("100000"));
        request1.setDefault(true);
        accountService.createAccount(testUser, request1);

        AccountRequest request2 = new AccountRequest();
        request2.setBankName("토스뱅크");
        request2.setAccountNumber("333-444");
        request2.setBalance(new BigDecimal("200000"));
        request2.setDefault(true);

        // when
        accountService.createAccount(testUser, request2);

        // then
        List<AccountResponse> accounts = accountService.getMyAccounts(testUser);
        assertThat(accounts).hasSize(2);

        AccountResponse oldDefault = accounts.stream()
                .filter(a -> a.getBankName().equals("국민은행"))
                .findFirst()
                .orElseThrow();
        AccountResponse newDefault = accounts.stream()
                .filter(a -> a.getBankName().equals("토스뱅크"))
                .findFirst()
                .orElseThrow();

        assertThat(oldDefault.isDefault()).isFalse();
        assertThat(newDefault.isDefault()).isTrue();
    }

    @Test
    @DisplayName("기본 계좌를 특정 계좌로 변경 설정할 수 있다")
    void setDefaultAccount_Success() {
        // given
        AccountRequest request1 = new AccountRequest();
        request1.setBankName("국민은행");
        request1.setAccountNumber("111-222");
        request1.setBalance(new BigDecimal("100000"));
        request1.setDefault(true);
        AccountEntity acc1 = accountService.createAccount(testUser, request1);

        AccountRequest request2 = new AccountRequest();
        request2.setBankName("카카오뱅크");
        request2.setAccountNumber("333-444");
        request2.setBalance(new BigDecimal("200000"));
        request2.setDefault(false);
        AccountEntity acc2 = accountService.createAccount(testUser, request2);

        // when
        accountService.setDefaultAccount(testUser, acc2.getId());

        // then
        AccountEntity updatedAcc1 = accountJpaRepository.findById(acc1.getId()).orElseThrow();
        AccountEntity updatedAcc2 = accountJpaRepository.findById(acc2.getId()).orElseThrow();

        assertThat(updatedAcc1.isDefault()).isFalse();
        assertThat(updatedAcc2.isDefault()).isTrue();
    }

    @Test
    @DisplayName("계좌를 성공적으로 삭제할 수 있다")
    void deleteAccount_Success() {
        // given
        AccountRequest request = new AccountRequest();
        request.setBankName("우리은행");
        request.setAccountNumber("555-666");
        request.setBalance(new BigDecimal("300000"));
        request.setDefault(false);
        AccountEntity acc = accountService.createAccount(testUser, request);

        // when
        accountService.deleteAccount(testUser, acc.getId());

        // then
        assertThat(accountJpaRepository.findById(acc.getId())).isEmpty();
    }

    @Test
    @DisplayName("타인의 계좌는 삭제할 수 없다")
    void deleteAccount_AccessDenied() {
        // given
        UserEntity otherUserEntity = UserEntity.builder()
                .loginId("otheruser")
                .password("pass")
                .role("ROLE_USER")
                .isApproved(true)
                .point(0L)
                .build();
        userJpaRepository.save(otherUserEntity);

        AccountEntity otherAccount = AccountEntity.builder()
                .user(otherUserEntity)
                .bankName("농협은행")
                .accountNumber("999-000")
                .balance(new BigDecimal("10000"))
                .isDefault(false)
                .build();
        accountJpaRepository.save(otherAccount);

        // when & then
        assertThatThrownBy(() -> accountService.deleteAccount(testUser, otherAccount.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제 권한이 없습니다.");
    }

    @Test
    @DisplayName("사용자의 모든 계좌 잔액 합계를 정확히 계산한다")
    void getAccountSummary_Success() {
        // given
        AccountRequest request1 = new AccountRequest();
        request1.setBankName("은행A");
        request1.setAccountNumber("111");
        request1.setBalance(new BigDecimal("150000.50"));
        accountService.createAccount(testUser, request1);

        AccountRequest request2 = new AccountRequest();
        request2.setBankName("은행B");
        request2.setAccountNumber("222");
        request2.setBalance(new BigDecimal("250000.50"));
        accountService.createAccount(testUser, request2);

        // when
        BigDecimal total = accountService.getAccountSummary(testUser);

        // then
        assertThat(total).isEqualByComparingTo(new BigDecimal("400001.00"));
    }
}
