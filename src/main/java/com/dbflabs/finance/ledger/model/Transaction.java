package com.dbflabs.finance.ledger.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Transaction(
        UUID id,
        LocalDateTime creation,
        String accountId,
        BigDecimal amount,
        TransactionType type,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter
)
{
    public enum TransactionType {
        DEBIT,
        CREDIT
    }
}
