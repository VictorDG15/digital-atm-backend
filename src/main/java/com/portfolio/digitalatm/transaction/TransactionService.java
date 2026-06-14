package com.portfolio.digitalatm.transaction;

import com.portfolio.digitalatm.account.BankAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public Optional<TransactionResponse> findByIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }
        return transactionRepository.findByIdempotencyKey(idempotencyKey).map(TransactionMapper::toResponse);
    }

    @Transactional
    public BankTransaction create(TransactionType type,
                                  TransactionStatus status,
                                  BigDecimal amount,
                                  BankAccount sourceAccount,
                                  BankAccount targetAccount,
                                  String idempotencyKey,
                                  String createdByUsername,
                                  String description) {
        BankTransaction transaction = BankTransaction.builder()
                .type(type)
                .status(status)
                .amount(amount)
                .sourceAccount(sourceAccount)
                .targetAccount(targetAccount)
                .transactionCode(generateCode())
                .idempotencyKey(normalizeKey(idempotencyKey))
                .createdByUsername(createdByUsername)
                .description(description)
                .build();
        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> myTransactions(String username) {
        return transactionRepository.findBySourceAccountOwnerUsernameOrTargetAccountOwnerUsernameOrderByCreatedAtDesc(username, username)
                .stream()
                .map(TransactionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll() {
        return transactionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(TransactionMapper::toResponse)
                .toList();
    }

    private String normalizeKey(String idempotencyKey) {
        return idempotencyKey == null || idempotencyKey.isBlank() ? null : idempotencyKey.trim();
    }

    private String generateCode() {
        String code;
        do {
            code = "TXN-" + Instant.now().toEpochMilli() + "-" + (100000 + RANDOM.nextInt(900000));
        } while (transactionRepository.existsByTransactionCode(code));
        return code;
    }
}
