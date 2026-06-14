package com.portfolio.digitalatm.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<BankTransaction, Long> {
    Optional<BankTransaction> findByIdempotencyKey(String idempotencyKey);
    boolean existsByTransactionCode(String transactionCode);
    List<BankTransaction> findBySourceAccountOwnerUsernameOrTargetAccountOwnerUsernameOrderByCreatedAtDesc(String sourceUsername, String targetUsername);
    List<BankTransaction> findAllByOrderByCreatedAtDesc();
}
