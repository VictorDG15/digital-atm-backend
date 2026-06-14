package com.portfolio.digitalatm.card;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cards")
public class CardController {
    private final CardService cardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponse create(Authentication authentication, @Valid @RequestBody CreateCardRequest request) {
        return cardService.create(authentication.getName(), request);
    }

    @GetMapping("/my")
    public List<CardResponse> myCards(Authentication authentication) {
        return cardService.myCards(authentication.getName());
    }

    @PatchMapping("/{id}/block")
    public CardResponse block(Authentication authentication, @PathVariable Long id) {
        return cardService.block(authentication.getName(), id);
    }

    @PostMapping("/{id}/validate-pin")
    public PinValidationResponse validatePin(Authentication authentication, @PathVariable Long id, @Valid @RequestBody PinValidationRequest request) {
        return cardService.validatePin(authentication.getName(), id, request);
    }
}
