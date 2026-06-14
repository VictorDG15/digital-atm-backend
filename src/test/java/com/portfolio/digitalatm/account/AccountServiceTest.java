package com.portfolio.digitalatm.account;

import com.portfolio.digitalatm.audit.AuditService;
import com.portfolio.digitalatm.user.AppUser;
import com.portfolio.digitalatm.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditService auditService;

    @InjectMocks private AccountService accountService;

    @Test
    void createGeneratesAccountForUser() {
        AppUser owner = AppUser.builder().id(1L).username("yordi").email("yordi@test.com").build();
        when(userRepository.findByUsername("yordi")).thenReturn(Optional.of(owner));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> {
            BankAccount account = invocation.getArgument(0);
            account.setId(99L);
            return account;
        });

        AccountResponse response = accountService.create("yordi", new CreateAccountRequest(AccountType.SAVINGS, new BigDecimal("100.00")));

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.type()).isEqualTo(AccountType.SAVINGS);
        assertThat(response.balance()).isEqualByComparingTo("100.00");
        verify(auditService).record("yordi", "ACCOUNT_CREATED", "SUCCESS", null);
    }
}
