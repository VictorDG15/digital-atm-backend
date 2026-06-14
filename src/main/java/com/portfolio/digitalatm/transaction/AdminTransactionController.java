package com.portfolio.digitalatm.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/transactions")
public class AdminTransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public List<TransactionResponse> findAll() {
        return transactionService.findAll();
    }
}
