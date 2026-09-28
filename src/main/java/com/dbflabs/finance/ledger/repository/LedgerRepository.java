package com.dbflabs.finance.ledger.repository;

import com.dbflabs.finance.ledger.model.Account;
import com.dbflabs.finance.ledger.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class LedgerRepository {

    private final Map<String, Account> accountById;
    private final Map<String, List<Transaction>> transactionsByAccountId;

    public LedgerRepository() {
        this.accountById = new ConcurrentHashMap<>();
        // Adding a test account in order to facilitate tests on Transaction API
        // while Account API is not ready
        insertAccount(new Account("1"));
        this.transactionsByAccountId = new ConcurrentHashMap<>();
    }

    public List<Transaction> findTransactionsByAccountId(String accountId) {
        return transactionsByAccountId.getOrDefault(accountId, Collections.emptyList());
    }

    public void insertTransaction(Account account, Transaction transaction) {
        transactionsByAccountId.computeIfAbsent(account.getId(), k -> new ArrayList<>()).add(transaction);
    }

    public void insertAccount(Account account) {
        this.accountById.put(account.getId(), account);
    }

    public Account findAccountById(String accountId) {
        return this.accountById.get(accountId);
    }
}
