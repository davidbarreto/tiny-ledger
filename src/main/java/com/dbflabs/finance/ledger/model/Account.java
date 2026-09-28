package com.dbflabs.finance.ledger.model;

import java.math.BigDecimal;

public class Account {

    private final String id;
    private BigDecimal balance;

    public Account(String id) {
        this.id = id;
        this.balance = BigDecimal.ZERO;
    }

    public BigDecimal addToBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        return this.balance;
    }

    public BigDecimal subtractFromBalance(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
        return this.balance;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
