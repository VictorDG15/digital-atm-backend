package com.portfolio.digitalatm.account;

import com.portfolio.digitalatm.audit.AuditService;
import com.portfolio.digitalatm.common.exception.BadRequestException;
import com.portfolio.digitalatm.common.exception.ForbiddenException;
import com.portfolio.digitalatm.common.exception.NotFoundException;
import com.portfolio.digitalatm.user.AppUser;
import com.portfolio.digitalatm.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public AccountResponse create(String username, CreateAccountRequest request) {
        if (request.initialBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El saldo inicial no puede ser negativo");
        }
        AppUser owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        BankAccount account = BankAccount.builder()
                .owner(owner)
                .type(request.type())
                .status(AccountStatus.ACTIVE)
                .balance(request.initialBalance())
                .accountNumber(generateAccountNumber())
                .build();
        BankAccount saved = accountRepository.save(account);
        auditService.record(username, "ACCOUNT_CREATED", "SUCCESS", null);
        return AccountMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> myAccounts(String username) {
        return accountRepository.findByOwnerUsernameOrderByCreatedAtDesc(username).stream()
                .map(AccountMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BalanceResponse balance(String username, Long accountId) {
        BankAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada"));
        if (!account.getOwner().getUsername().equals(username)) {
            throw new ForbiddenException("No puedes ver esta cuenta");
        }
        return AccountMapper.toBalance(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> findAll() {
        return accountRepository.findAll().stream().map(AccountMapper::toResponse).toList();
    }

    public BankAccount getActiveAccountForUser(Long accountId, String username) {
        BankAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada"));
        if (!account.getOwner().getUsername().equals(username)) {
            throw new ForbiddenException("No puedes operar esta cuenta");
        }
        if (!account.isActive()) {
            throw new BadRequestException("La cuenta no está activa");
        }
        return account;
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = "10" + (1000000000L + Math.abs(RANDOM.nextLong() % 9000000000L));
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
