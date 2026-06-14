package com.portfolio.digitalatm.account;

public final class AccountMapper {
    private AccountMapper() {
    }

    public static AccountResponse toResponse(BankAccount account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getType(),
                account.getStatus(),
                account.getBalance(),
                account.getOwner().getId(),
                account.getOwner().getUsername(),
                account.getCreatedAt()
        );
    }

    public static BalanceResponse toBalance(BankAccount account) {
        return new BalanceResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getStatus(),
                account.getBalance()
        );
    }
}
