package com.portfolio.digitalatm.card;

import com.portfolio.digitalatm.account.*;
import com.portfolio.digitalatm.audit.AuditService;
import com.portfolio.digitalatm.common.exception.BadRequestException;
import com.portfolio.digitalatm.common.exception.ForbiddenException;
import com.portfolio.digitalatm.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_PIN_ATTEMPTS = 3;

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional
    public CardResponse create(String username, CreateCardRequest request) {
        BankAccount account = accountRepository.findByIdAndOwnerUsername(request.accountId(), username)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada"));
        if (!account.isActive()) {
            throw new BadRequestException("La cuenta no está activa");
        }
        DebitCard card = DebitCard.builder()
                .account(account)
                .cardNumber(generateCardNumber())
                .pinHash(passwordEncoder.encode(request.pin()))
                .status(CardStatus.ACTIVE)
                .build();
        DebitCard saved = cardRepository.save(card);
        auditService.record(username, "CARD_CREATED", "SUCCESS", null);
        return CardMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CardResponse> myCards(String username) {
        return cardRepository.findByAccountOwnerUsernameOrderByCreatedAtDesc(username).stream()
                .map(CardMapper::toResponse)
                .toList();
    }

    @Transactional
    public CardResponse block(String username, Long cardId) {
        DebitCard card = cardRepository.findByIdAndAccountOwnerUsername(cardId, username)
                .orElseThrow(() -> new NotFoundException("Tarjeta no encontrada"));
        card.setStatus(CardStatus.BLOCKED);
        auditService.record(username, "CARD_BLOCKED", "SUCCESS", null);
        return CardMapper.toResponse(cardRepository.save(card));
    }

    @Transactional
    public PinValidationResponse validatePin(String username, Long cardId, PinValidationRequest request) {
        DebitCard card = cardRepository.findByIdAndAccountOwnerUsername(cardId, username)
                .orElseThrow(() -> new NotFoundException("Tarjeta no encontrada"));
        validatePinForOperation(card, card.getAccount(), request.pin(), username);
        return new PinValidationResponse(true, "PIN correcto");
    }

    public DebitCard validatePinForOperation(Long cardId, BankAccount account, String pin, String username) {
        DebitCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Tarjeta no encontrada"));
        if (!card.getAccount().getId().equals(account.getId())) {
            throw new ForbiddenException("La tarjeta no pertenece a la cuenta");
        }
        if (!card.getAccount().getOwner().getUsername().equals(username)) {
            throw new ForbiddenException("No puedes usar esta tarjeta");
        }
        validatePinForOperation(card, account, pin, username);
        return card;
    }

    private void validatePinForOperation(DebitCard card, BankAccount account, String pin, String username) {
        if (card.isBlocked()) {
            auditService.record(username, "PIN_VALIDATION", "CARD_BLOCKED", null);
            throw new BadRequestException("La tarjeta está bloqueada");
        }
        if (!account.isActive()) {
            auditService.record(username, "PIN_VALIDATION", "ACCOUNT_BLOCKED", null);
            throw new BadRequestException("La cuenta no está activa");
        }
        if (!passwordEncoder.matches(pin, card.getPinHash())) {
            int attempts = account.getFailedPinAttempts() + 1;
            account.setFailedPinAttempts(attempts);
            if (attempts >= MAX_PIN_ATTEMPTS) {
                account.setStatus(AccountStatus.BLOCKED);
                card.setStatus(CardStatus.BLOCKED);
                auditService.record(username, "PIN_VALIDATION", "ACCOUNT_BLOCKED_BY_PIN", null);
                throw new BadRequestException("PIN incorrecto. La cuenta fue bloqueada por seguridad");
            }
            auditService.record(username, "PIN_VALIDATION", "FAILED", null);
            throw new BadRequestException("PIN incorrecto");
        }
        if (account.getFailedPinAttempts() > 0) {
            account.setFailedPinAttempts(0);
        }
        auditService.record(username, "PIN_VALIDATION", "SUCCESS", null);
    }

    private String generateCardNumber() {
        String cardNumber;
        do {
            cardNumber = "400000" + String.format("%010d", Math.abs(RANDOM.nextLong() % 10_000_000_000L));
        } while (cardRepository.existsByCardNumber(cardNumber));
        return cardNumber;
    }
}
