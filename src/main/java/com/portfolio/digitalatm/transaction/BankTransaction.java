package com.portfolio.digitalatm.transaction;

import com.portfolio.digitalatm.account.BankAccount;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transactions_code", columnList = "transaction_code"),
        @Index(name = "idx_transactions_idempotency", columnList = "idempotency_key")
})
public class BankTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id")
    private BankAccount sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_account_id")
    private BankAccount targetAccount;

    @Column(nullable = false, unique = true, length = 40)
    private String transactionCode;

    @Column(unique = true, length = 120)
    private String idempotencyKey;

    @Column(nullable = false, length = 120)
    private String createdByUsername;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        status = status == null ? TransactionStatus.PENDING : status;
    }
}
