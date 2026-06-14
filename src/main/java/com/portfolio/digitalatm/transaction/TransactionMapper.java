package com.portfolio.digitalatm.transaction;

public final class TransactionMapper {
    private TransactionMapper() {
    }

    public static TransactionResponse toResponse(BankTransaction transaction) {
        String source = transaction.getSourceAccount() == null ? null : transaction.getSourceAccount().getAccountNumber();
        String target = transaction.getTargetAccount() == null ? null : transaction.getTargetAccount().getAccountNumber();
        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionCode(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getAmount(),
                source,
                target,
                transaction.getIdempotencyKey(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}
