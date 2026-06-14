package com.portfolio.digitalatm.transaction;

import com.portfolio.digitalatm.account.BankAccount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock private TransactionRepository transactionRepository;

    @InjectMocks private TransactionService transactionService;

    @Test
    void createStoresTransactionWithUniqueCode() {
        BankAccount target = BankAccount.builder().id(1L).accountNumber("100000000001").build();
        when(transactionRepository.existsByTransactionCode(anyString())).thenReturn(false);
        when(transactionRepository.save(any(BankTransaction.class))).thenAnswer(invocation -> {
            BankTransaction transaction = invocation.getArgument(0);
            transaction.setId(1L);
            return transaction;
        });

        BankTransaction transaction = transactionService.create(
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCESS,
                new BigDecimal("50.00"),
                null,
                target,
                "dep-001",
                "yordi",
                "test"
        );

        assertThat(transaction.getTransactionCode()).startsWith("TXN-");
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(transaction.getAmount()).isEqualByComparingTo("50.00");
    }
}
