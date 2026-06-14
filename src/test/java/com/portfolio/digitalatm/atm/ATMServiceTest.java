package com.portfolio.digitalatm.atm;

import com.portfolio.digitalatm.account.*;
import com.portfolio.digitalatm.audit.AuditService;
import com.portfolio.digitalatm.card.CardService;
import com.portfolio.digitalatm.transaction.*;
import com.portfolio.digitalatm.user.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ATMServiceTest {
    @Mock private AccountRepository accountRepository;
    @Mock private CardService cardService;
    @Mock private TransactionService transactionService;
    @Mock private AuditService auditService;

    @InjectMocks private ATMService atmService;

    @Test
    void depositIncreasesBalanceAndCreatesTransaction() {
        AppUser owner = AppUser.builder().id(1L).username("yordi").build();
        BankAccount account = BankAccount.builder()
                .id(10L)
                .accountNumber("100000000001")
                .owner(owner)
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("500.00"))
                .build();
        when(transactionService.findByIdempotencyKey("dep-001")).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(account));
        when(transactionService.create(any(), any(), any(), any(), any(), anyString(), anyString(), anyString()))
                .thenAnswer(invocation -> BankTransaction.builder()
                        .id(1L)
                        .type(invocation.getArgument(0))
                        .status(invocation.getArgument(1))
                        .amount(invocation.getArgument(2))
                        .sourceAccount(invocation.getArgument(3))
                        .targetAccount(invocation.getArgument(4))
                        .idempotencyKey(invocation.getArgument(5))
                        .createdByUsername(invocation.getArgument(6))
                        .description(invocation.getArgument(7))
                        .transactionCode("TXN-TEST")
                        .build());

        TransactionResponse response = atmService.deposit("yordi", new DepositRequest(10L, new BigDecimal("100.00"), "dep-001", "test"));

        assertThat(account.getBalance()).isEqualByComparingTo("600.00");
        assertThat(response.transactionCode()).isEqualTo("TXN-TEST");
        verify(auditService).record("yordi", "ATM_DEPOSIT", "SUCCESS", null);
    }

    @Test
    void withdrawValidatesPinAndDiscountsBalance() {
        AppUser owner = AppUser.builder().id(1L).username("yordi").build();
        BankAccount account = BankAccount.builder()
                .id(10L)
                .accountNumber("100000000001")
                .owner(owner)
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("500.00"))
                .build();
        when(transactionService.findByIdempotencyKey("wd-001")).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(account));
        when(transactionService.create(any(), any(), any(), any(), any(), anyString(), anyString(), anyString()))
                .thenAnswer(invocation -> BankTransaction.builder()
                        .id(2L)
                        .type(invocation.getArgument(0))
                        .status(invocation.getArgument(1))
                        .amount(invocation.getArgument(2))
                        .sourceAccount(invocation.getArgument(3))
                        .targetAccount(invocation.getArgument(4))
                        .idempotencyKey(invocation.getArgument(5))
                        .createdByUsername(invocation.getArgument(6))
                        .description(invocation.getArgument(7))
                        .transactionCode("TXN-WD")
                        .build());

        TransactionResponse response = atmService.withdraw("yordi", new WithdrawRequest(10L, 5L, "1234", new BigDecimal("75.00"), "wd-001", "test"));

        assertThat(account.getBalance()).isEqualByComparingTo("425.00");
        assertThat(response.type()).isEqualTo(TransactionType.WITHDRAWAL);
        verify(cardService).validatePinForOperation(5L, account, "1234", "yordi");
    }
}
