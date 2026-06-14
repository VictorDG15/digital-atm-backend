package com.portfolio.digitalatm.atm;

import com.portfolio.digitalatm.transaction.TransactionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/atm")
public class ATMController {
    private final ATMService atmService;

    @PostMapping("/deposit")
    public TransactionResponse deposit(Authentication authentication, @Valid @RequestBody DepositRequest request) {
        return atmService.deposit(authentication.getName(), request);
    }

    @PostMapping("/withdraw")
    public TransactionResponse withdraw(Authentication authentication, @Valid @RequestBody WithdrawRequest request) {
        return atmService.withdraw(authentication.getName(), request);
    }

    @PostMapping("/transfer")
    public TransactionResponse transfer(Authentication authentication, @Valid @RequestBody TransferRequest request) {
        return atmService.transfer(authentication.getName(), request);
    }

    @GetMapping("/transactions")
    public List<TransactionResponse> myTransactions(Authentication authentication) {
        return atmService.myTransactions(authentication.getName());
    }
}
