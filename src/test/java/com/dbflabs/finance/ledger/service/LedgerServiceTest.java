package com.dbflabs.finance.ledger.service;

import com.dbflabs.finance.ledger.exception.AccountNotFoundException;
import com.dbflabs.finance.ledger.exception.InsufficientFundsException;
import com.dbflabs.finance.ledger.exception.InvalidAmountException;
import com.dbflabs.finance.ledger.model.Account;
import com.dbflabs.finance.ledger.model.Transaction;
import com.dbflabs.finance.ledger.repository.LedgerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class LedgerServiceTest {

    private LedgerService service;
    private Account account;
    private Clock clock;

    @BeforeEach
    void init() {
        clock = Clock.fixed(
                Instant.parse("2020-11-30T19:05:00-03:00"), ZoneId.of("UTC"));
        account = new Account("testAcc1");
        var repository = new LedgerRepository();
        repository.insertAccount(account);
        service = new LedgerService(repository, clock);
    }

    @Test
    void whenAccountHasInsufficientFoundsThenInsufficientFundsExceptionIsThrown() {
        var accountId = account.getId();
        assertThatThrownBy(() -> service.withdraw(accountId, BigDecimal.TEN))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Insufficient funds for accountId: %s", accountId);
    }

    @ParameterizedTest
    @NullSource
    @CsvSource({"0.00", "-1.00"})
    void whenAmountIdIsNotValidThenInvalidAmountExceptionIsThrown(BigDecimal invalidAmount) {
        var accountId = account.getId();
        assertThatThrownBy(() -> service.withdraw(accountId, invalidAmount))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("Invalid amount: %s", invalidAmount);

        assertThatThrownBy(() -> service.deposit(accountId, invalidAmount))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("Invalid amount: %s", invalidAmount);
    }

    @Test
    void whenAccountIdIsNotFoundThenAccountNotFoundExceptionIsThrown() {
        var accountId = "accountNotFound";
        assertThatThrownBy(() -> service.withdraw(accountId, BigDecimal.ONE))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account not found for accountId: %s", accountId);

        assertThatThrownBy(() -> service.deposit(accountId, BigDecimal.TWO))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account not found for accountId: %s", accountId);
    }

    @Test
    void whenAccountHasEnoughFundsThenWithdrawIsDoneSuccessfully() {
        // Given
        var accountId = account.getId();
        service.deposit(accountId, BigDecimal.TWO);

        // When
        var transaction = service.withdraw(accountId, BigDecimal.ONE);

        // Then
        assertNotNull(transaction);
        assertEquals(accountId, transaction.accountId());
        assertEquals(Transaction.TransactionType.DEBIT, transaction.type());
        assertEquals(BigDecimal.ONE, transaction.amount());
        assertEquals(BigDecimal.TWO, transaction.balanceBefore());
        assertEquals(BigDecimal.ONE, transaction.balanceAfter());
        assertEquals(LocalDateTime.parse("2020-11-30T22:05:00"), transaction.creation());
    }

    @Test
    void whenAccountExistsAndAmountIsValidThenDepositIsDoneSuccessfully() {
        // Given
        var accountId = account.getId();

        // When
        var transaction = service.deposit(accountId, BigDecimal.TEN);

        // Then
        assertNotNull(transaction);
        assertEquals(accountId, transaction.accountId());
        assertEquals(Transaction.TransactionType.CREDIT, transaction.type());
        assertEquals(BigDecimal.TEN, transaction.amount());
        assertEquals(BigDecimal.ZERO, transaction.balanceBefore());
        assertEquals(BigDecimal.TEN, transaction.balanceAfter());
        assertEquals(LocalDateTime.parse("2020-11-30T22:05:00"), transaction.creation());
    }

    @Test
    void whenNoTransactionsOnAccountThenHistoryOfTransactionsReturnsEmptyList() {
        // Given
        var accountId = account.getId();

        // When
        var history = service.viewHistory(accountId);

        // Then
        assertEquals(List.of(), history);
    }

    @Test
    void whenTransactionsAreDoneThenHistoryIsReturnedAccordingly() {
        // Given
        var accountId = account.getId();
        var transaction1 = service.deposit(accountId, BigDecimal.TEN);
        var transaction2 = service.withdraw(accountId, BigDecimal.ONE);
        var transaction3 = service.deposit(accountId, BigDecimal.ONE);

        // When
        var history = service.viewHistory(accountId);

        // Then
        assertThat(history).hasSize(3).containsExactly(transaction1, transaction2, transaction3);
    }

    @Test
    void whenTransactionsAreDoneThenBalanceIsReturnedAccordingly() {
        // Given
        var accountId = account.getId();
        service.deposit(accountId, BigDecimal.TEN);
        service.withdraw(accountId, BigDecimal.ONE);
        service.deposit(accountId, BigDecimal.ONE);

        // When
        var balance = service.viewBalance(accountId);

        // Then
        assertEquals(accountId, balance.accountId());
        assertEquals(BigDecimal.TEN, balance.balance());
        assertEquals(LocalDateTime.parse("2020-11-30T22:05:00"), balance.balanceAt());
    }

    @Test
    void whenNotTransactionsDoneThenBalanceIsZero() {
        // Given
        var accountId = account.getId();

        // When
        var balance = service.viewBalance(accountId);

        // Then
        assertEquals(accountId, balance.accountId());
        assertEquals(BigDecimal.ZERO, balance.balance());
        assertEquals(LocalDateTime.parse("2020-11-30T22:05:00"), balance.balanceAt());
    }
}