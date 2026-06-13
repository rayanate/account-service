package com.charlesschwab.accountService.service;

import com.charlesschwab.accountService.entity.AppliedTransaction;
import com.charlesschwab.accountService.repository.AppliedTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransactionServiceTests {

    @Autowired
    TransactionService transactionService;

    @Autowired
    AppliedTransactionRepository repository;

    @BeforeEach
    void cleanup() {
        repository.deleteAll();
    }

    @Test
    void applyTransaction_createsRecordAndReturnsIt() {
        String eventId = "evt-create-1";
        String accountId = "acct-1";
        BigDecimal amount = new BigDecimal("100.00");

        AppliedTransaction saved = transactionService.applyTransaction(eventId, accountId, amount);

        assertNotNull(saved);
        assertEquals(eventId, saved.getEventId());
        assertEquals(accountId, saved.getAccountId());
        assertEquals(0, amount.compareTo(saved.getAmount()));

        // repository should contain one record
        assertTrue(repository.findById(eventId).isPresent());
        assertEquals(1, repository.findByAccountId(accountId).size());
    }

    @Test
    void applyTransaction_isIdempotentOnDuplicateEventId() {
        String eventId = "evt-dup-1";
        String accountId = "acct-dup";
        BigDecimal amount = new BigDecimal("42.00");

        AppliedTransaction first = transactionService.applyTransaction(eventId, accountId, amount);
        AppliedTransaction second = transactionService.applyTransaction(eventId, accountId, amount);

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first.getEventId(), second.getEventId());

        // only one persisted row for this eventId
        assertEquals(1, repository.findByAccountId(accountId).size());
        assertEquals(1, repository.count());
    }

    @Test
    void getBalance_sumsAllTransactions() {
        String accountId = "acct-balance";

        transactionService.applyTransaction("evt-b-1", accountId, new BigDecimal("10.00"));
        transactionService.applyTransaction("evt-b-2", accountId, new BigDecimal("-3.25"));
        transactionService.applyTransaction("evt-b-3", accountId, new BigDecimal("2.75"));

        BigDecimal balance = transactionService.getBalance(accountId);

        // 10.00 - 3.25 + 2.75 = 9.50
        assertEquals(0, new BigDecimal("9.50").compareTo(balance));
    }

    @Test
    void getRecentTransactions_returnsMostRecentFirst() throws InterruptedException {
        String accountId = "acct-recent";

        AppliedTransaction t1 = transactionService.applyTransaction("evt-r-1", accountId, new BigDecimal("1.00"));
        // ensure timestamps are ordered
        Thread.sleep(5);
        AppliedTransaction t2 = transactionService.applyTransaction("evt-r-2", accountId, new BigDecimal("2.00"));
        Thread.sleep(5);
        AppliedTransaction t3 = transactionService.applyTransaction("evt-r-3", accountId, new BigDecimal("3.00"));

        List<AppliedTransaction> recent2 = transactionService.getRecentTransactions(accountId, 2);

        assertEquals(2, recent2.size());
        // most recent should be t3 then t2
        assertEquals(t3.getEventId(), recent2.get(0).getEventId());
        assertEquals(t2.getEventId(), recent2.get(1).getEventId());
    }
}

