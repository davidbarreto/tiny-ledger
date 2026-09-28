package com.dbflabs.finance.ledger.controller;

import com.dbflabs.finance.ledger.model.Balance;
import com.dbflabs.finance.ledger.model.LedgerRecordRequest;
import com.dbflabs.finance.ledger.model.Transaction;
import com.dbflabs.finance.ledger.service.LedgerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping("/accounts/{accountId}/deposit")
    public Transaction deposit(@PathVariable("accountId") String accountId, @RequestBody LedgerRecordRequest request) {
        return ledgerService.deposit(accountId, request.amount());
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    public Transaction withdraw(@PathVariable("accountId") String accountId, @RequestBody LedgerRecordRequest request) {
        return ledgerService.withdraw(accountId, request.amount());
    }

    @GetMapping("/accounts/{accountId}/balance")
    public Balance viewBalance(@PathVariable("accountId") String accountId) {
        return ledgerService.viewBalance(accountId);
    }

    @GetMapping("/accounts/{accountId}/history")
    public List<Transaction> viewHistory(@PathVariable("accountId") String accountId) {
        return ledgerService.viewHistory(accountId);
    }
}
