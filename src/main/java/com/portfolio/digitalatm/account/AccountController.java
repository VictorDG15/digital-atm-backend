package com.portfolio.digitalatm.account;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(Authentication authentication, @Valid @RequestBody CreateAccountRequest request) {
        return accountService.create(authentication.getName(), request);
    }

    @GetMapping("/my")
    public List<AccountResponse> myAccounts(Authentication authentication) {
        return accountService.myAccounts(authentication.getName());
    }

    @GetMapping("/{id}/balance")
    public BalanceResponse balance(Authentication authentication, @PathVariable Long id) {
        return accountService.balance(authentication.getName(), id);
    }
}
