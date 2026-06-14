package com.portfolio.digitalatm.account;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {
    private final AccountService accountService;

    @GetMapping
    public List<AccountResponse> findAll() {
        return accountService.findAll();
    }
}
