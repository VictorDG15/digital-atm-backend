package com.portfolio.digitalatm.atm;

import com.portfolio.digitalatm.account.*;
import com.portfolio.digitalatm.audit.AuditService;
import com.portfolio.digitalatm.card.CardService;
import com.portfolio.digitalatm.common.exception.BadRequestException;
import com.portfolio.digitalatm.common.exception.NotFoundException;
import com.portfolio.digitalatm.transaction.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ATMService {
    private final AccountRepository accountRepository;
    private final CardService cardService;
    private final TransactionService transactionService;
    private final AuditService auditService;

    @Transactional
    public TransactionResponse deposit(String username, DepositRequest request) {
        var duplicated = transactionService.findByIdempotencyKey(request.idempotencyKey());
        if (duplicated.isPresent()) {
            return duplicated.get();
        }

        BankAccount account = accountRepository.findByIdForUpdate(request.accountId())
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada"));
        validateOwnerAndActive(account, username);

        account.setBalance(account.getBalance().add(request.amount()));
        BankTransaction transaction = transactionService.create(
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCESS,
                request.amount(),
                null,
                account,
                request.idempotencyKey(),
                username,
                valueOrDefault(request.description(), "Depósito en cajero")
        );
        auditService.record(username, "ATM_DEPOSIT", "SUCCESS", null);
        log.info("deposit code={} account={} amount={}", transaction.getTransactionCode(), account.getAccountNumber(), request.amount());
        return TransactionMapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponse withdraw(String username, WithdrawRequest request) {
        var duplicated = transactionService.findByIdempotencyKey(request.idempotencyKey());
        if (duplicated.isPresent()) {
            return duplicated.get();
        }

        BankAccount account = accountRepository.findByIdForUpdate(request.accountId())
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada"));
        validateOwnerAndActive(account, username);
        cardService.validatePinForOperation(request.cardId(), account, request.pin(), username);
        validateSufficientFunds(account, request.amount());

        account.setBalance(account.getBalance().subtract(request.amount()));
        BankTransaction transaction = transactionService.create(
                TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCESS,
                request.amount(),
                account,
                null,
                request.idempotencyKey(),
                username,
                valueOrDefault(request.description(), "Retiro en cajero")
        );
        auditService.record(username, "ATM_WITHDRAWAL", "SUCCESS", null);
        log.info("withdraw code={} account={} amount={}", transaction.getTransactionCode(), account.getAccountNumber(), request.amount());
        return TransactionMapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponse transfer(String username, TransferRequest request) {
        var duplicated = transactionService.findByIdempotencyKey(request.idempotencyKey());
        if (duplicated.isPresent()) {
            return duplicated.get();
        }

        BankAccount source = accountRepository.findByIdForUpdate(request.sourceAccountId())
                .orElseThrow(() -> new NotFoundException("Cuenta origen no encontrada"));
        BankAccount target = accountRepository.findByAccountNumberForUpdate(request.targetAccountNumber())
                .orElseThrow(() -> new NotFoundException("Cuenta destino no encontrada"));
        validateOwnerAndActive(source, username);
        validateActive(target, "La cuenta destino no está activa");
        if (source.getId().equals(target.getId())) {
            throw new BadRequestException("No puedes transferir a la misma cuenta");
        }
        cardService.validatePinForOperation(request.cardId(), source, request.pin(), username);
        validateSufficientFunds(source, request.amount());

        source.setBalance(source.getBalance().subtract(request.amount()));
        target.setBalance(target.getBalance().add(request.amount()));
        BankTransaction transaction = transactionService.create(
                TransactionType.TRANSFER,
                TransactionStatus.SUCCESS,
                request.amount(),
                source,
                target,
                request.idempotencyKey(),
                username,
                valueOrDefault(request.description(), "Transferencia entre cuentas")
        );
        auditService.record(username, "ATM_TRANSFER", "SUCCESS", null);
        log.info("transfer code={} source={} target={} amount={}", transaction.getTransactionCode(), source.getAccountNumber(), target.getAccountNumber(), request.amount());
        return TransactionMapper.toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> myTransactions(String username) {
        return transactionService.myTransactions(username);
    }

    private void validateOwnerAndActive(BankAccount account, String username) {
        if (!account.getOwner().getUsername().equals(username)) {
            throw new BadRequestException("La cuenta no pertenece al usuario autenticado");
        }
        validateActive(account, "La cuenta no está activa");
    }

    private void validateActive(BankAccount account, String message) {
        if (!account.isActive()) {
            throw new BadRequestException(message);
        }
    }

    private void validateSufficientFunds(BankAccount account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Saldo insuficiente");
        }
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
