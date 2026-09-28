package com.dbflabs.finance.ledger.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Balance(LocalDateTime balanceAt, String accountId, BigDecimal balance) {

}
