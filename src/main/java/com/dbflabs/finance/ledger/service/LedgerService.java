package com.dbflabs.finance.ledger.service;

import com.dbflabs.finance.ledger.exception.AccountNotFoundException;
import com.dbflabs.finance.ledger.exception.InsufficientFundsException;
import com.dbflabs.finance.ledger.exception.InvalidAmountException;
import com.dbflabs.finance.ledger.model.Account;
import com.dbflabs.finance.ledger.model.Balance;
import com.dbflabs.finance.ledger.model.Transaction;
import com.dbflabs.finance.ledger.repository.LedgerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class LedgerService {

    private final LedgerRepository repository;
    private final Clock clock;

    @Autowired
    public LedgerService(LedgerRepository repository) {
        this(repository, Clock.system(ZoneId.of("UTC")));
    }

    public LedgerService(LedgerRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public Transaction deposit(String accountId, BigDecimal amount) {
        validateAmount(amount);
        var account = loadAccount(accountId);

        var balanceBefore = account.getBalance();
        var balanceAfter = account.addToBalance(amount);
        var transaction = new Transaction(
                UUID.randomUUID(),
                LocalDateTime.now(clock),
                accountId,
                amount,
                Transaction.TransactionType.CREDIT,
                balanceBefore,
                balanceAfter
        );
        repository.insertTransaction(account, transaction);
        return transaction;
    }

    public Transaction withdraw(String accountId, BigDecimal amount) {
        validateAmount(amount);
        var account = loadAccount(accountId);

        var balanceBefore = account.getBalance();
        if (balanceBefore.subtract(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds for accountId: " + accountId);
        }

        var balanceAfter = account.subtractFromBalance(amount);
        var transaction = new Transaction(
                UUID.randomUUID(),
                LocalDateTime.now(clock),
                accountId,
                amount,
                Transaction.TransactionType.DEBIT,
                balanceBefore,
                balanceAfter);
        repository.insertTransaction(account, transaction);
        return transaction;
    }

    public Balance viewBalance(String accountId) {
        return new Balance(LocalDateTime.now(clock), accountId, loadAccount(accountId).getBalance());
    }

    public List<Transaction> viewHistory(String accountId) {
        loadAccount(accountId); //Called to validate accountId
        return repository.findTransactionsByAccountId(accountId);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Invalid amount: " + amount);
        }
    }

    private Account loadAccount(String accountId) {
        var account = repository.findAccountById(accountId);
        if (account == null) {
            throw new AccountNotFoundException("Account not found for accountId: " + accountId);
        }
        return account;
    }
}
